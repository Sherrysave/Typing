package com.sjt.typing;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * 打字练习平台后端启动类。
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@MapperScan("com.sjt.typing.mapper")
public class TypingApplication {

    public static void main(String[] args) {
        SpringApplication.run(TypingApplication.class, args);
    }
}
