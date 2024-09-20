/*
 * Copyright (C) 2019 Baidu, Inc. All Rights Reserved.
 */
package com.fsdm.tools.enums;

/**
 * Created by @author liushouyun on 2019/4/16 11:05 下午.
 * <p>
 * 枚举抽象接口
 */
public interface AbstractEnum {
    /**
     * valueOf
     *
     * @param enumType 枚举类型
     * @param value    value
     * @param <T>      枚举类
     * @return 枚举类
     */
    static <T extends AbstractEnum> T valueOf(Class<T> enumType, int value) {
        for (T t : enumType.getEnumConstants()) {
            if (t.getValue() == value) {
                return t;
            }
        }
        throw new IllegalArgumentException("No enum constant " + enumType.getCanonicalName() + "." + value);
    }

    /**
     * 获取value
     *
     * @return value
     */
    int getValue();
}
