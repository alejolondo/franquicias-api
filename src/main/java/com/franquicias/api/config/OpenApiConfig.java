package com.franquicias.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI franquiciasOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Franquicias")
                        .description("API reactiva para la gestión de franquicias, sucursales y productos")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Alejandro")
                                .url("https://github.com/alejolondo02/franquicias-api")));
    }
}