package com.muyuanjin.map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.muyuanjin.annotating.CustomSerializationEnum;
import com.muyuanjin.annotating.EnumSerialize;
import com.muyuanjin.annotating.EnumSerializeProxy;
import org.springframework.core.annotation.AnnotationUtils;

import java.io.IOException;

/**
 * @author muyuanjin
 */
public class CustomSerializationEnumJsonSerializer<T extends Enum<T> & EnumSerialize<T>> extends JsonSerializer<T> {
    private final CustomSerializationEnum.Type type;

    public CustomSerializationEnumJsonSerializer(Class<T> customSerializationEnumClass) {
        CustomSerializationEnum annotation = AnnotationUtils.findAnnotation(customSerializationEnumClass, CustomSerializationEnum.class);
        type = annotation == null ? CustomSerializationEnum.Type.NAME : annotation.json();
    }

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        Object serializedValue;
        //noinspection ConstantConditions
        if (value instanceof EnumSerialize) {
            serializedValue = type.getSerializedValue(value);
        } else {
            //noinspection ConstantConditions
            serializedValue = type.getSerializedValue(new EnumSerializeProxy(value));
        }
        serializers.findValueSerializer(serializedValue.getClass()).serialize(serializedValue, gen, serializers);
    }
}
