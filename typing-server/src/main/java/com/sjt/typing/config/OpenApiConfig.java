package com.sjt.typing.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档配置。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI typingOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("打字练习平台 API")
                        .version("1.0.0")
                        .description("对标 Monkeytype 架构的打字练习平台后端接口文档"));
    }
}
