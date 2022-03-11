package com.muyuanjin.annotating;

import sun.misc.SharedSecrets;

import java.lang.annotation.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <H1>指示枚举类自定义序列化的方法，被注解类必须为 <span style="color:#c7c7c7">枚举类</span></H1><br>
 * <H1>被注解类通常实现 <span style="color:#c7c7c7">{@link EnumSerialize}</span>，用以提供更丰富的序列化选择，否则会包装成<span style="color:#c7c7c7">{@link EnumSerializeProxy}</span></H1>
 *
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
                return serializationEnum.getOriginalClass().getCanonicalName() + ":" + serializationEnum.getOriginalEnum().name();
            }
        },
        TO_STRING {
            @Override
            public Object getSerializedValue(EnumSerialize<?> serializationEnum) {
                return serializationEnum.toString();
            }
        };

        Type() {
        }

        static final Map<Class<? extends EnumSerialize<?>>, Map<Object, Enum<?>>> DESERIALIZE_MAP = new ConcurrentHashMap<>();

        public abstract Object getSerializedValue(EnumSerialize<?> serializationEnum);

        @SuppressWarnings("unchecked")
        public <T extends Enum<T> & EnumSerialize<T>> T getDeserializeObj(Class<T> enumClass, Object serializedValue) {
            if (enumClass == null || serializedValue == null) {
                return null;
            }
            return (T) DESERIALIZE_MAP.computeIfAbsent(enumClass, t -> new ConcurrentHashMap<>())
                    .computeIfAbsent(serializedValue.toString(),
                            t -> Arrays.stream(SharedSecrets.getJavaLangAccess().getEnumConstantsShared(enumClass)).filter(Objects::nonNull)
                                    .filter(e -> {
                                        //noinspection ConstantConditions
                                        if (e instanceof EnumSerialize) {
                                            return getSerializedValue(e).toString().equals(serializedValue.toString());
                                        } else if (e.getClass().isEnum()) {
                                            return getSerializedValue(new EnumSerializeProxy(e)).toString().equals(serializedValue.toString());
                                        }
                                        return false;
                                    }).findFirst().orElse(null)
                    );
        }
    }
}