package com.fsdm.tools.ratelimiter.core.event.model;

import com.fsdm.tools.ratelimiter.repo.model.RateLimiterConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by @author fsdm on 2022/9/19 11:43 上午.
 */

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RlEvent {
    /**
     * 标记
     */
    private String tag;

    /**
     * 类型
     */
    private Type eventType;

    /**
     * 指标
     */
    private Quota quota;


    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Quota {
        /**
         * 单次使用许可证数量
         */
        private Integer numberOfPermits;

        /**
         * 可用许可证数量
         */
        private Integer availablePermissions;

        /**
         * 等待获取许可证数量
         */
        private Integer waitingPermissions;

        /**
         * 配置
         */
        private RateLimiterConfig config;
    }


    public enum Type {
        /**
         * 失败
         */
        FAILED_ACQUIRE,
        /**
         * 成功
         */
        SUCCESSFUL_ACQUIRE,
    }
}
