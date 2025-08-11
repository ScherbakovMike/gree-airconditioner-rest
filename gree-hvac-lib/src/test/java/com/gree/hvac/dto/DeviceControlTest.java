package com.gree.hvac.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeviceControlTest {

  private DeviceControl deviceControl;

  @BeforeEach
  void setUp() {
    deviceControl = new DeviceControl();
  }

  @Test
  void testDefaultConstructor() {
    assertNotNull(deviceControl);
    assertNull(deviceControl.getPower());
    assertNull(deviceControl.getTemperature());
    assertNull(deviceControl.getMode());
    assertNull(deviceControl.getFanSpeed());
    assertNull(deviceControl.getSwingHorizontal());
    assertNull(deviceControl.getSwingVertical());
    assertNull(deviceControl.getLights());
    assertNull(deviceControl.getTurbo());
    assertNull(deviceControl.getQuiet());
    assertNull(deviceControl.getHealth());
    assertNull(deviceControl.getPowerSave());
    assertNull(deviceControl.getSleep());
  }

  @Test
  void testPowerProperty() {
    // Test setting power to true
    deviceControl.setPower(true);
    assertTrue(deviceControl.getPower());

    // Test setting power to false
    deviceControl.setPower(false);
    assertFalse(deviceControl.getPower());

    // Test setting power to null
    deviceControl.setPower(null);
    assertNull(deviceControl.getPower());
  }

  @Test
  void testTemperatureProperty() {
    // Test valid temperature values
    deviceControl.setTemperature(16);
    assertEquals(16, deviceControl.getTemperature());

    deviceControl.setTemperature(22);
    assertEquals(22, deviceControl.getTemperature());

    deviceControl.setTemperature(30);
    assertEquals(30, deviceControl.getTemperature());

    // Test edge cases
    deviceControl.setTemperature(17);
    assertEquals(17, deviceControl.getTemperature());

    deviceControl.setTemperature(29);
    assertEquals(29, deviceControl.getTemperature());

    // Test setting to null
    deviceControl.setTemperature(null);
    assertNull(deviceControl.getTemperature());
  }

  @Test
  void testModeProperty() {
    // Test valid mode values
    deviceControl.setMode("AUTO");
    assertEquals("AUTO", deviceControl.getMode());

    deviceControl.setMode("COOL");
    assertEquals("COOL", deviceControl.getMode());

    deviceControl.setMode("HEAT");
    assertEquals("HEAT", deviceControl.getMode());

    deviceControl.setMode("DRY");
    assertEquals("DRY", deviceControl.getMode());

    deviceControl.setMode("FAN_ONLY");
    assertEquals("FAN_ONLY", deviceControl.getMode());

    // Test case sensitivity
    deviceControl.setMode("cool");
    assertEquals("cool", deviceControl.getMode());

    deviceControl.setMode("Heat");
    assertEquals("Heat", deviceControl.getMode());

    // Test setting to null
    deviceControl.setMode(null);
    assertNull(deviceControl.getMode());
  }

  @Test
  void testFanSpeedProperty() {
    // Test valid fan speed values
    deviceControl.setFanSpeed("AUTO");
    assertEquals("AUTO", deviceControl.getFanSpeed());

    deviceControl.setFanSpeed("LOW");
    assertEquals("LOW", deviceControl.getFanSpeed());

    deviceControl.setFanSpeed("MEDIUM");
    assertEquals("MEDIUM", deviceControl.getFanSpeed());

    deviceControl.setFanSpeed("HIGH");
    assertEquals("HIGH", deviceControl.getFanSpeed());

    // Test case sensitivity
    deviceControl.setFanSpeed("low");
    assertEquals("low", deviceControl.getFanSpeed());

    deviceControl.setFanSpeed("Medium");
    assertEquals("Medium", deviceControl.getFanSpeed());

    // Test setting to null
    deviceControl.setFanSpeed(null);
    assertNull(deviceControl.getFanSpeed());
  }

  @Test
  void testSwingHorizontalProperty() {
    // Test valid swing horizontal values
    deviceControl.setSwingHorizontal("DEFAULT");
    assertEquals("DEFAULT", deviceControl.getSwingHorizontal());

    deviceControl.setSwingHorizontal("FULL");
    assertEquals("FULL", deviceControl.getSwingHorizontal());

    deviceControl.setSwingHorizontal("FIXED_LEFT");
    assertEquals("FIXED_LEFT", deviceControl.getSwingHorizontal());

    deviceControl.setSwingHorizontal("FIXED_RIGHT");
    assertEquals("FIXED_RIGHT", deviceControl.getSwingHorizontal());

    // Test case sensitivity
    deviceControl.setSwingHorizontal("full");
    assertEquals("full", deviceControl.getSwingHorizontal());

    deviceControl.setSwingHorizontal("Fixed_Left");
    assertEquals("Fixed_Left", deviceControl.getSwingHorizontal());

    // Test setting to null
    deviceControl.setSwingHorizontal(null);
    assertNull(deviceControl.getSwingHorizontal());
  }

  @Test
  void testSwingVerticalProperty() {
    // Test valid swing vertical values
    deviceControl.setSwingVertical("DEFAULT");
    assertEquals("DEFAULT", deviceControl.getSwingVertical());

    deviceControl.setSwingVertical("FULL");
    assertEquals("FULL", deviceControl.getSwingVertical());

    deviceControl.setSwingVertical("FIXED_TOP");
    assertEquals("FIXED_TOP", deviceControl.getSwingVertical());

    deviceControl.setSwingVertical("FIXED_BOTTOM");
    assertEquals("FIXED_BOTTOM", deviceControl.getSwingVertical());

    // Test case sensitivity
    deviceControl.setSwingVertical("full");
    assertEquals("full", deviceControl.getSwingVertical());

    deviceControl.setSwingVertical("Fixed_Top");
    assertEquals("Fixed_Top", deviceControl.getSwingVertical());

    // Test setting to null
    deviceControl.setSwingVertical(null);
    assertNull(deviceControl.getSwingVertical());
  }

  @Test
  void testLightsProperty() {
    // Test setting lights to true
    deviceControl.setLights(true);
    assertTrue(deviceControl.getLights());

    // Test setting lights to false
    deviceControl.setLights(false);
    assertFalse(deviceControl.getLights());

    // Test setting lights to null
    deviceControl.setLights(null);
    assertNull(deviceControl.getLights());
  }

  @Test
  void testTurboProperty() {
    // Test setting turbo to true
    deviceControl.setTurbo(true);
    assertTrue(deviceControl.getTurbo());

    // Test setting turbo to false
    deviceControl.setTurbo(false);
    assertFalse(deviceControl.getTurbo());

    // Test setting turbo to null
    deviceControl.setTurbo(null);
    assertNull(deviceControl.getTurbo());
  }

  @Test
  void testQuietProperty() {
    // Test setting quiet to true
    deviceControl.setQuiet(true);
    assertTrue(deviceControl.getQuiet());

    // Test setting quiet to false
    deviceControl.setQuiet(false);
    assertFalse(deviceControl.getQuiet());

    // Test setting quiet to null
    deviceControl.setQuiet(null);
    assertNull(deviceControl.getQuiet());
  }

  @Test
  void testHealthProperty() {
    // Test setting health to true
    deviceControl.setHealth(true);
    assertTrue(deviceControl.getHealth());

    // Test setting health to false
    deviceControl.setHealth(false);
    assertFalse(deviceControl.getHealth());

    // Test setting health to null
    deviceControl.setHealth(null);
    assertNull(deviceControl.getHealth());
  }

  @Test
  void testPowerSaveProperty() {
    // Test setting power save to true
    deviceControl.setPowerSave(true);
    assertTrue(deviceControl.getPowerSave());

    // Test setting power save to false
    deviceControl.setPowerSave(false);
    assertFalse(deviceControl.getPowerSave());

    // Test setting power save to null
    deviceControl.setPowerSave(null);
    assertNull(deviceControl.getPowerSave());
  }

  @Test
  void testSleepProperty() {
    // Test setting sleep to true
    deviceControl.setSleep(true);
    assertTrue(deviceControl.getSleep());

    // Test setting sleep to false
    deviceControl.setSleep(false);
    assertFalse(deviceControl.getSleep());

    // Test setting sleep to null
    deviceControl.setSleep(null);
    assertNull(deviceControl.getSleep());
  }

  @Test
  void testCompleteDeviceControl() {
    // Test setting all properties
    deviceControl.setPower(true);
    deviceControl.setTemperature(22);
    deviceControl.setMode("COOL");
    deviceControl.setFanSpeed("HIGH");
    deviceControl.setSwingHorizontal("FULL");
    deviceControl.setSwingVertical("FULL");
    deviceControl.setLights(true);
    deviceControl.setTurbo(false);
    deviceControl.setQuiet(true);
    deviceControl.setHealth(false);
    deviceControl.setPowerSave(true);
    deviceControl.setSleep(false);

    // Verify all properties
    assertTrue(deviceControl.getPower());
    assertEquals(22, deviceControl.getTemperature());
    assertEquals("COOL", deviceControl.getMode());
    assertEquals("HIGH", deviceControl.getFanSpeed());
    assertEquals("FULL", deviceControl.getSwingHorizontal());
    assertEquals("FULL", deviceControl.getSwingVertical());
    assertTrue(deviceControl.getLights());
    assertFalse(deviceControl.getTurbo());
    assertTrue(deviceControl.getQuiet());
    assertFalse(deviceControl.getHealth());
    assertTrue(deviceControl.getPowerSave());
    assertFalse(deviceControl.getSleep());
  }

  @Test
  void testTemperatureRangeValidation() {
    // Test temperature within valid range (16-30)
    deviceControl.setTemperature(16);
    assertEquals(16, deviceControl.getTemperature());

    deviceControl.setTemperature(30);
    assertEquals(30, deviceControl.getTemperature());

    deviceControl.setTemperature(22);
    assertEquals(22, deviceControl.getTemperature());

    // Test edge cases (these should still work as the DTO doesn't enforce validation)
    deviceControl.setTemperature(15);
    assertEquals(15, deviceControl.getTemperature());

    deviceControl.setTemperature(31);
    assertEquals(31, deviceControl.getTemperature());

    deviceControl.setTemperature(0);
    assertEquals(0, deviceControl.getTemperature());

    deviceControl.setTemperature(100);
    assertEquals(100, deviceControl.getTemperature());
  }

  @Test
  void testModeValidation() {
    // Test valid modes
    String[] validModes = {"AUTO", "COOL", "HEAT", "DRY", "FAN_ONLY"};
    for (String mode : validModes) {
      deviceControl.setMode(mode);
      assertEquals(mode, deviceControl.getMode());
    }

    // Test invalid modes (should still work as DTO doesn't enforce validation)
    String[] invalidModes = {"INVALID", "TEST", "MODE", "", "123"};
    for (String mode : invalidModes) {
      deviceControl.setMode(mode);
      assertEquals(mode, deviceControl.getMode());
    }
  }

  @Test
  void testFanSpeedValidation() {
    // Test valid fan speeds
    String[] validFanSpeeds = {"AUTO", "LOW", "MEDIUM", "HIGH"};
    for (String fanSpeed : validFanSpeeds) {
      deviceControl.setFanSpeed(fanSpeed);
      assertEquals(fanSpeed, deviceControl.getFanSpeed());
    }

    // Test invalid fan speeds (should still work as DTO doesn't enforce validation)
    String[] invalidFanSpeeds = {"INVALID", "TEST", "SPEED", "", "123"};
    for (String fanSpeed : invalidFanSpeeds) {
      deviceControl.setFanSpeed(fanSpeed);
      assertEquals(fanSpeed, deviceControl.getFanSpeed());
    }
  }

  @Test
  void testSwingValidation() {
    // Test valid horizontal swings
    String[] validHorizontalSwings = {"DEFAULT", "FULL", "FIXED_LEFT", "FIXED_RIGHT"};
    for (String swing : validHorizontalSwings) {
      deviceControl.setSwingHorizontal(swing);
      assertEquals(swing, deviceControl.getSwingHorizontal());
    }

    // Test valid vertical swings
    String[] validVerticalSwings = {"DEFAULT", "FULL", "FIXED_TOP", "FIXED_BOTTOM"};
    for (String swing : validVerticalSwings) {
      deviceControl.setSwingVertical(swing);
      assertEquals(swing, deviceControl.getSwingVertical());
    }

    // Test invalid swings (should still work as DTO doesn't enforce validation)
    String[] invalidSwings = {"INVALID", "TEST", "SWING", "", "123"};
    for (String swing : invalidSwings) {
      deviceControl.setSwingHorizontal(swing);
      assertEquals(swing, deviceControl.getSwingHorizontal());

      deviceControl.setSwingVertical(swing);
      assertEquals(swing, deviceControl.getSwingVertical());
    }
  }

  @Test
  void testBooleanPropertiesEdgeCases() {
    // Test all boolean properties with various values
    Boolean[] booleanValues = {true, false, null};

    for (Boolean value : booleanValues) {
      deviceControl.setPower(value);
      assertEquals(value, deviceControl.getPower());

      deviceControl.setLights(value);
      assertEquals(value, deviceControl.getLights());

      deviceControl.setTurbo(value);
      assertEquals(value, deviceControl.getTurbo());

      deviceControl.setQuiet(value);
      assertEquals(value, deviceControl.getQuiet());

      deviceControl.setHealth(value);
      assertEquals(value, deviceControl.getHealth());

      deviceControl.setPowerSave(value);
      assertEquals(value, deviceControl.getPowerSave());

      deviceControl.setSleep(value);
      assertEquals(value, deviceControl.getSleep());
    }
  }

  @Test
  void testStringPropertiesEdgeCases() {
    // Test string properties with various values
    String[] stringValues = {"", " ", "test", "123", "!@#$%", null};

    for (String value : stringValues) {
      deviceControl.setMode(value);
      assertEquals(value, deviceControl.getMode());

      deviceControl.setFanSpeed(value);
      assertEquals(value, deviceControl.getFanSpeed());

      deviceControl.setSwingHorizontal(value);
      assertEquals(value, deviceControl.getSwingHorizontal());

      deviceControl.setSwingVertical(value);
      assertEquals(value, deviceControl.getSwingVertical());
    }
  }

  @Test
  void testIntegerPropertiesEdgeCases() {
    // Test integer properties with various values
    Integer[] integerValues = {Integer.MIN_VALUE, -100, -1, 0, 1, 100, Integer.MAX_VALUE, null};

    for (Integer value : integerValues) {
      deviceControl.setTemperature(value);
      assertEquals(value, deviceControl.getTemperature());
    }
  }
}
