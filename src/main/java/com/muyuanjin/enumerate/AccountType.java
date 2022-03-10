package com.muyuanjin.enumerate;

import com.muyuanjin.annotating.CustomSerializationEnum;

/**
 * @author muyuanjin
 */
@CustomSerializationEnum(myBatis = CustomSerializationEnum.Type.ID, json = CustomSerializationEnum.Type.NAME, requestParam = CustomSerializationEnum.Type.ID)
public enum AccountType {
    BUILT_IN, ORDINARY, GUEST
}