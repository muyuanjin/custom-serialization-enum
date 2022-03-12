package com.muyuanjin.annotating;

/**
 * 用来包装没有实现{@link EnumSerialize} 的 但又被{@link CustomSerializationEnum}注解的类
 *
 * @author muyuanjin
 */
@SuppressWarnings("rawtypes")
public final class EnumSerializeAdapter implements EnumSerialize {
    private final Enum<?> enumInstance;

    public Enum<?> getEnumInstance() {
        return enumInstance;
    }

    public EnumSerializeAdapter(Enum<?> enumInstance) {
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

    /**
     * 获取原始类，专门给适配器使用的
     */
    @Override
    public Class<?> getOriginalClass() {
        return enumInstance.getClass();
    }
    /**
     * 获取原始枚举对象，专门给适配器使用的
     */
    @Override
    public Enum<?> getOriginalEnum() {
        return enumInstance;
    }
}
