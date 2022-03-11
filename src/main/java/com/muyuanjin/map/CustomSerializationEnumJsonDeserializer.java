package com.muyuanjin.map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.muyuanjin.annotating.CustomSerializationEnum;
import com.muyuanjin.annotating.EnumSerialize;
import javafx.util.Pair;

import java.io.IOException;
import java.util.Set;

/**
 * @author muyuanjin
 */
public class CustomSerializationEnumJsonDeserializer<T extends Enum<T> & EnumSerialize<T>> extends JsonDeserializer<T> {

    private final CustomSerializationEnum.Type type;
    private final Class<T> clazz;

    public CustomSerializationEnumJsonDeserializer(Pair<Class<Enum<?>>, Set<EnumSerialize<T>>> enumSerialize) {
        EnumSerialize<T> next = enumSerialize.getValue().iterator().next();
        clazz = next.getOriginalClass();
        CustomSerializationEnum annotation = next.getAnnotation();
        type = annotation == null ? CustomSerializationEnum.Type.NAME : annotation.json();
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return type.getDeserializeObj(clazz, p.getText());
    }
}
