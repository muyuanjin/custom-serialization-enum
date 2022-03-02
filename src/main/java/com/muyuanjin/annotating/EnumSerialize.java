package com.muyuanjin.annotating;

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
}