package com.gree.hvac.discovery;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultTimeServiceTest {

  private DefaultTimeService timeService;

  @BeforeEach
  void setUp() {
    timeService = new DefaultTimeService();
  }

  @Test
  void testGetCurrentTimeMillis() {
    // Act
    long before = System.currentTimeMillis();
    long actual = timeService.getCurrentTimeMillis();
    long after = System.currentTimeMillis();

    // Assert
    assertTrue(actual >= before);
    assertTrue(actual <= after);
  }
}
