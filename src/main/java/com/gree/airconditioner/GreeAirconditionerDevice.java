package com.gree.airconditioner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;

@Slf4j
@SpringBootApplication
@Configuration
public class GreeAirconditionerDevice {

    public static void main(String[] args) {
        log.info("🌡️ Starting GREE Air Conditioner REST API...");
        SpringApplication.run(GreeAirconditionerDevice.class, args);
        log.info("✅ GREE Air Conditioner REST API started successfully!");
        log.info("📚 API Documentation available at: http://localhost:8080/swagger-ui.html");
        log.info("📋 API Endpoints available at: http://localhost:8080/api/devices");
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GREE Air Conditioner REST API")
                        .version("1.0")
                        .description("REST API for discovering, connecting to, and controlling GREE air conditioning devices")
                        .contact(new Contact()
                                .name("GREE AC API")
                                .url("http://localhost:8080")
                                .email("support@example.com")));
    }
}
