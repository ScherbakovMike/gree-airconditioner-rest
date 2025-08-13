package com.gree.hvac.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import javafx.stage.Stage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MainController
 */
@ExtendWith(MockitoExtension.class)
class MainControllerTest {

    @Mock
    private Stage mockStage;

    private MainController controller;

    @BeforeEach
    void setUp() {
        controller = new MainController();
    }

    @Test
    void testSetPrimaryStage() {
        // Test that primary stage can be set
        assertDoesNotThrow(() -> controller.setPrimaryStage(mockStage));
    }

    @Test
    void testInitialization() {
        // Test that controller can be instantiated
        assertNotNull(controller);
    }
}


