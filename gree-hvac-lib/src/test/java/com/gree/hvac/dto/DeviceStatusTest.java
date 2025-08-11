package com.gree.hvac.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeviceStatusTest {

  private DeviceStatus deviceStatus;

  @BeforeEach
  void setUp() {
    deviceStatus = new DeviceStatus();
  }

  @Test
  void testDefaultConstructor() {
    assertNotNull(deviceStatus);
    assertNull(deviceStatus.getDeviceId());
    assertNull(deviceStatus.getPower());
    assertNull(deviceStatus.getTemperature());
    assertNull(deviceStatus.getCurrentTemperature());
    assertNull(deviceStatus.getMode());
    assertNull(deviceStatus.getFanSpeed());
    assertNull(deviceStatus.getSwingHorizontal());
    assertNull(deviceStatus.getSwingVertical());
    assertNull(deviceStatus.getLights());
    assertNull(deviceStatus.getTurbo());
    assertNull(deviceStatus.getQuiet());
    assertNull(deviceStatus.getHealth());
    assertNull(deviceStatus.getPowerSave());
    assertNull(deviceStatus.getSleep());
  }

  @Test
  void testDeviceIdProperty() {
    // Test setting device ID
    deviceStatus.setDeviceId("test-device-123");
    assertEquals("test-device-123", deviceStatus.getDeviceId());

    deviceStatus.setDeviceId("MAC:AA:BB:CC:DD:EE:FF");
    assertEquals("MAC:AA:BB:CC:DD:EE:FF", deviceStatus.getDeviceId());

    deviceStatus.setDeviceId("192.168.1.100");
    assertEquals("192.168.1.100", deviceStatus.getDeviceId());

    // Test setting to null
    deviceStatus.setDeviceId(null);
    assertNull(deviceStatus.getDeviceId());

    // Test empty string
    deviceStatus.setDeviceId("");
    assertEquals("", deviceStatus.getDeviceId());
  }

  @Test
  void testPowerProperty() {
    // Test setting power to true
    deviceStatus.setPower(true);
    assertTrue(deviceStatus.getPower());

    // Test setting power to false
    deviceStatus.setPower(false);
    assertFalse(deviceStatus.getPower());

    // Test setting power to null
    deviceStatus.setPower(null);
    assertNull(deviceStatus.getPower());
  }

  @Test
  void testTemperatureProperty() {
    // Test valid temperature values
    deviceStatus.setTemperature(16);
    assertEquals(16, deviceStatus.getTemperature());

    deviceStatus.setTemperature(22);
    assertEquals(22, deviceStatus.getTemperature());

    deviceStatus.setTemperature(30);
    assertEquals(30, deviceStatus.getTemperature());

    // Test edge cases
    deviceStatus.setTemperature(17);
    assertEquals(17, deviceStatus.getTemperature());

    deviceStatus.setTemperature(29);
    assertEquals(29, deviceStatus.getTemperature());

    // Test setting to null
    deviceStatus.setTemperature(null);
    assertNull(deviceStatus.getTemperature());
  }

  @Test
  void testCurrentTemperatureProperty() {
    // Test valid current temperature values
    deviceStatus.setCurrentTemperature(16);
    assertEquals(16, deviceStatus.getCurrentTemperature());

    deviceStatus.setCurrentTemperature(22);
    assertEquals(22, deviceStatus.getCurrentTemperature());

    deviceStatus.setCurrentTemperature(30);
    assertEquals(30, deviceStatus.getCurrentTemperature());

    // Test edge cases
    deviceStatus.setCurrentTemperature(17);
    assertEquals(17, deviceStatus.getCurrentTemperature());

    deviceStatus.setCurrentTemperature(29);
    assertEquals(29, deviceStatus.getCurrentTemperature());

    // Test setting to null
    deviceStatus.setCurrentTemperature(null);
    assertNull(deviceStatus.getCurrentTemperature());
  }

  @Test
  void testModeProperty() {
    // Test valid mode values
    deviceStatus.setMode("AUTO");
    assertEquals("AUTO", deviceStatus.getMode());

    deviceStatus.setMode("COOL");
    assertEquals("COOL", deviceStatus.getMode());

    deviceStatus.setMode("HEAT");
    assertEquals("HEAT", deviceStatus.getMode());

    deviceStatus.setMode("DRY");
    assertEquals("DRY", deviceStatus.getMode());

    deviceStatus.setMode("FAN_ONLY");
    assertEquals("FAN_ONLY", deviceStatus.getMode());

    // Test case sensitivity
    deviceStatus.setMode("cool");
    assertEquals("cool", deviceStatus.getMode());

    deviceStatus.setMode("Heat");
    assertEquals("Heat", deviceStatus.getMode());

    // Test setting to null
    deviceStatus.setMode(null);
    assertNull(deviceStatus.getMode());
  }

  @Test
  void testFanSpeedProperty() {
    // Test valid fan speed values
    deviceStatus.setFanSpeed("AUTO");
    assertEquals("AUTO", deviceStatus.getFanSpeed());

    deviceStatus.setFanSpeed("LOW");
    assertEquals("LOW", deviceStatus.getFanSpeed());

    deviceStatus.setFanSpeed("MEDIUM");
    assertEquals("MEDIUM", deviceStatus.getFanSpeed());

    deviceStatus.setFanSpeed("HIGH");
    assertEquals("HIGH", deviceStatus.getFanSpeed());

    // Test case sensitivity
    deviceStatus.setFanSpeed("low");
    assertEquals("low", deviceStatus.getFanSpeed());

    deviceStatus.setFanSpeed("Medium");
    assertEquals("Medium", deviceStatus.getFanSpeed());

    // Test setting to null
    deviceStatus.setFanSpeed(null);
    assertNull(deviceStatus.getFanSpeed());
  }

  @Test
  void testSwingHorizontalProperty() {
    // Test valid swing horizontal values
    deviceStatus.setSwingHorizontal("DEFAULT");
    assertEquals("DEFAULT", deviceStatus.getSwingHorizontal());

    deviceStatus.setSwingHorizontal("FULL");
    assertEquals("FULL", deviceStatus.getSwingHorizontal());

    deviceStatus.setSwingHorizontal("FIXED_LEFT");
    assertEquals("FIXED_LEFT", deviceStatus.getSwingHorizontal());

    deviceStatus.setSwingHorizontal("FIXED_RIGHT");
    assertEquals("FIXED_RIGHT", deviceStatus.getSwingHorizontal());

    // Test case sensitivity
    deviceStatus.setSwingHorizontal("full");
    assertEquals("full", deviceStatus.getSwingHorizontal());

    deviceStatus.setSwingHorizontal("Fixed_Left");
    assertEquals("Fixed_Left", deviceStatus.getSwingHorizontal());

    // Test setting to null
    deviceStatus.setSwingHorizontal(null);
    assertNull(deviceStatus.getSwingHorizontal());
  }

  @Test
  void testSwingVerticalProperty() {
    // Test valid swing vertical values
    deviceStatus.setSwingVertical("DEFAULT");
    assertEquals("DEFAULT", deviceStatus.getSwingVertical());

    deviceStatus.setSwingVertical("FULL");
    assertEquals("FULL", deviceStatus.getSwingVertical());

    deviceStatus.setSwingVertical("FIXED_TOP");
    assertEquals("FIXED_TOP", deviceStatus.getSwingVertical());

    deviceStatus.setSwingVertical("FIXED_BOTTOM");
    assertEquals("FIXED_BOTTOM", deviceStatus.getSwingVertical());

    // Test case sensitivity
    deviceStatus.setSwingVertical("full");
    assertEquals("full", deviceStatus.getSwingVertical());

    deviceStatus.setSwingVertical("Fixed_Top");
    assertEquals("Fixed_Top", deviceStatus.getSwingVertical());

    // Test setting to null
    deviceStatus.setSwingVertical(null);
    assertNull(deviceStatus.getSwingVertical());
  }

  @Test
  void testLightsProperty() {
    // Test setting lights to true
    deviceStatus.setLights(true);
    assertTrue(deviceStatus.getLights());

    // Test setting lights to false
    deviceStatus.setLights(false);
    assertFalse(deviceStatus.getLights());

    // Test setting lights to null
    deviceStatus.setLights(null);
    assertNull(deviceStatus.getLights());
  }

  @Test
  void testTurboProperty() {
    // Test setting turbo to true
    deviceStatus.setTurbo(true);
    assertTrue(deviceStatus.getTurbo());

    // Test setting turbo to false
    deviceStatus.setTurbo(false);
    assertFalse(deviceStatus.getTurbo());

    // Test setting turbo to null
    deviceStatus.setTurbo(null);
    assertNull(deviceStatus.getTurbo());
  }

  @Test
  void testQuietProperty() {
    // Test setting quiet to true
    deviceStatus.setQuiet(true);
    assertTrue(deviceStatus.getQuiet());

    // Test setting quiet to false
    deviceStatus.setQuiet(false);
    assertFalse(deviceStatus.getQuiet());

    // Test setting quiet to null
    deviceStatus.setQuiet(null);
    assertNull(deviceStatus.getQuiet());
  }

  @Test
  void testHealthProperty() {
    // Test setting health to true
    deviceStatus.setHealth(true);
    assertTrue(deviceStatus.getHealth());

    // Test setting health to false
    deviceStatus.setHealth(false);
    assertFalse(deviceStatus.getHealth());

    // Test setting health to null
    deviceStatus.setHealth(null);
    assertNull(deviceStatus.getHealth());
  }

  @Test
  void testPowerSaveProperty() {
    // Test setting power save to true
    deviceStatus.setPowerSave(true);
    assertTrue(deviceStatus.getPowerSave());

    // Test setting power save to false
    deviceStatus.setPowerSave(false);
    assertFalse(deviceStatus.getPowerSave());

    // Test setting power save to null
    deviceStatus.setPowerSave(null);
    assertNull(deviceStatus.getPowerSave());
  }

  @Test
  void testSleepProperty() {
    // Test setting sleep to true
    deviceStatus.setSleep(true);
    assertTrue(deviceStatus.getSleep());

    // Test setting sleep to false
    deviceStatus.setSleep(false);
    assertFalse(deviceStatus.getSleep());

    // Test setting sleep to null
    deviceStatus.setSleep(null);
    assertNull(deviceStatus.getSleep());
  }

  @Test
  void testCompleteDeviceStatus() {
    // Test setting all properties
    deviceStatus.setDeviceId("test-device-123");
    deviceStatus.setPower(true);
    deviceStatus.setTemperature(22);
    deviceStatus.setCurrentTemperature(23);
    deviceStatus.setMode("COOL");
    deviceStatus.setFanSpeed("HIGH");
    deviceStatus.setSwingHorizontal("FULL");
    deviceStatus.setSwingVertical("FULL");
    deviceStatus.setLights(true);
    deviceStatus.setTurbo(false);
    deviceStatus.setQuiet(true);
    deviceStatus.setHealth(false);
    deviceStatus.setPowerSave(true);
    deviceStatus.setSleep(false);

    // Verify all properties
    assertEquals("test-device-123", deviceStatus.getDeviceId());
    assertTrue(deviceStatus.getPower());
    assertEquals(22, deviceStatus.getTemperature());
    assertEquals(23, deviceStatus.getCurrentTemperature());
    assertEquals("COOL", deviceStatus.getMode());
    assertEquals("HIGH", deviceStatus.getFanSpeed());
    assertEquals("FULL", deviceStatus.getSwingHorizontal());
    assertEquals("FULL", deviceStatus.getSwingVertical());
    assertTrue(deviceStatus.getLights());
    assertFalse(deviceStatus.getTurbo());
    assertTrue(deviceStatus.getQuiet());
    assertFalse(deviceStatus.getHealth());
    assertTrue(deviceStatus.getPowerSave());
    assertFalse(deviceStatus.getSleep());
  }

  @Test
  void testTemperatureRangeValidation() {
    // Test temperature within valid range (16-30)
    deviceStatus.setTemperature(16);
    assertEquals(16, deviceStatus.getTemperature());

    deviceStatus.setTemperature(30);
    assertEquals(30, deviceStatus.getTemperature());

    deviceStatus.setTemperature(22);
    assertEquals(22, deviceStatus.getTemperature());

    // Test edge cases (these should still work as the DTO doesn't enforce validation)
    deviceStatus.setTemperature(15);
    assertEquals(15, deviceStatus.getTemperature());

    deviceStatus.setTemperature(31);
    assertEquals(31, deviceStatus.getTemperature());

    deviceStatus.setTemperature(0);
    assertEquals(0, deviceStatus.getTemperature());

    deviceStatus.setTemperature(100);
    assertEquals(100, deviceStatus.getTemperature());
  }

  @Test
  void testCurrentTemperatureRangeValidation() {
    // Test current temperature within valid range (16-30)
    deviceStatus.setCurrentTemperature(16);
    assertEquals(16, deviceStatus.getCurrentTemperature());

    deviceStatus.setCurrentTemperature(30);
    assertEquals(30, deviceStatus.getCurrentTemperature());

    deviceStatus.setCurrentTemperature(22);
    assertEquals(22, deviceStatus.getCurrentTemperature());

    // Test edge cases (these should still work as the DTO doesn't enforce validation)
    deviceStatus.setCurrentTemperature(15);
    assertEquals(15, deviceStatus.getCurrentTemperature());

    deviceStatus.setCurrentTemperature(31);
    assertEquals(31, deviceStatus.getCurrentTemperature());

    deviceStatus.setCurrentTemperature(0);
    assertEquals(0, deviceStatus.getCurrentTemperature());

    deviceStatus.setCurrentTemperature(100);
    assertEquals(100, deviceStatus.getCurrentTemperature());
  }

  @Test
  void testModeValidation() {
    // Test valid modes
    String[] validModes = {"AUTO", "COOL", "HEAT", "DRY", "FAN_ONLY"};
    for (String mode : validModes) {
      deviceStatus.setMode(mode);
      assertEquals(mode, deviceStatus.getMode());
    }

    // Test invalid modes (should still work as DTO doesn't enforce validation)
    String[] invalidModes = {"INVALID", "TEST", "MODE", "", "123"};
    for (String mode : invalidModes) {
      deviceStatus.setMode(mode);
      assertEquals(mode, deviceStatus.getMode());
    }
  }

  @Test
  void testFanSpeedValidation() {
    // Test valid fan speeds
    String[] validFanSpeeds = {"AUTO", "LOW", "MEDIUM", "HIGH"};
    for (String fanSpeed : validFanSpeeds) {
      deviceStatus.setFanSpeed(fanSpeed);
      assertEquals(fanSpeed, deviceStatus.getFanSpeed());
    }

    // Test invalid fan speeds (should still work as DTO doesn't enforce validation)
    String[] invalidFanSpeeds = {"INVALID", "TEST", "SPEED", "", "123"};
    for (String fanSpeed : invalidFanSpeeds) {
      deviceStatus.setFanSpeed(fanSpeed);
      assertEquals(fanSpeed, deviceStatus.getFanSpeed());
    }
  }

  @Test
  void testSwingValidation() {
    // Test valid horizontal swings
    String[] validHorizontalSwings = {"DEFAULT", "FULL", "FIXED_LEFT", "FIXED_RIGHT"};
    for (String swing : validHorizontalSwings) {
      deviceStatus.setSwingHorizontal(swing);
      assertEquals(swing, deviceStatus.getSwingHorizontal());
    }

    // Test valid vertical swings
    String[] validVerticalSwings = {"DEFAULT", "FULL", "FIXED_TOP", "FIXED_BOTTOM"};
    for (String swing : validVerticalSwings) {
      deviceStatus.setSwingVertical(swing);
      assertEquals(swing, deviceStatus.getSwingVertical());
    }

    // Test invalid swings (should still work as DTO doesn't enforce validation)
    String[] invalidSwings = {"INVALID", "TEST", "SWING", "", "123"};
    for (String swing : invalidSwings) {
      deviceStatus.setSwingHorizontal(swing);
      assertEquals(swing, deviceStatus.getSwingHorizontal());

      deviceStatus.setSwingVertical(swing);
      assertEquals(swing, deviceStatus.getSwingVertical());
    }
  }

  @Test
  void testBooleanPropertiesEdgeCases() {
    // Test all boolean properties with various values
    Boolean[] booleanValues = {true, false, null};

    for (Boolean value : booleanValues) {
      deviceStatus.setPower(value);
      assertEquals(value, deviceStatus.getPower());

      deviceStatus.setLights(value);
      assertEquals(value, deviceStatus.getLights());

      deviceStatus.setTurbo(value);
      assertEquals(value, deviceStatus.getTurbo());

      deviceStatus.setQuiet(value);
      assertEquals(value, deviceStatus.getQuiet());

      deviceStatus.setHealth(value);
      assertEquals(value, deviceStatus.getHealth());

      deviceStatus.setPowerSave(value);
      assertEquals(value, deviceStatus.getPowerSave());

      deviceStatus.setSleep(value);
      assertEquals(value, deviceStatus.getSleep());
    }
  }

  @Test
  void testStringPropertiesEdgeCases() {
    // Test string properties with various values
    String[] stringValues = {"", " ", "test", "123", "!@#$%", null};

    for (String value : stringValues) {
      deviceStatus.setDeviceId(value);
      assertEquals(value, deviceStatus.getDeviceId());

      deviceStatus.setMode(value);
      assertEquals(value, deviceStatus.getMode());

      deviceStatus.setFanSpeed(value);
      assertEquals(value, deviceStatus.getFanSpeed());

      deviceStatus.setSwingHorizontal(value);
      assertEquals(value, deviceStatus.getSwingHorizontal());

      deviceStatus.setSwingVertical(value);
      assertEquals(value, deviceStatus.getSwingVertical());
    }
  }

  @Test
  void testIntegerPropertiesEdgeCases() {
    // Test integer properties with various values
    Integer[] integerValues = {Integer.MIN_VALUE, -100, -1, 0, 1, 100, Integer.MAX_VALUE, null};

    for (Integer value : integerValues) {
      deviceStatus.setTemperature(value);
      assertEquals(value, deviceStatus.getTemperature());

      deviceStatus.setCurrentTemperature(value);
      assertEquals(value, deviceStatus.getCurrentTemperature());
    }
  }

  @Test
  void testTemperatureConsistency() {
    // Test that temperature and currentTemperature can be set independently
    deviceStatus.setTemperature(22);
    deviceStatus.setCurrentTemperature(23);

    assertEquals(22, deviceStatus.getTemperature());
    assertEquals(23, deviceStatus.getCurrentTemperature());

    // Change one without affecting the other
    deviceStatus.setTemperature(25);
    assertEquals(25, deviceStatus.getTemperature());
    assertEquals(23, deviceStatus.getCurrentTemperature());

    deviceStatus.setCurrentTemperature(26);
    assertEquals(25, deviceStatus.getTemperature());
    assertEquals(26, deviceStatus.getCurrentTemperature());
  }

  @Test
  void testDeviceIdFormatVariations() {
    // Test various device ID formats
    String[] deviceIds = {
      "MAC:AA:BB:CC:DD:EE:FF",
      "192.168.1.100",
      "test-device-123",
      "GREE-AC-001",
      "device_123",
      "DEVICE-123",
      "1234567890",
      "!@#$%^&*()",
      ""
    };

    for (String deviceId : deviceIds) {
      deviceStatus.setDeviceId(deviceId);
      assertEquals(deviceId, deviceStatus.getDeviceId());
    }
  }
}
