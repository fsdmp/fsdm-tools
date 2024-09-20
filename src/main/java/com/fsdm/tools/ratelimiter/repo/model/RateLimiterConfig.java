package com.fsdm.tools.ratelimiter.repo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

/**
 * Created by @author fsdm on 2022/9/16 2:20 下午.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RateLimiterConfig {

    private Duration timeoutDuration;

    private Duration limitRefreshPeriod;

    private Integer limitForPeriod;
    /**
     * 是否进行阻断
     * true为阻断触发限流不执行业务流程，
     * false为不阻断，触发限流打印日志业务流程照常执行
     */
    private Boolean isInterdict;


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RateLimiterConfig)) {
            return false;
        }
        RateLimiterConfig other = (RateLimiterConfig) obj;
        return this.getTimeoutDuration().equals(other.getTimeoutDuration())
                && this.getLimitRefreshPeriod().equals(other.getLimitRefreshPeriod())
                && this.getLimitForPeriod().equals(other.getLimitForPeriod());
    }

    @Override
    public int hashCode() {
        return this.getTimeoutDuration().hashCode()
                + this.getLimitRefreshPeriod().hashCode()
                + this.getLimitForPeriod().hashCode();
    }

    public Boolean getIsInterdict() {
        // 默认为true
        return null == isInterdict || isInterdict;
    }
}
