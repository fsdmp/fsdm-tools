package com.fsdm.tools.thread.shard.impl;


import com.fsdm.tools.thread.shard.ShardExecutor;
import com.fsdm.tools.thread.shard.config.ShardExecutorConfig;
import com.fsdm.tools.thread.shard.exec.ISingleExecutor;
import com.fsdm.tools.thread.shard.exec.SingleExecutor;
import com.fsdm.tools.thread.shard.stats.ShardExecutorStats;
import com.fsdm.tools.util.Crc32Utils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Comparator.comparingInt;
import static java.util.concurrent.TimeUnit.SECONDS;


/**
 * Created by @author liushouyun on 2023/1/19 5:21 下午.
 */
@Getter
@Slf4j
public class ShardExecutorImpl<T> implements ShardExecutor<T> {


    @Override
    public void execute(@Nonnull String key, @Nonnull T taskData) throws InterruptedException {
        String newKey = String.valueOf(Crc32Utils.crc32(key) % partition);
        this.select(newKey).put(taskData);
    }

    @Override
    public ShardExecutorStats stats() {
        final List<ShardExecutorStats.KeyExecutorStats> list = this.getExecutors().stream()
                .map(entry ->
                        new ShardExecutorStats.KeyExecutorStats(entry.stats())
                ).collect(Collectors.toList());

        return new ShardExecutorStats(
                this.getThreadName(), this.getParallelism(), this.getQueueBufferSize(), this.getPartition(), list
        );
    }

    private final String threadName;
    private final int parallelism;
    private final int queueBufferSize;
    private final int partition;
    private final Consumer<T> taskConsumer;
    private final List<ISingleExecutor<T>> executors;
    private final Map<String, ISingleExecutor<T>> mapping = new ConcurrentHashMap<>();

    public ShardExecutorImpl(String threadName, int parallelism, int partition, int queueBufferSize,
                             Consumer<T> taskConsumer) {
        this.threadName = "shard-" + threadName;
        this.parallelism = parallelism;
        this.queueBufferSize = queueBufferSize;
        this.partition = partition;
        this.taskConsumer = taskConsumer;


        if (ShardExecutorConfig.autoClose()) {
            // ShutdownHook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    this.close();
                } catch (Exception e) {
                    log.error("ShardExecutorBuilder close err", e);
                }
            }));
        }

        // 初始化执行期集合
        executors = IntStream.range(0, parallelism)
                .mapToObj(i ->
                        new SingleExecutor<>(
                                this.threadName, i, new LinkedBlockingQueue<>(queueBufferSize), taskConsumer
                        )
                ).collect(Collectors.toList());
    }


    public ISingleExecutor<T> select(String key) {
        // key不易过于分散，外部需要进行聚合，否则可能会内存溢出
        return mapping.computeIfAbsent(key, k -> {
            // 尽可能找出最闲的执行器
            final List<ISingleExecutor<T>> list = new ArrayList<>(executors);
            Collections.shuffle(list);
            return list.stream()
                    .min(comparingInt(e -> e.stats().getQueueSize()))
                    .orElseThrow(() -> new IllegalArgumentException(key + "select 失败"));
        });
    }

    public void close() throws Exception {
        int count = 0;
        int stopPutCount = 5;
        int stopCount = 10;
        while (executors.stream().anyMatch(it -> it.stats().getQueueSize() > 0)) {
            if (count == stopPutCount) {
                executors.forEach(ISingleExecutor::stopPut);
            }
            if (count == stopCount) {
                executors.forEach(ISingleExecutor::stop);
                break;
            }
            executors.wait(SECONDS.toMillis(1));
            count++;
        }
    }
}
