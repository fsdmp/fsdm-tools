package com.fsdm.tools.ratelimiter.core.exec;


import com.fsdm.tools.ratelimiter.exception.ExecNotPermittedException;
import com.fsdm.tools.ratelimiter.repo.model.RateLimiterConfig;

import java.util.concurrent.Callable;

/**
 * Created by @author fsdm on 2022/9/16 3:47 下午.
 */
public interface RateLimiterExec {
    /**
     * 执行
     *
     * @param tag               标记
     * @param rateLimiterConfig 配置
     * @param <T>               调用返回
     * @param callable          可执行方法
     * @return 调用返回
     * @throws ExecNotPermittedException 没有执行权限（触发限流）
     * @throws Exception                 其他异常
     */
    <T> T exec(String tag, RateLimiterConfig rateLimiterConfig, Callable<T> callable)
            throws ExecNotPermittedException, Exception;
}
