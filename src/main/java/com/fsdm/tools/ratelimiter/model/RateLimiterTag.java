package com.fsdm.tools.ratelimiter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by @author fsdm on 2022/9/16 11:47 下午.
 */

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RateLimiterTag {
    /**
     * tag
     */
    private String tag;

    /**
     * sub tags的kv接口
     */
    private List<SubTag> subTagList;

    public String getTotalTag() {
        if (CollectionUtils.isEmpty(this.getSubTagList())) {
            return this.getTag();
        }
        final List<String> subTags = buildSubTagList();
        return this.getTag() +
                Constants.TAG_SEPARATOR +
                String.join(Constants.SUBTAG_SEPARATOR, subTags);
    }

    private List<String> buildSubTagList() {
        return this.getSubTagList().stream()
                .map(kv -> kv.getKey() + Constants.SUBTAG_KV_SEPARATOR + kv.getValue())
                .collect(Collectors.toList());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubTag {
        private String key;

        private String value;

    }

}
