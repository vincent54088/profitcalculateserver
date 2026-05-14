package com.huawei.tool;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@MapperScan("com.huawei.tool.dao")
public class ProfitCalculateApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProfitCalculateApplication.class, args);
    }
}
