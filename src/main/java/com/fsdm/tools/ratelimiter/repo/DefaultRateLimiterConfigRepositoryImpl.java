package com.fsdm.tools.ratelimiter.repo;


import com.fsdm.tools.ratelimiter.model.RateLimiterTag;
import com.fsdm.tools.ratelimiter.repo.model.RateLimiterConfig;

import java.time.Duration;

/**
 * Created by @author fsdm on 2022/9/16 3:26 下午.
 */
public class DefaultRateLimiterConfigRepositoryImpl implements RateLimiterConfigRepository {
    @Override
    public RateLimiterConfig getRateLimiterConfig(RateLimiterTag tag) {
        return RateLimiterConfig.builder()
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .limitForPeriod(1000)
                .timeoutDuration(Duration.ofMillis(500))
                .build();
    }
}
