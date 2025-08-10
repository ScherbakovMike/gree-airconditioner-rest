package com.gree.airconditioner;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Slf4j
@SpringBootApplication
@Configuration
public class GreeAirconditionerApplication {

  @Value("${server.port}")
  private String serverPort;

  public static void main(String[] args) {
    log.info("🌡️ Starting GREE Air Conditioner REST API...");
    SpringApplication.run(GreeAirconditionerApplication.class, args);
  }

  @EventListener(ApplicationReadyEvent.class)
  public void logApplicationUrls() {
    log.info("✅ GREE Air Conditioner REST API started successfully!");
    log.info("📚 API Documentation available at: http://localhost:{}/swagger-ui.html", serverPort);
    log.info("📋 API Endpoints available at: http://localhost:{}/api/devices", serverPort);
  }

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("GREE Air Conditioner REST API")
                .version("1.0")
                .description(
                    "REST API for discovering, connecting to, and controlling GREE air conditioning devices")
                .contact(
                    new Contact()
                        .name("GREE AC API")
                        .url("http://localhost:" + serverPort)
                        .email("support@example.com")));
  }
}
