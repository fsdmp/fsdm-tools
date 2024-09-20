package com.fsdm.tools.ratelimiter.core.resilience4j.exec;

import com.fsdm.tools.ratelimiter.core.exec.RateLimiterExec;
import com.fsdm.tools.ratelimiter.core.resilience4j.holder.Resilience4jRateLimiterHolder;
import com.fsdm.tools.ratelimiter.exception.ExecNotPermittedException;
import com.fsdm.tools.ratelimiter.repo.model.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

/**
 * Created by @author fsdm on 2022/9/16 3:51 下午.
 */
@Slf4j
public class Resilience4jRateLimiterExec implements RateLimiterExec {

    @Override
    public <T> T exec(String tag, RateLimiterConfig rateLimiterConfig, Callable<T> callable) throws ExecNotPermittedException, Exception {
        final RateLimiter rateLimiter = Resilience4jRateLimiterHolder.get(tag, rateLimiterConfig);

        try {
            return rateLimiter.executeCallable(callable);
        } catch (RequestNotPermitted e) {
            throw new ExecNotPermittedException(e.getMessage(), e);
        }
    }
}
