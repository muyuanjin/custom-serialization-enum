package com.muyuanjin.map;

import com.muyuanjin.annotating.CustomSerializationEnum;
import com.muyuanjin.annotating.EnumSerialize;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.springframework.core.annotation.AnnotationUtils;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author muyuanjin
 */
public class CustomSerializationEnumTypeHandler<T extends Enum<T> & EnumSerialize<T>> extends BaseTypeHandler<T> {
    private final Class<T> enumCLass;
    private final CustomSerializationEnum.Type type;

    public CustomSerializationEnumTypeHandler(Class<T> customSerializationEnumClass) {
        enumCLass = customSerializationEnumClass;
        CustomSerializationEnum annotation = AnnotationUtils.findAnnotation(customSerializationEnumClass, CustomSerializationEnum.class);
        type = annotation == null ? CustomSerializationEnum.Type.NAME : annotation.myBatis();
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        Object serializedValue = type.getSerializedValue(parameter);
        if (serializedValue instanceof String) {
            ps.setString(i, (String) serializedValue);
        } else if (serializedValue instanceof Integer) {
            ps.setInt(i, (Integer) serializedValue);
        }
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        if (type.equals(CustomSerializationEnum.Type.ID)) {
            return type.getDeserializeObj(enumCLass, rs.getInt(columnName));
        } else {
            return type.getDeserializeObj(enumCLass, rs.getString(columnName));
        }
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        if (type.equals(CustomSerializationEnum.Type.ID)) {
            return type.getDeserializeObj(enumCLass, rs.getInt(columnIndex));
        } else {
            return type.getDeserializeObj(enumCLass, rs.getString(columnIndex));
        }
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        if (type.equals(CustomSerializationEnum.Type.ID)) {
            return type.getDeserializeObj(enumCLass, cs.getInt(columnIndex));
        } else {
            return type.getDeserializeObj(enumCLass, cs.getString(columnIndex));
        }
    }
}
