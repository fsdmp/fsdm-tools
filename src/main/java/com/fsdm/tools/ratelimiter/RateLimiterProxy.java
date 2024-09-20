package com.fsdm.tools.ratelimiter;


import com.fsdm.tools.ratelimiter.core.exec.RateLimiterExec;
import com.fsdm.tools.ratelimiter.metrics.RlMetrics;
import com.fsdm.tools.ratelimiter.exception.ExecNotPermittedException;
import com.fsdm.tools.ratelimiter.model.RateLimiterTag;
import com.fsdm.tools.ratelimiter.repo.model.RateLimiterConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

/**
 * Created by @author fsdm on 2022/9/16 11:46 下午.
 */
@Slf4j
public class RateLimiterProxy {
    static {
        RlMetrics.open();
    }

    public static <T> T exec(RateLimiterExec exec, RateLimiterTag rateLimiterTag, RateLimiterConfig rateLimiterConfig,
                             Callable<T> callable)
            throws ExecNotPermittedException, Exception {
        try {
            return exec.exec(rateLimiterTag.getTotalTag(), rateLimiterConfig, callable);
        } catch (ExecNotPermittedException e) {
            if (rateLimiterConfig.getIsInterdict()) {
                throw e;
            }
            log.error("tag:{} the trigger current limiting (but the business normal execution) rateLimiterConfig:{}",
                    rateLimiterTag.getTotalTag(), rateLimiterConfig, e);
            return callable.call();
        }
    }
}
