package com.fsdm.tools.ratelimiter.annotation;


import java.lang.annotation.*;

/**
 * Created by @author fsdm on 2022/9/16 2:17 下午.
 * <p>
 * 基于spring环境的限流注解，多纬度限流
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiters {
    /**
     * 限流配置集
     * note：数组顺序决定优先级
     *
     * @return 限流配置集
     */
    RateLimiter[] value();
}
