package com.gree.hvac.controller;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for MainController */
@ExtendWith(MockitoExtension.class)
class MainControllerTest {

  private MainController controller;

  @BeforeEach
  void setUp() {
    controller = new MainController();
  }

  @Test
  void testInitialization() {
    // Test that controller can be instantiated
    assertNotNull(controller);
  }
}
