package com.fsdm.tools.ratelimiter.annotation.aop;

import com.fsdm.tools.ratelimiter.RateLimiterProxy;
import com.fsdm.tools.ratelimiter.annotation.RateLimiter;
import com.fsdm.tools.ratelimiter.annotation.RateLimiters;
import com.fsdm.tools.ratelimiter.annotation.aop.holder.RateLimiterConfigRepoHolder;
import com.fsdm.tools.ratelimiter.annotation.aop.holder.RateLimiterExecRepoHolder;
import com.fsdm.tools.ratelimiter.core.exec.RateLimiterExec;
import com.fsdm.tools.ratelimiter.model.RateLimiterTag;
import com.fsdm.tools.ratelimiter.repo.RateLimiterConfigRepository;
import com.fsdm.tools.ratelimiter.repo.model.RateLimiterConfig;
import com.fsdm.tools.util.FsdmJSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by @author fsdm on 2022/9/16 3:30 下午.
 * todo： 需要依托spring
 */
@Aspect
@Slf4j
// @Component
// @Order(Ordered.LOWEST_PRECEDENCE - 1000)
public class RateLimitersAspect {


    @Pointcut("@annotation(com.fsdm.tools.ratelimiter.annotation.RateLimiters)")
    public void pointCut() {
    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        RateLimiters rls = method.getAnnotation(RateLimiters.class);
        // TODO: 2022/9/16 多维度限流暂不支持，目前只支持单个纬度限流
        final RateLimiter rl = rls.value()[0];
        // 合成sub tag
        List<RateLimiterTag.SubTag> subTag = getSubTag(point, rl.extraTags());
        // 获取执行器
        final RateLimiterExec exec = RateLimiterExecRepoHolder.get(rl.exec());
        // 构建RateLimiterTag
        final String tag = rl.value();
        final RateLimiterTag build = RateLimiterTag.builder().tag(tag).subTagList(subTag).build();
        // 获取config
        final RateLimiterConfigRepository rateLimiterConfigRepository = RateLimiterConfigRepoHolder.get(rl.config());
        final RateLimiterConfig config = rateLimiterConfigRepository.getRateLimiterConfig(build);
        // 执行，被限流会抛出 {@link ExecNotPermittedException}
        return RateLimiterProxy.exec(exec, build, config,
                () -> {
                    try {
                        return point.proceed();
                    } catch (RuntimeException e) {
                        log.error("RateLimitersAspect proceed runtimeException error point:{} args:{}",
                                point.toLongString(), FsdmJSON.toJson(point.getArgs()), e);
                        throw e;
                    } catch (Throwable throwable) {
                        log.error("RateLimitersAspect proceed throwable error point:{} args:{}",
                                point.toLongString(), FsdmJSON.toJson(point.getArgs()), throwable);
                        throw new RuntimeException(throwable);
                    }
                }
        );
    }


    private List<RateLimiterTag.SubTag> getSubTag(ProceedingJoinPoint point, String[] extraTags) {
        // TODO: 2022/9/16 表达式解析
        return new ArrayList<>();
//        return Arrays.stream(extraTags)
//                .map(extraTag ->
//                        new RateLimiterTag.SubTag(
//                                extraTag.replace("#", ""),
//                                ExpressionEvaluator.getConditionValue(point, extraTag, String.class)
//                        )
//                ).collect(Collectors.toList());
    }

}
