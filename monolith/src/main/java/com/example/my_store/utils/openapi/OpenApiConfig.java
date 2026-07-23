package com.example.my_store.utils.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI myStoreOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("My Store API")
                        .description("REST API интернет-магазина")
                        .version("1.0"));
    }
}
