package com.gree.airconditioner;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ConfigurationTest {

  @Autowired private Environment environment;

  @Test
  void testConfigurationProperties() {
    // Test that test configuration properties are properly loaded
    assertEquals(
        "gree-airconditioner-rest-api-test", environment.getProperty("spring.application.name"));

    // Test logging configuration
    assertEquals("DEBUG", environment.getProperty("logging.level.com.gree.airconditioner"));
    assertEquals("DEBUG", environment.getProperty("logging.level.com.gree.hvac"));
    assertEquals("DEBUG", environment.getProperty("logging.level.org.springframework.web"));
    assertEquals("INFO", environment.getProperty("logging.level.root"));
  }

  @Test
  void testHvacClientConfiguration() {
    // Test HVAC client configuration
    assertEquals("1000", environment.getProperty("gree.hvac.client.connect-timeout"));
  }

  @Test
  void testServerConfiguration() {
    // Test server configuration
    assertEquals("0", environment.getProperty("server.port"));
  }

  @Test
  void testProfileConfiguration() {
    // Test that test profile is active
    String[] activeProfiles = environment.getActiveProfiles();
    assertTrue(activeProfiles.length > 0);
    assertTrue(contains(activeProfiles, "test"));
  }

  private boolean contains(String[] array, String value) {
    for (String item : array) {
      if (value.equals(item)) {
        return true;
      }
    }
    return false;
  }
}
