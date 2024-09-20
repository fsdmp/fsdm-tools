package com.fsdm.tools.ratelimiter.annotation;


import com.fsdm.tools.ratelimiter.core.exec.RateLimiterExec;
import com.fsdm.tools.ratelimiter.core.resilience4j.exec.Resilience4jRateLimiterExec;
import com.fsdm.tools.ratelimiter.repo.DefaultRateLimiterConfigRepositoryImpl;
import com.fsdm.tools.ratelimiter.repo.RateLimiterConfigRepository;

import java.lang.annotation.*;

/**
 * Created by @author fsdm on 2022/9/16 2:17 下午.
 * <p>
 * 基于spring环境的限流注解，
 * 被限流后会抛出 {@link ExecNotPermittedException} 异常
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {

    /**
     * 限流标识
     *
     * @return 限流标识
     */
    String value();


    /**
     * 额外标签的参数列表
     * 可支持表达式
     *
     * @return 额外标签的参数列表
     */
    String[] extraTags() default {};

    /**
     * 限流配置资源库(内部会单例使用)
     * 限流组件会维护该类单例对象，
     * 如需动态配置，实现 {@link RateLimiterConfigRepository#getRateLimiterConfig()} 方法时，可以进行动态配置变更
     *
     * @return 限流配置资源库
     */
    Class<? extends RateLimiterConfigRepository> config() default DefaultRateLimiterConfigRepositoryImpl.class;


    /**
     * 限流执行器(内部会单例使用)
     *
     * @return 限流执行器
     */
    Class<? extends RateLimiterExec> exec() default Resilience4jRateLimiterExec.class;
}
