package com.senac.viajaapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApi {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Viajá")
                        .version("1.0")
                        .description("API do app Viajá - organização de viagens de turismo e trabalho.")
                        .contact(new Contact().name("Rafael Reis")
                                .email("rafael.dnreis@gmail.com"))
                );
    }
}
