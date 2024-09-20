package com.fsdm.tools.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

/**
 * Created by @author fsdm on 2024/6/13 1:36 下午.
 */
@Slf4j
public class ClazzUtil {

    /**
     * map 转 obj
     *
     * @param map   map(嵌套类由.分割)
     * @param clazz 对象类型
     * @param <T>   对象类型
     * @return obj
     */
    public static <T> Optional<T> mapToObj(Map<String, Object> map, Class<T> clazz) {
        try {
            T obj = clazz.getDeclaredConstructor().newInstance();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                setFieldValue(obj, entry.getKey(), entry.getValue());
            }
            return Optional.of(obj);
        } catch (ReflectiveOperationException e) {
            log.error("mapToObj fail map:{} clazz:{}", map, clazz, e);
            return Optional.empty();
        }
    }


    /**
     * obj转map
     *
     * @param obj 对象
     * @param <T> 对象类型
     * @return key:字段名(嵌套类由.分割) value: 字段值
     */
    public static <T> Map<String, Optional<Object>> objToMap(T obj) {
        if (obj == null) {
            return Collections.emptyMap();
        }
        // 获取对象的类定义
        Class<?> clazz = obj.getClass();

        // 获取所有字段，包括私有字段
        final Map<Field, Optional<Object>> clazzFields = getObjFieldKv(obj, clazz);
        final Map<Field, Optional<Object>> superClazzFields = getObjFieldKv(obj, clazz.getSuperclass());
        final Map<Field, Optional<Object>> fields = new HashMap<>(clazzFields.size() + superClazzFields.size());
        fields.putAll(clazzFields);
        fields.putAll(superClazzFields);

        // 创建一个字符串列表来保存所有字段信息
        Map<String, Optional<Object>> fieldsInfo = new HashMap<>(fields.size());
        // 遍历所有字段
        for (Field field : fields.keySet()) {
            // 获取字段名
            String fieldName = field.getName();
            Optional<Object> fieldValue = fields.get(field);
            if (!fieldValue.isPresent()) {
                // 将字段信息添加到结果列表中
                fieldsInfo.put(fieldName, Optional.empty());
                continue;
            }
            // 如果字段类型是对象类型，则递归处理该对象类型的字段
            if (isPrimitiveOrWrapper(field.getType())) {
                // 将字段信息添加到结果列表中
                fieldsInfo.put(fieldName, fieldValue);
            } else {
                // 递归获取嵌套对象类型内的所有字段信息
                Map<String, Optional<Object>> nestedFieldNames = objToMap(fieldValue.get());
                if (MapUtils.isNotEmpty(nestedFieldNames)) {
                    // 将嵌套对象类型的字段信息添加到结果列表中
                    fieldsInfo.putAll(addNestedFieldPrefix(fieldName, nestedFieldNames));
                }
            }
        }
        // 将结果列表转换为字符串数组并返回
        return fieldsInfo;
    }

    /**
     * 在嵌套对象类型的字段名前添加前缀
     */
    private static Map<String, Optional<Object>> addNestedFieldPrefix(String prefix,
                                                                      Map<String, Optional<Object>> fieldsInfo) {
        Map<String, Optional<Object>> newFieldsInfo = new HashMap<>(fieldsInfo.size());
        for (Map.Entry<String, Optional<Object>> fieldInfo : fieldsInfo.entrySet()) {
            newFieldsInfo.put(prefix + "." + fieldInfo.getKey(), fieldInfo.getValue());
        }
        return newFieldsInfo;
    }


    /**
     * 获取对象字段kv
     *
     * @param obj obj
     * @return kv
     */
    public static <T> Map<Field, Optional<Object>> getObjFieldKv(T obj, Class<?> clazz) {
        final Field[] declaredFields = getDeclaredFields(clazz);
        return Arrays.stream(declaredFields)
                .peek(d -> d.setAccessible(true))
                .collect(Collectors.toMap(Function.identity(), d -> {
                    try {
                        return Optional.ofNullable(d.get(obj));
                    } catch (IllegalAccessException e) {
                        log.error("getObjFieldKv fail obj:{}", obj, e);
                        return Optional.empty();
                    }
                }));
    }

    /**
     * 检查类型是否为基本类型或包装类型
     *
     * @param type type
     * @return bool
     */
    public static boolean isPrimitiveOrWrapper(Class<?> type) {
        return type.isPrimitive() || isWrapperType(type);
    }

    /**
     * 检查类型是否为包装类型
     *
     * @param type type
     * @return bool
     */
    private static boolean isWrapperType(Class<?> type) {
        return type == String.class || type == Boolean.class || type == Character.class || type == Byte.class || type == Short.class
                || type == Integer.class || type == Long.class || type == Float.class || type == Double.class;
    }


    private static void setFieldValue(Object obj, String fieldName, Object value) throws ReflectiveOperationException {
        Class<?> clazz = obj.getClass();
        String[] fields = fieldName.split("\\.");
        Object currentObject = obj;

        for (int i = 0; i < fields.length - 1; i++) {
            Field field = clazz.getDeclaredField(fields[i]);
            field.setAccessible(true);
            currentObject = field.get(currentObject);
            if (currentObject == null) {
                if (field.getType().isPrimitive()) {
                    throw new IllegalStateException("Cannot instantiate primitive types");
                } else {
                    currentObject = field.getType().getDeclaredConstructor().newInstance();
                    field.set(obj, currentObject);
                }
            }
            clazz = field.getType();
        }

        Field field = clazz.getDeclaredField(fields[fields.length - 1]);
        field.setAccessible(true);
        field.set(currentObject, convertValue(field.getType(), value));
    }

    private static Object convertValue(Class<?> fieldType, Object value) {
        if (value == null) {
            return null;
        }
        if (fieldType.isPrimitive() || fieldType.isAssignableFrom(value.getClass())) {
            return value;
        }
        if (Integer.class.equals(fieldType) && value instanceof String) {
            return Integer.parseInt((String) value);
        } else if (Double.class.equals(fieldType) && value instanceof String) {
            return Double.parseDouble((String) value);
        } else if (Long.class.equals(fieldType) && value instanceof String) {
            return Long.parseLong((String) value);
        } else if (Float.class.equals(fieldType) && value instanceof String) {
            return Float.parseFloat((String) value);
        } else if (Boolean.class.equals(fieldType) && value instanceof String) {
            return Boolean.parseBoolean((String) value);
        } else if (String.class.equals(fieldType) && value instanceof Character) {
            return String.valueOf((Character) value);
        } else if (Character.class.equals(fieldType) && value instanceof String) {
            return ((String) value).charAt(0);
        }
        return value;
    }


    /**
     * 获取类所有字段列表（不包括合成字段）
     *
     * @param clazz
     * @return
     */
    public static Field[] getDeclaredFields(Class<?> clazz) {
        return stream(clazz.getDeclaredFields()).filter(ClazzUtil::classDeclaredFieldFilter).toArray(Field[]::new);
    }

    private static boolean classDeclaredFieldFilter(Field field) {
        return !(field.isSynthetic());
    }

}
