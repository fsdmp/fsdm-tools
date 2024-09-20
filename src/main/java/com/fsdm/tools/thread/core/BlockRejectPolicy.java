package com.fsdm.tools.thread.core;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by @author fsdm on 2022/11/2 11:48 上午.
 */
@Slf4j
public class BlockRejectPolicy implements RejectedExecutionHandler {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        if (executor.isShutdown()) {
            return;
        }
        try {
            executor.getQueue().put(r);
        } catch (InterruptedException e) {
            log.error("rejectedExecution", e);
            Thread.currentThread().interrupt();
        }
    }

}
