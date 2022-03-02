package com.muyuanjin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan
@SpringBootApplication
public class TestDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestDemoApplication.class, args);
    }
}
