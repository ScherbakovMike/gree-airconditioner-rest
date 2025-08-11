package com.gree.airconditioner;

import static org.junit.jupiter.api.Assertions.*;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class GreeAirconditionerApplicationTest {

  @Autowired private ApplicationContext applicationContext;

  @Test
  void contextLoads() {
    // This test verifies that the Spring application context loads successfully
    assertNotNull(applicationContext);
  }

  @Test
  void applicationNameIsSet() {
    // Test that the application name is configured
    String applicationName =
        applicationContext.getEnvironment().getProperty("spring.application.name");
    assertNotNull(applicationName);
  }

  @Test
  void openApiBeanExists() {
    // Test that OpenAPI bean is configured
    OpenAPI openAPI = applicationContext.getBean(OpenAPI.class);
    assertNotNull(openAPI);

    Info info = openAPI.getInfo();
    assertNotNull(info);
    assertNotNull(info.getTitle());
    assertNotNull(info.getVersion());
  }
}
