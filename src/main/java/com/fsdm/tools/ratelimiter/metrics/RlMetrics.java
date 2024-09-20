package com.fsdm.tools.ratelimiter.metrics;

import com.fsdm.tools.metrics.FsdmMeterRegistry;
import com.fsdm.tools.ratelimiter.core.event.RlEventPublisher;
import com.fsdm.tools.ratelimiter.core.event.model.RlEvent;
import com.fsdm.tools.ratelimiter.model.Constants;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by @author fsdm on 2022/9/19 12:17 下午.
 */
@Slf4j
public class RlMetrics {
    private static final String KEY = "rateLimiterMetrics";
    private static final Map<String, RlEvent> MAP = new ConcurrentHashMap<>();

    public static void open() {
        log.info("{} start...", KEY);
        RlEventPublisher.addEvent(event -> {
            MAP.put(event.getTag(), event);
            final Map<String, String> kv = getKv(event);
            monitor(event, kv);
        });
    }

    private static Map<String, String> getKv(RlEvent event) {
        final String[] tagSpit = event.getTag().split(Constants.TAG_SEPARATOR);
        return new HashMap<String, String>(8) {
            {
                put("tag", tagSpit[0]);
                put("eventType", event.getEventType().name());
            }
        };
    }

    private static void monitor(RlEvent event, Map<String, String> kv) {
        if (log.isDebugEnabled()) {
            log.info("{} -> kv:{} event:{}", KEY, kv, event);
        }
        MeterRegistry registry = FsdmMeterRegistry.get();
        List<Tag> tagList = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, String> entry : kv.entrySet()) {
            tagList.add(new ImmutableTag(entry.getKey(), entry.getValue()));
        }

        registry.counter(KEY, tagList);
        registry.gauge(KEY + "-" + "numberOfPermits", tagList, event,
                input -> MAP.get(event.getTag()).getQuota().getNumberOfPermits());
        registry.gauge(KEY + "-" + "availablePermissions", tagList, event,
                input -> MAP.get(event.getTag()).getQuota().getAvailablePermissions());
        registry.gauge(KEY + "-" + "waitingPermissions", tagList, event,
                input -> MAP.get(event.getTag()).getQuota().getWaitingPermissions());
        registry.gauge(KEY + "-" + "limitForPeriod", tagList, event,
                input -> MAP.get(event.getTag()).getQuota().getConfig().getLimitForPeriod());
        registry.gauge(KEY + "-" + "limitRefreshPeriod", tagList, event,
                input -> MAP.get(event.getTag()).getQuota().getConfig().getLimitRefreshPeriod().toMillis());
        registry.gauge(KEY + "-" + "timeoutDuration", tagList, event,
                input -> MAP.get(event.getTag()).getQuota().getConfig().getTimeoutDuration().toMillis());
    }
}
