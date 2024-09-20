package com.fsdm.tools.thread.shard.metrics;

import com.fsdm.tools.thread.metrics.FsmdMeterRegistry;
import com.fsdm.tools.thread.shard.ShardExecutor;
import com.fsdm.tools.thread.shard.impl.ShardExecutorImpl;
import com.fsdm.tools.thread.shard.stats.ShardExecutorStats;
import com.google.common.collect.Lists;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by @author liushouyun on 2023/1/29 1:58 下午.
 */
@Slf4j
public class ShardExecutorMetrics<T> extends ShardExecutorImpl<T> implements ShardExecutor<T> {
    private final ShardExecutorImpl<T> shardExecutor;
    private static final MeterRegistry REGISTRY = FsmdMeterRegistry.get();
    private static final String METRIC_PREFIX = "ShardExecutorMetrics.";


    public ShardExecutorMetrics(String threadName, int parallelism, int partition, int queueBufferSize,
                                Consumer<T> taskConsumer) {
        super(threadName, parallelism, partition, queueBufferSize, warp(threadName, taskConsumer));
        shardExecutor = this;

        metrics();


    }

    private <T> void metrics() {
        final List<Tag> tags = Lists.newArrayList(new Tag() {
            @Nonnull
            @Override
            public String getKey() {
                return "threadName";
            }

            @Nonnull
            @Override
            public String getValue() {
                return shardExecutor.getThreadName();
            }
        });
        final ShardExecutorStats stats = shardExecutor.stats();
        REGISTRY.gauge(METRIC_PREFIX + "executor.queueBufferSize"
                , tags, shardExecutor, s -> s.stats().getQueueBufferSize());
        REGISTRY.gauge(METRIC_PREFIX + "executor.parallelism"
                , tags, shardExecutor, s -> s.stats().getParallelism());
        REGISTRY.gauge(METRIC_PREFIX + "executor.partition"
                , tags, shardExecutor, s -> s.stats().getPartition());
        for (int i = 0; i < stats.getStats().size(); i++) {
            final ShardExecutorStats.KeyExecutorStats stat = stats.getStats().get(i);
            List<Tag> subTags = Lists.newArrayList(new Tag() {
                @Nonnull
                @Override
                public String getKey() {
                    return "threadName";
                }

                @Nonnull
                @Override
                public String getValue() {
                    return shardExecutor.getThreadName();
                }
            }, new Tag() {
                @Nonnull
                @Override
                public String getKey() {
                    return "subThreadName";
                }

                @Nonnull
                @Override
                public String getValue() {
                    return stat.getThreadName();
                }
            });
            int finalI = i;
            REGISTRY.gauge(METRIC_PREFIX + "executor.queue.remaining",
                    subTags, shardExecutor, s -> s.stats().getStats().get(finalI).getQueueRemainingCapacity());
            REGISTRY.gauge(METRIC_PREFIX + "executor.queue.size",
                    subTags, shardExecutor, s -> s.stats().getStats().get(finalI).getQueueSize());
            REGISTRY.gauge(METRIC_PREFIX + "executor.threadState",
                    subTags, shardExecutor, s -> s.stats().getStats().get(finalI).getThreadState().ordinal());
        }
    }

    private static <T> Consumer<T> warp(String threadName, Consumer<T> taskConsumer) {
        return new TimedConsumer<T>(taskConsumer, Lists.newArrayList(new Tag() {
            @Nonnull
            @Override
            public String getKey() {
                return "threadName";
            }

            @Nonnull
            @Override
            public String getValue() {
                return "shard-" + threadName;
            }
        }));
    }

    /**
     * A wrapper for a {@link Runnable} with idle and execution timings.
     */
    static class TimedConsumer<T> implements Consumer<T> {
        private final Timer executionTimer;
        private final Timer idleTimer;
        private final Consumer<T> command;
        private final Timer.Sample idleSample;

        TimedConsumer(Consumer<T> command, Iterable<Tag> tags) {
            this.executionTimer = REGISTRY.timer(METRIC_PREFIX + "executor", tags);
            this.idleTimer = REGISTRY.timer(METRIC_PREFIX + "executor.idle", tags);
            this.command = command;
            this.idleSample = Timer.start(REGISTRY);
        }

        @Override
        public void accept(T t) {
            idleSample.stop(idleTimer);
            Timer.Sample executionSample = Timer.start(REGISTRY);
            try {
                command.accept(t);
            } finally {
                executionSample.stop(executionTimer);
            }
        }
    }

}
