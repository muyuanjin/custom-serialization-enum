package com.muyuanjin.annotating;

import sun.misc.SharedSecrets;

import java.lang.annotation.*;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author muyuanjin
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CustomSerializationEnum {
    Type myBatis();

    Type json() default Type.NAME;

    Type requestParam() default Type.NAME;

    enum Type {

        NAME {
            @Override
            public String getSerializedValue(EnumSerialize<?> serializationEnum) {
                return serializationEnum.getSerializationName();
            }
        },
        ID {
            @Override
            public Integer getSerializedValue(EnumSerialize<?> serializationEnum) {
                return serializationEnum.getSerializationId();
            }
        },
        CLASS {
            @Override
            public String getSerializedValue(EnumSerialize<?> serializationEnum) {
                return serializationEnum.getClass().getCanonicalName() + ":" + ((Enum<?>) serializationEnum).name();
            }
        };

        Type() {
        }

        static final Map<Class<? extends EnumSerialize<?>>, Map<Object, EnumSerialize<?>>> DESERIALIZE_MAP = new ConcurrentHashMap<>();

        public abstract Object getSerializedValue(EnumSerialize<?> serializationEnum);

        @SuppressWarnings("unchecked")
        public <T extends Enum<T> & EnumSerialize<T>> T getDeserializeObj(Class<T> enumClass, Object serializedValue) {
            if (enumClass == null || serializedValue == null) {
                return null;
            }
            return (T) DESERIALIZE_MAP.computeIfAbsent(enumClass, t -> new ConcurrentHashMap<>())
                    .computeIfAbsent(serializedValue, t -> Arrays.stream(SharedSecrets.getJavaLangAccess().getEnumConstantsShared(enumClass))
                            .filter(e -> getSerializedValue(e).equals(serializedValue)).findFirst().orElse(null));
        }
    }
}