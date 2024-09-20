package com.fsdm.tools.thread.shard.exec;

import com.fsdm.tools.thread.core.FsdmThread;
import com.fsdm.tools.thread.shard.stats.ShardExecutorStats;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;


/**
 * Created by @author fsdm on 2023/1/29 10:36 上午.
 */

@Slf4j
public class SingleExecutor<T> implements ISingleExecutor<T> {
    private final String name;
    private final Integer index;
    private final BlockingQueue<T> queue;
    private final Consumer<T> consumer;
    private final Thread thread;

    volatile boolean isStopPut;
    volatile boolean isStop;


    public SingleExecutor(String name, Integer index, BlockingQueue<T> queue, Consumer<T> consumer) {
        this.name = name + "-" + index;
        this.index = index;
        this.queue = queue;
        this.consumer = consumer;
        this.isStopPut = false;
        this.isStop = false;
        thread = new FsdmThread(this, name);
        thread.start();
    }

    @Override
    public void put(T data) throws InterruptedException {
        if (isStopPut) {
            log.error("SingleExecutor stop put name:{} data:{}", name, data);
            throw new InterruptedException(name + "停止写入");
        }
        queue.put(data);
    }

    @Override
    public void stopPut() {
        isStopPut = true;
    }

    @Override
    public void stop() {
        isStop = true;
    }

    @Override
    public ShardExecutorStats.SingleExecutorStats stats() {
        return new ShardExecutorStats.SingleExecutorStats(
                name, thread.getState(), queue.size(), queue.remainingCapacity()
        );
    }


    @Override
    public void run() {
        for (; ; ) {
            try {
                final T data = queue.take();
                exec(data);
            } catch (InterruptedException e) {
                log.error("SingleExecutor processed err Interrupted name:{}", name, e);
            } catch (Exception e) {
                log.error("SingleExecutor processed err name:{}", name, e);
            }
            if (isStop) {
                if (queue.size() > 0) {
                    log.error("SingleExecutor not processed name:{} data:{}", name, queue);
                }
                break;
            }
        }
    }

    @Override
    public void exec(T data) {
        consumer.accept(data);
    }
}
