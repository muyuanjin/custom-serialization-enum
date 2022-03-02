package com.muyuanjin.entity;

import com.muyuanjin.enumerate.Gender;
import lombok.Data;

/**
 * @author muyuanjin
 */
@Data
public class UserEntity {
    private String username;
    private String password;
    private Gender gender;
}
