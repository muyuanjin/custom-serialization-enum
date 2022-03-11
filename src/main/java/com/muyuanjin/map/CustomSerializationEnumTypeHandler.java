package com.muyuanjin.map;

import com.muyuanjin.annotating.CustomSerializationEnum;
import com.muyuanjin.annotating.EnumSerialize;
import com.muyuanjin.annotating.EnumSerializeProxy;
import javafx.util.Pair;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

/**
 * @author muyuanjin
 */
public class CustomSerializationEnumTypeHandler<T extends Enum<T> & EnumSerialize<T>> extends BaseTypeHandler<T> {
    private final CustomSerializationEnum.Type type;
    private final Class<T> clazz;

    public CustomSerializationEnumTypeHandler(Pair<Class<Enum<?>>, Set<EnumSerialize<T>>> enumSerialize) {
        EnumSerialize<T> next = enumSerialize.getValue().iterator().next();
        clazz = next.getOriginalClass();
        CustomSerializationEnum annotation = next.getAnnotation();
        type = annotation == null ? CustomSerializationEnum.Type.NAME : annotation.myBatis();
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        Object serializedValue;
        //noinspection ConstantConditions
        if (parameter instanceof EnumSerialize) {
            serializedValue = type.getSerializedValue(parameter);
        } else {
            //noinspection ConstantConditions
            serializedValue = type.getSerializedValue(new EnumSerializeProxy(parameter));
        }
        if (serializedValue instanceof String) {
            ps.setString(i, (String) serializedValue);
        } else if (serializedValue instanceof Integer) {
            ps.setInt(i, (Integer) serializedValue);
        }
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return type.getDeserializeObj(clazz, rs.getObject(columnName));
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return type.getDeserializeObj(clazz, rs.getObject(columnIndex));
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return type.getDeserializeObj(clazz, cs.getObject(columnIndex));
    }
}
