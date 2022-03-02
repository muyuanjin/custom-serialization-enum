package com.muyuanjin.map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.muyuanjin.annotating.CustomSerializationEnum;
import com.muyuanjin.annotating.EnumSerialize;
import org.springframework.core.annotation.AnnotationUtils;

import java.io.IOException;

/**
 * @author muyuanjin
 */
public class CustomSerializationEnumJsonDeserializer<T extends Enum<T> & EnumSerialize<T>> extends JsonDeserializer<T> {
    private final Class<T> enumCLass;
    private final CustomSerializationEnum.Type type;

    public CustomSerializationEnumJsonDeserializer(Class<T> customSerializationEnumClass) {
        enumCLass = customSerializationEnumClass;
        CustomSerializationEnum annotation = AnnotationUtils.findAnnotation(customSerializationEnumClass, CustomSerializationEnum.class);
        type = annotation == null ? CustomSerializationEnum.Type.NAME : annotation.json();
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (type.equals(CustomSerializationEnum.Type.ID)) {
            return type.getDeserializeObj(enumCLass, p.getIntValue());
        } else {
            return type.getDeserializeObj(enumCLass, p.getText());
        }
    }
}
