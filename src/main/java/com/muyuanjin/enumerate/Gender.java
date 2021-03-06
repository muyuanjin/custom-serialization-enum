package com.muyuanjin.enumerate;

import com.muyuanjin.annotating.CustomSerializationEnum;
import com.muyuanjin.annotating.EnumSerialize;

/**
 * @author muyuanjin
 */
@CustomSerializationEnum(myBatis = CustomSerializationEnum.Type.ID, json = CustomSerializationEnum.Type.NAME)
public enum Gender implements EnumSerialize<Gender> {
    MALE("男"),
    FEMALE("女"),
    UNKNOWN("未知") {
        @Override
        public String getSerializationName() {
            return "秀吉";
        }

        @Override
        public Integer getSerializationId() {
            return 114514;
        }
    };

    private final String name;

    Gender(String name) {
        this.name = name;
    }

    @Override
    public String getSerializationName() {
        return name;
    }
}