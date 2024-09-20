package com.fsdm.tools.ratelimiter.annotation.aop.holder;


import com.fsdm.tools.ratelimiter.core.exec.RateLimiterExec;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by @author fsdm on 2022/9/16 4:32 下午.
 */
public class RateLimiterExecRepoHolder {
    private final static Map<Class<? extends RateLimiterExec>, RateLimiterExec> MAP =
            new ConcurrentHashMap<>(16);

    public static RateLimiterExec get(Class<? extends RateLimiterExec> clazz)
            throws IllegalAccessException, InstantiationException {
        if (MAP.containsKey(clazz)) {
            return MAP.get(clazz);
        }
        synchronized (clazz) {
            if (MAP.containsKey(clazz)) {
                return MAP.get(clazz);
            }
            final RateLimiterExec exec = clazz.newInstance();
            MAP.put(clazz, exec);
            return exec;

        }
    }

}
