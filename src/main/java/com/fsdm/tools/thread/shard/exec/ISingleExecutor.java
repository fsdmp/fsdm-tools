package com.fsdm.tools.thread.shard.exec;


import com.fsdm.tools.thread.shard.stats.ShardExecutorStats;

/**
 * Created by @author fsdm on 2023/1/29 11:54 上午.
 */
public interface ISingleExecutor<T> extends Runnable {

    /**
     * 添加数据
     *
     * @param data 任务数据对象
     * @throws InterruptedException InterruptedException
     */
    void put(T data) throws InterruptedException;

    /**
     * 停止put
     */
    void stopPut();

    /**
     * 停止处理
     */
    void stop();


    /**
     * 执行方法
     *
     * @param data 数据
     */
    void exec(T data);

    /**
     * 统计数据
     *
     * @return 统计数据
     */
    ShardExecutorStats.SingleExecutorStats stats();
}
