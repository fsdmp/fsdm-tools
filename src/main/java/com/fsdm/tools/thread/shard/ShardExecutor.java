package com.fsdm.tools.thread.shard;


import com.fsdm.tools.thread.shard.stats.ShardExecutorStats;

import javax.annotation.Nonnull;


/**
 * Created by @author fsdm on 2023/1/19 5:21 下午.
 */
public interface ShardExecutor<T> {
    /**
     * 提交执行一个任务
     *
     * @param key      任务对应的Key
     * @param taskData 任务数据对象
     * @throws InterruptedException InterruptedException
     */
    void execute(@Nonnull String key, @Nonnull T taskData) throws InterruptedException;

    /**
     * 获取统计数据
     *
     * @return 统计数据
     */
    ShardExecutorStats stats();
}
