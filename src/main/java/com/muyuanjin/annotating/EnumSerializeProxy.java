package com.muyuanjin.annotating;

/**
 * 用来包装没有实现{@link EnumSerialize} 的 但又被{@link CustomSerializationEnum}注解的类
 *
 * @author muyuanjin
 */
@SuppressWarnings("rawtypes")
public class EnumSerializeProxy implements EnumSerialize {
    private final Enum<?> enumInstance;

    public Enum<?> getEnumInstance() {
        return enumInstance;
    }

    public EnumSerializeProxy(Enum<?> enumInstance) {
        this.enumInstance = enumInstance;
    }

    @Override
    public String getSerializationName() {
        return enumInstance.name();
    }

    @Override
    public Integer getSerializationId() {
        return enumInstance.ordinal();
    }

    @Override
    public String toString() {
        return enumInstance.toString();
    }

    @Override
    public Class<?> getOriginalClass() {
        return enumInstance.getClass();
    }

    @Override
    public Enum<?> getOriginalEnum() {
        return enumInstance;
    }
}
