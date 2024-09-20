package com.fsdm.tools.ratelimiter.repo;


import com.fsdm.tools.ratelimiter.model.RateLimiterTag;
import com.fsdm.tools.ratelimiter.repo.model.RateLimiterConfig;

/**
 * Created by @author fsdm on 2022/9/16 3:24 下午.
 */
public interface RateLimiterConfigRepository {
    /**
     * 获取限流配置
     *
     * @param tag 标记
     * @return 配置
     */
    RateLimiterConfig getRateLimiterConfig(RateLimiterTag tag);
}
