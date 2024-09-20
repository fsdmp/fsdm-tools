package com.fsdm.tools.thread.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.RunnableWrapper;

import java.util.concurrent.*;

/**
 * Created by @author liushouyun on 2022/3/24 2:58 下午.
 */
@Slf4j
public class FsdmThreadPoolExecutor extends ThreadPoolExecutor {

    public FsdmThreadPoolExecutor(int corePoolSize,
                                  int maximumPoolSize,
                                  long keepAliveTime,
                                  TimeUnit unit,
                                  BlockingQueue<Runnable> workQueue,
                                  ThreadFactory threadFactory,
                                  RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);

    }

    @Deprecated
    @Override
    public void execute(Runnable command) {
        Runnable task = RunnableWrapper.of(() -> {
            try {
                command.run();
            } catch (Exception e) {
                log.error("fsdm thread pool task execute fail", e);
                throw e;
            }
        });
        super.execute(task);
    }
}
