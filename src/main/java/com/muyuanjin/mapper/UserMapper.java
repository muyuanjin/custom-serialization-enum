package com.muyuanjin.mapper;

import com.muyuanjin.entity.UserEntity;

import java.util.List;

/**
 * @author muyuanjin
 */
public interface UserMapper {
    int insert(UserEntity entity);

    List<UserEntity> select();
}