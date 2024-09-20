package com.fsdm.tools.ratelimiter.annotation.aop.holder;

import com.fsdm.tools.ratelimiter.repo.RateLimiterConfigRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by @author fsdm on 2022/9/16 4:32 下午.
 */
public class RateLimiterConfigRepoHolder {
    private final static Map<Class<? extends RateLimiterConfigRepository>, RateLimiterConfigRepository> REPO_MAP =
            new ConcurrentHashMap<>(16);

    public static RateLimiterConfigRepository get(Class<? extends RateLimiterConfigRepository> clazz) throws IllegalAccessException, InstantiationException {
        if (REPO_MAP.containsKey(clazz)) {
            return REPO_MAP.get(clazz);
        }
        synchronized (clazz) {
            if (REPO_MAP.containsKey(clazz)) {
                return REPO_MAP.get(clazz);
            }
            final RateLimiterConfigRepository repo = clazz.newInstance();
            REPO_MAP.put(clazz, repo);
            return repo;

        }
    }

}
