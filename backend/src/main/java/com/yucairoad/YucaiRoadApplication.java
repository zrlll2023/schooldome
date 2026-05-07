package com.yucairoad;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.yucairoad.mapper")
public class YucaiRoadApplication {
    public static void main(String[] args) {
        SpringApplication.run(YucaiRoadApplication.class, args);
    }
}
