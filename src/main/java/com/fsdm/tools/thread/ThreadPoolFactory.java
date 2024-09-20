package com.fsdm.tools.thread;

import com.fsdm.tools.thread.core.FsdmThreadPoolExecutor;
import com.fsdm.tools.thread.metrics.FsmdMeterRegistry;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by @author liushouyun on 2021/8/21 6:42 下午.
 * <p>
 * 线程池工厂
 */
@Slf4j
public class ThreadPoolFactory {
    private static final String NAME_PRE = "fsdm-tpf";

    /**
     * 线程池map
     */
    private static final Map<String, ExecutorService> POOL_MAP = new ConcurrentHashMap<>();


    static {
        // 定时监测所有线程池信息
        //  timingDetectionThreadInfo();
    }


    public static ExecutorService getThreadPool(String name) {
        return getThreadPool(name, new ThreadPoolExecutor.CallerRunsPolicy() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                log.warn("fsdm thread:{} main exec", name);
                super.rejectedExecution(r, e);
            }
        });
    }

    /**
     * 产生一个新的线程池
     *
     * @param name 线程名
     * @return 新的线程池
     */
    public static ExecutorService getThreadPool(String name, RejectedExecutionHandler rejectedExecutionHandler) {
        return getThreadPool(name, 20, rejectedExecutionHandler);
    }

    /**
     * 产生一个新的线程池
     *
     * @param name 线程名
     * @return 新的线程池
     */
    public static ExecutorService getThreadPool(String name, int corePoolSize,
                                                RejectedExecutionHandler rejectedExecutionHandler) {
        return getThreadPool(name, corePoolSize, 100, 30L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1024), rejectedExecutionHandler);
    }

    /**
     * 产生一个新的线程池
     *
     * @param name 线程名
     * @return 新的线程池
     */
    public static ExecutorService getThreadPool(String name,
                                                int corePoolSize,
                                                int maximumPoolSize,
                                                long keepAliveTime,
                                                TimeUnit unit,
                                                BlockingQueue<Runnable> workQueue,
                                                RejectedExecutionHandler handler) {
        name = NAME_PRE + "-" + name;
        if (POOL_MAP.containsKey(name)) {
            return POOL_MAP.get(name);
        }
        synchronized (POOL_MAP) {
            if (POOL_MAP.containsKey(name)) {
                return POOL_MAP.get(name);
            }
            ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(name + "-%d").build();
            ThreadPoolExecutor threadPoolExecutor = new FsdmThreadPoolExecutor(corePoolSize, maximumPoolSize,
                    keepAliveTime, unit,
                    workQueue, threadFactory, handler);
            ExecutorService executorService =
                    ExecutorServiceMetrics.monitor(FsmdMeterRegistry.get(), threadPoolExecutor, name);
            threadPoolExecutor.prestartCoreThread();
            log.info("create a name:{} thread pool ！", name);
            POOL_MAP.put(name, executorService);
            return executorService;
        }
    }
}
