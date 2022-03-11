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

    default Class<T> getOriginalClass() {
        //noinspection unchecked
        return (Class<T>) this.getClass();
    }

    default Enum<T> getOriginalEnum() {
        //noinspection unchecked
        return (Enum<T>) this;
    }

    default CustomSerializationEnum getAnnotation() {
        return AnnotationUtils.findAnnotation(getOriginalClass(), CustomSerializationEnum.class);
    }
}