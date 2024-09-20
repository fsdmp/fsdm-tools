package com.fsdm.tools.thread;

import com.google.common.collect.Lists;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by @author fsdm on 2024/9/3 7:08 下午.
 */
public class ThreadPoolUtil {

    /**
     * 获取一个线程池
     *
     * @param name 线程池名
     * @return 线程池
     */
    public static ExecutorService get(String name) {
        return ThreadPoolFactory.getThreadPool(name);
    }

    /**
     * 线程池并行执行，不阻塞执行（单个）
     *
     * @param executor 线程池
     * @param runnable 执行任务
     */
    public static void execSingle(ExecutorService executor, Runnable runnable) {
        exec(executor, Lists.newArrayList(runnable), null, Runnable::run);
    }


    /**
     * 线程池并行执行，不阻塞执行(批量)
     *
     * @param executor  线程池
     * @param dataList  原始数据集合
     * @param predicate 断言
     * @param consumer  单任务处理
     * @param <D>       原始数据类型
     */
    public static <D> void exec(ExecutorService executor, List<D> dataList,
                                Predicate<D> predicate, Consumer<D> consumer) {
        for (D d : dataList) {
            if (null != predicate && !predicate.test(d)) {
                continue;
            }
            executor.execute(() -> consumer.accept(d));
        }
    }


    /**
     * 线程池并行执行，阻塞执行，拿到结果
     *
     * @param executor    线程池
     * @param parallelism 期望并行度（可以理解为将集合切成几个任务集）
     * @param dataList    原始数据集合
     * @param predicate   断言
     * @param function    单任务处理与元素转换方法
     * @param <D>         原始数据类型
     * @param <T>         目标数据类型
     * @return 目标数据集合
     */
    public static <D, T> List<T> execGet(ExecutorService executor, int parallelism, List<D> dataList,
                                         Predicate<D> predicate, Function<D, T> function) {
        parallelism = Math.min(parallelism, dataList.size());
        ParallelFlux<D> parallelFlux = Flux.fromIterable(dataList)
                .parallel(parallelism)
                .runOn(Schedulers.fromExecutor(executor));
        if (null != predicate) {
            parallelFlux = parallelFlux.filter(predicate);
        }
        return parallelFlux
                .map(function)
                .sequential()
                .collectList()
                .block();
    }
}
