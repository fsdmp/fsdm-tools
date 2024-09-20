package com.fsdm.tools.ratelimiter.core.resilience4j.holder;

import com.fsdm.tools.ratelimiter.core.event.RlEventPublisher;
import com.fsdm.tools.ratelimiter.core.event.model.RlEvent;
import com.fsdm.tools.ratelimiter.repo.model.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.event.AbstractRateLimiterEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by @author fsdm on 2022/9/16 3:51 下午.
 */
@Slf4j
public class Resilience4jRateLimiterHolder {
    private final static Map<String, RateLimiterConfig> CONFIG_MAP = new ConcurrentHashMap<>(16);
    private final static Map<String, RateLimiter> BEAN_MAP = new ConcurrentHashMap<>(16);


    public static RateLimiter get(String tag, RateLimiterConfig newConfig) {
        synchronized (tag.intern()) {
            final RateLimiterConfig config = CONFIG_MAP.get(tag);
            // 如果不存在配置或者配置不同重新装载实例
            if (null == config || !config.equals(newConfig)) {
                CONFIG_MAP.put(tag, newConfig);
                BEAN_MAP.remove(tag);
                log.info("rl config change~ tag:{} oldConfig:{} newConfig:{}", tag, config, newConfig);
            }
            return getRateLimiter(tag, newConfig);
        }
    }


    public static List<RateLimiter> getAll() {
        return new ArrayList<>(BEAN_MAP.values());
    }


    private static RateLimiter getRateLimiter(String tag, RateLimiterConfig config) {
        if (BEAN_MAP.containsKey(tag)) {
            return BEAN_MAP.get(tag);
        }
        synchronized (tag.intern()) {
            if (BEAN_MAP.containsKey(tag)) {
                return BEAN_MAP.get(tag);
            }
            final RateLimiter rateLimiter = newRateLimiter(tag, config);
            registerEvent(rateLimiter);
            BEAN_MAP.put(tag, rateLimiter);
            return rateLimiter;
        }
    }

    private static void registerEvent(RateLimiter rateLimiter) {
        final RateLimiter.EventPublisher eventPublisher = rateLimiter.getEventPublisher();
        eventPublisher.onFailure(event -> trigger(rateLimiter, event, RlEvent.Type.FAILED_ACQUIRE));
        eventPublisher.onSuccess(event -> trigger(rateLimiter, event, RlEvent.Type.SUCCESSFUL_ACQUIRE));
    }

    private static void trigger(RateLimiter rateLimiter, AbstractRateLimiterEvent event, RlEvent.Type eventType) {
        final RateLimiter.Metrics metrics = rateLimiter.getMetrics();
        final int numberOfPermits = event.getNumberOfPermits();
        final int availablePermissions = metrics.getAvailablePermissions();
        final int numberOfWaitingThreads = metrics.getNumberOfWaitingThreads();
        final RlEvent rlEvent = RlEvent.builder()
                .tag(event.getRateLimiterName())
                .eventType(eventType)
                .quota(RlEvent.Quota.builder()
                        .config(CONFIG_MAP.get(event.getRateLimiterName()))
                        .numberOfPermits(numberOfPermits)
                        .availablePermissions(availablePermissions)
                        .waitingPermissions(numberOfWaitingThreads)
                        .build())
                .build();
        RlEventPublisher.trigger(rlEvent);
    }

    private static RateLimiter newRateLimiter(String tag, RateLimiterConfig config) {
        io.github.resilience4j.ratelimiter.RateLimiterConfig rateLimiterConfig =
                io.github.resilience4j.ratelimiter.RateLimiterConfig.custom()
                        .limitRefreshPeriod(config.getLimitRefreshPeriod())
                        .limitForPeriod(config.getLimitForPeriod())
                        .timeoutDuration(config.getTimeoutDuration())
                        .build();
        // Create registry
        RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.of(rateLimiterConfig);
        // Use registry
        return rateLimiterRegistry.rateLimiter(tag);
    }
}
