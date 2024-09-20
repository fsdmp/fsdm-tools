package com.fsdm.tools.thread.shard;

import com.fsdm.tools.thread.shard.metrics.ShardExecutorMetrics;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Created by @author fsdm on 2023/1/29 2:12 下午.
 */
@Slf4j
public class ShardExecutorFactory {
    private static final String NAME_PRE = "fsdm-tpf";


    /**
     * 执行器map
     */
    private static final Map<String, ShardExecutor<?>> POOL_MAP = new ConcurrentHashMap<>();


    /**
     * 产生一个新的执行器
     *
     * @param name 线程名
     * @return 新的线程池
     */
    public static <T> ShardExecutor<T> getExecutor(String name, Consumer<T> taskConsumer) {
        return getExecutor(name, 10, taskConsumer);
    }

    /**
     * 产生一个新的执行器
     *
     * @param name 线程名
     * @return 新的线程池
     */
    public static <T> ShardExecutor<T> getExecutor(String name, int parallelism, Consumer<T> taskConsumer) {
        return getExecutor(name, parallelism, parallelism * 2, taskConsumer);
    }


    /**
     * 产生一个新的执行器
     *
     * @param name 线程名
     * @return 新的线程池
     */
    public static <T> ShardExecutor<T> getExecutor(String name, int parallelism, int partition,
                                                   Consumer<T> taskConsumer) {
        return getExecutor(name, parallelism, partition, 64, taskConsumer);
    }

    /**
     * 产生一个新的执行器
     *
     * @param name 线程名
     * @return 新的线程池
     */
    public static <T> ShardExecutor<T> getExecutor(String name, int parallelism, int partition,
                                                   int queueBufferSize, Consumer<T> taskConsumer) {
        name = NAME_PRE + "-" + name;
        if (POOL_MAP.containsKey(name)) {
            return (ShardExecutor<T>) POOL_MAP.get(name);
        }
        synchronized (POOL_MAP) {
            if (POOL_MAP.containsKey(name)) {
                return (ShardExecutor<T>) POOL_MAP.get(name);
            }
            final ShardExecutor<T> shardExecutor =
                    new ShardExecutorMetrics<>(name, parallelism, partition, queueBufferSize, taskConsumer);
            log.info("create a name:{} shardExecutor ！", name);
            POOL_MAP.put(name, shardExecutor);
            return shardExecutor;
        }
    }

    public static List<ShardExecutor<?>> getExecutors() {
        return Collections.unmodifiableList(new ArrayList<>(POOL_MAP.values()));
    }

    static {
        Class<ShardExecutorMetrics> clazz = ShardExecutorMetrics.class;
    }
}
