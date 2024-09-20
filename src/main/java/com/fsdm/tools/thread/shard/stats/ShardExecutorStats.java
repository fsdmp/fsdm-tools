package com.fsdm.tools.thread.shard.stats;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Created by @author fsdm on 2023/1/29 11:46 上午.
 */
@AllArgsConstructor
@Getter
public class ShardExecutorStats {
    private final String threadName;
    private final int parallelism;
    private final int queueBufferSize;
    private final int partition;
    private final List<KeyExecutorStats> stats;


    public List<SingleExecutorStats> getThreadPoolStats() {
        return Collections.unmodifiableList(stats);
    }

    @Override
    public String toString() {
        return toStringHelper(this).add("stats", stats).toString();
    }

    @Getter
    public static class KeyExecutorStats extends SingleExecutorStats {

        public KeyExecutorStats(SingleExecutorStats stats) {
            super(stats.getThreadName(), stats.getThreadState(),
                    stats.getQueueSize(), stats.getQueueRemainingCapacity());

        }

        @Override
        public String toString() {
            return toStringHelper(this)
                    .add("threadName", getThreadName())
                    .add("threadState", getThreadState())
                    .add("queueSize", getQueueSize())
                    .add("queueRemainingCapacity", getQueueRemainingCapacity())
                    .toString();
        }
    }

    @Getter
    public static class SingleExecutorStats {

        private final String threadName;
        private final Thread.State threadState;
        private final int queueSize;
        private final int queueRemainingCapacity;

        public SingleExecutorStats(String threadName, Thread.State threadState, int queueSize,
                                   int queueRemainingCapacity) {
            this.threadName = threadName;
            this.threadState = threadState;
            this.queueSize = queueSize;
            this.queueRemainingCapacity = queueRemainingCapacity;
        }


        @Override
        public String toString() {
            return toStringHelper(this)
                    .add("threadName", threadName)
                    .add("threadState", threadState)
                    .add("queueSize", queueSize)
                    .add("queueRemainingCapacity", queueRemainingCapacity)
                    .toString();
        }
    }
}
