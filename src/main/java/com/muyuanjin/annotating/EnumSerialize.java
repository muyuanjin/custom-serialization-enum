package com.muyuanjin.annotating;

import org.springframework.core.annotation.AnnotationUtils;

/**
 * @author muyuanjin
 */
public interface EnumSerialize<T extends Enum<T> & EnumSerialize<T>> {
    default String getSerializationName() {
        return ((Enum<?>) this).name();
    }

    default Integer getSerializationId() {
        return ((Enum<?>) this).ordinal();
    }
    /**
     * 获取原始类，专门给适配器使用的
     */
    default Class<T> getOriginalClass() {
        //noinspection unchecked
        return (Class<T>) this.getClass();
    }
    /**
     * 获取原始枚举对象，专门给适配器使用的
     */
    default Enum<T> getOriginalEnum() {
        //noinspection unchecked
        return (Enum<T>) this;
    }

    static <T extends Enum<T> & EnumSerialize<T>> CustomSerializationEnum getAnnotation(Class<T> enumClass) {
        return AnnotationUtils.findAnnotation(enumClass, CustomSerializationEnum.class);
    }
}