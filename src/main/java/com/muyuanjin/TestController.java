package com.muyuanjin;

import com.muyuanjin.entity.UserEntity;
import com.muyuanjin.enumerate.Gender;
import com.muyuanjin.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author muyuanjin
 */
@RestController
@RequiredArgsConstructor
public class TestController {
    private final UserMapper userMapper;

    @GetMapping
    public List<UserEntity> get() {
        return userMapper.select();
    }

    @PostMapping
    public void creat(@RequestBody UserEntity userEntity) {
        userMapper.insert(userEntity);
    }

    @GetMapping("/show")
    public Gender show(Gender gender) {
        return gender;
    }
}
