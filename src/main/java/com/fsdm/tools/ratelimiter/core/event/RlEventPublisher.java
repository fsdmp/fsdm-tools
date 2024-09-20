package com.fsdm.tools.ratelimiter.core.event;


import com.fsdm.tools.ratelimiter.core.event.model.RlEvent;
import com.fsdm.tools.thread.ThreadPoolFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;


/**
 * Created by @author fsdm on 2022/9/19 11:29 上午.
 */
public class RlEventPublisher {
    private static final ExecutorService EXECUTOR_SERVICE = ThreadPoolFactory.getThreadPool("RlEventPublisher");
    private static final List<Consumer<RlEvent>> CONSUMER_LIST = new CopyOnWriteArrayList<>();
    private static final List<Consumer<RlEvent>> ASYNC_CONSUMER_LIST = new CopyOnWriteArrayList<>();

    /**
     * 添加事件
     *
     * @param consumerEvent 事件
     */
    public static void addEvent(Consumer<RlEvent> consumerEvent, boolean isAsync) {
        if (isAsync) {
            ASYNC_CONSUMER_LIST.add(consumerEvent);
        } else {
            CONSUMER_LIST.add(consumerEvent);
        }

    }


    /**
     * 添加事件
     *
     * @param consumerEvent 事件
     */
    public static void addEvent(Consumer<RlEvent> consumerEvent) {
        addEvent(consumerEvent, true);
    }


    /**
     * 触发事件
     *
     * @param event 事件
     */
    public static void trigger(RlEvent event) {
        for (Consumer<RlEvent> consumer : ASYNC_CONSUMER_LIST) {
            EXECUTOR_SERVICE.execute(() -> consumer.accept(event));
        }
        for (Consumer<RlEvent> consumer : CONSUMER_LIST) {
            consumer.accept(event);
        }
    }
}
