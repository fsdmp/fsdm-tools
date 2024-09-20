package com.fsdm.tools.ratelimiter;

import com.fsdm.tools.ratelimiter.core.exec.RateLimiterExec;
import com.fsdm.tools.ratelimiter.core.resilience4j.exec.Resilience4jRateLimiterExec;
import com.fsdm.tools.ratelimiter.exception.ExecNotPermittedException;
import com.fsdm.tools.ratelimiter.model.RateLimiterTag;
import com.fsdm.tools.ratelimiter.repo.DefaultRateLimiterConfigRepositoryImpl;
import com.fsdm.tools.ratelimiter.repo.RateLimiterConfigRepository;
import com.fsdm.tools.ratelimiter.repo.model.RateLimiterConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.concurrent.Callable;

/**
 * Created by @author fsdm on 2022/9/16 3:31 下午.
 * <p>
 * 限流工具类，
 * 被限流后会抛出 {@link ExecNotPermittedException} 异常
 */

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class RateLimiterUtil {
    private static final RateLimiterConfigRepository DEFAULT_REPO = new DefaultRateLimiterConfigRepositoryImpl();
    private static final RateLimiterExec DEFAULT_EXEC = new Resilience4jRateLimiterExec();


    private RateLimiterExec exec = DEFAULT_EXEC;
    private RateLimiterConfigRepository rateLimiterConfigRepository = DEFAULT_REPO;

    public RateLimiterUtil(RateLimiterExec exec) {
        this.exec = exec;
    }

    public <T> T exec(RateLimiterTag rateLimiterTag, Callable<T> callable)
            throws ExecNotPermittedException, Exception {
        final RateLimiterConfig rateLimiterConfig =
                this.getRateLimiterConfigRepository().getRateLimiterConfig(rateLimiterTag);
        return RateLimiterProxy.exec(this.getExec(), rateLimiterTag, rateLimiterConfig, callable);
    }
}
