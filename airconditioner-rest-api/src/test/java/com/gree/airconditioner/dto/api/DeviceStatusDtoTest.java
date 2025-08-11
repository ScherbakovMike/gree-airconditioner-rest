package com.gree.airconditioner.dto.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DeviceStatusDto Tests")
class DeviceStatusDtoTest {

  @Test
  @DisplayName("Should create DeviceStatusDto with all properties")
  void shouldCreateDeviceStatusDtoWithAllProperties() {
    DeviceStatusDto dto = new DeviceStatusDto();

    // Set all properties
    dto.setDeviceId("device-001");
    dto.setPower(true);
    dto.setTemperature(22);
    dto.setCurrentTemperature(24);
    dto.setMode("COOL");
    dto.setFanSpeed("AUTO");
    dto.setSwingHorizontal("DEFAULT");
    dto.setSwingVertical("DEFAULT");
    dto.setLights(true);
    dto.setTurbo(false);
    dto.setQuiet(false);
    dto.setHealth(false);
    dto.setPowerSave(false);
    dto.setSleep(false);

    // Verify all properties
    assertEquals("device-001", dto.getDeviceId());
    assertTrue(dto.isPower());
    assertEquals(22, dto.getTemperature());
    assertEquals(24, dto.getCurrentTemperature());
    assertEquals("COOL", dto.getMode());
    assertEquals("AUTO", dto.getFanSpeed());
    assertEquals("DEFAULT", dto.getSwingHorizontal());
    assertEquals("DEFAULT", dto.getSwingVertical());
    assertTrue(dto.isLights());
    assertFalse(dto.isTurbo());
    assertFalse(dto.isQuiet());
    assertFalse(dto.isHealth());
    assertFalse(dto.isPowerSave());
    assertFalse(dto.isSleep());
  }

  @Test
  @DisplayName("Should handle null values")
  void shouldHandleNullValues() {
    DeviceStatusDto dto = new DeviceStatusDto();

    dto.setDeviceId(null);
    dto.setTemperature(null);
    dto.setCurrentTemperature(null);
    dto.setMode(null);
    dto.setFanSpeed(null);
    dto.setSwingHorizontal(null);
    dto.setSwingVertical(null);

    assertNull(dto.getDeviceId());
    assertNull(dto.getTemperature());
    assertNull(dto.getCurrentTemperature());
    assertNull(dto.getMode());
    assertNull(dto.getFanSpeed());
    assertNull(dto.getSwingHorizontal());
    assertNull(dto.getSwingVertical());

    // Boolean properties default to false
    assertFalse(dto.isPower());
    assertFalse(dto.isLights());
    assertFalse(dto.isTurbo());
    assertFalse(dto.isQuiet());
    assertFalse(dto.isHealth());
    assertFalse(dto.isPowerSave());
    assertFalse(dto.isSleep());
  }

  @Test
  @DisplayName("Should handle empty strings")
  void shouldHandleEmptyStrings() {
    DeviceStatusDto dto = new DeviceStatusDto();

    dto.setDeviceId("");
    dto.setMode("");
    dto.setFanSpeed("");
    dto.setSwingHorizontal("");
    dto.setSwingVertical("");

    assertEquals("", dto.getDeviceId());
    assertEquals("", dto.getMode());
    assertEquals("", dto.getFanSpeed());
    assertEquals("", dto.getSwingHorizontal());
    assertEquals("", dto.getSwingVertical());
  }

  @Test
  @DisplayName("Should handle boolean properties")
  void shouldHandleBooleanProperties() {
    DeviceStatusDto dto = new DeviceStatusDto();

    // Test power
    dto.setPower(true);
    assertTrue(dto.isPower());
    dto.setPower(false);
    assertFalse(dto.isPower());

    // Test lights
    dto.setLights(true);
    assertTrue(dto.isLights());
    dto.setLights(false);
    assertFalse(dto.isLights());

    // Test turbo
    dto.setTurbo(true);
    assertTrue(dto.isTurbo());
    dto.setTurbo(false);
    assertFalse(dto.isTurbo());

    // Test quiet
    dto.setQuiet(true);
    assertTrue(dto.isQuiet());
    dto.setQuiet(false);
    assertFalse(dto.isQuiet());

    // Test health
    dto.setHealth(true);
    assertTrue(dto.isHealth());
    dto.setHealth(false);
    assertFalse(dto.isHealth());

    // Test powerSave
    dto.setPowerSave(true);
    assertTrue(dto.isPowerSave());
    dto.setPowerSave(false);
    assertFalse(dto.isPowerSave());

    // Test sleep
    dto.setSleep(true);
    assertTrue(dto.isSleep());
    dto.setSleep(false);
    assertFalse(dto.isSleep());
  }

  @Test
  @DisplayName("Should handle temperature values")
  void shouldHandleTemperatureValues() {
    DeviceStatusDto dto = new DeviceStatusDto();

    // Test target temperature
    dto.setTemperature(16);
    assertEquals(16, dto.getTemperature());

    dto.setTemperature(30);
    assertEquals(30, dto.getTemperature());

    dto.setTemperature(22);
    assertEquals(22, dto.getTemperature());

    // Test current temperature
    dto.setCurrentTemperature(18);
    assertEquals(18, dto.getCurrentTemperature());

    dto.setCurrentTemperature(28);
    assertEquals(28, dto.getCurrentTemperature());

    dto.setCurrentTemperature(25);
    assertEquals(25, dto.getCurrentTemperature());
  }

  @Test
  @DisplayName("Should handle edge case temperature values")
  void shouldHandleEdgeCaseTemperatureValues() {
    DeviceStatusDto dto = new DeviceStatusDto();

    // Test extreme values
    dto.setTemperature(-100);
    assertEquals(-100, dto.getTemperature());

    dto.setTemperature(100);
    assertEquals(100, dto.getTemperature());

    dto.setTemperature(0);
    assertEquals(0, dto.getTemperature());

    dto.setCurrentTemperature(-50);
    assertEquals(-50, dto.getCurrentTemperature());

    dto.setCurrentTemperature(50);
    assertEquals(50, dto.getCurrentTemperature());
  }

  @Test
  @DisplayName("Should handle valid operation modes")
  void shouldHandleValidOperationModes() {
    DeviceStatusDto dto = new DeviceStatusDto();

    String[] validModes = {"AUTO", "COOL", "HEAT", "DRY", "FAN_ONLY"};

    for (String mode : validModes) {
      dto.setMode(mode);
      assertEquals(mode, dto.getMode());
    }
  }

  @Test
  @DisplayName("Should handle valid fan speeds")
  void shouldHandleValidFanSpeeds() {
    DeviceStatusDto dto = new DeviceStatusDto();

    String[] validFanSpeeds = {"AUTO", "LOW", "MEDIUM", "HIGH"};

    for (String fanSpeed : validFanSpeeds) {
      dto.setFanSpeed(fanSpeed);
      assertEquals(fanSpeed, dto.getFanSpeed());
    }
  }

  @Test
  @DisplayName("Should handle valid swing values")
  void shouldHandleValidSwingValues() {
    DeviceStatusDto dto = new DeviceStatusDto();

    String[] validSwingHorizontal = {"DEFAULT", "FULL", "FIXED_LEFT", "FIXED_RIGHT"};
    String[] validSwingVertical = {"DEFAULT", "FULL", "FIXED_TOP", "FIXED_BOTTOM"};

    for (String swing : validSwingHorizontal) {
      dto.setSwingHorizontal(swing);
      assertEquals(swing, dto.getSwingHorizontal());
    }

    for (String swing : validSwingVertical) {
      dto.setSwingVertical(swing);
      assertEquals(swing, dto.getSwingVertical());
    }
  }

  @Test
  @DisplayName("Should handle case sensitivity in mode and fan speed")
  void shouldHandleCaseSensitivityInModeAndFanSpeed() {
    DeviceStatusDto dto = new DeviceStatusDto();

    dto.setMode("cool");
    assertEquals("cool", dto.getMode());

    dto.setFanSpeed("auto");
    assertEquals("auto", dto.getFanSpeed());

    dto.setSwingHorizontal("default");
    assertEquals("default", dto.getSwingHorizontal());

    dto.setSwingVertical("full");
    assertEquals("full", dto.getSwingVertical());
  }

  @Test
  @DisplayName("Should have proper toString representation")
  void shouldHaveProperToStringRepresentation() {
    DeviceStatusDto dto = new DeviceStatusDto();
    dto.setDeviceId("test-device");
    dto.setPower(true);
    dto.setTemperature(22);
    dto.setMode("COOL");

    String toString = dto.toString();

    assertNotNull(toString);
    assertTrue(toString.contains("test-device"));
    assertTrue(toString.contains("true"));
    assertTrue(toString.contains("22"));
    assertTrue(toString.contains("COOL"));
  }

  @Test
  @DisplayName("Should have proper equals and hashCode")
  void shouldHaveProperEqualsAndHashCode() {
    DeviceStatusDto dto1 = new DeviceStatusDto();
    dto1.setDeviceId("device-001");
    dto1.setPower(true);
    dto1.setTemperature(22);

    DeviceStatusDto dto2 = new DeviceStatusDto();
    dto2.setDeviceId("device-001");
    dto2.setPower(true);
    dto2.setTemperature(22);

    DeviceStatusDto dto3 = new DeviceStatusDto();
    dto3.setDeviceId("device-002");
    dto3.setPower(true);
    dto3.setTemperature(22);

    assertEquals(dto1, dto2);
    assertNotEquals(dto1, dto3);
    assertEquals(dto1.hashCode(), dto2.hashCode());
    assertNotEquals(dto1.hashCode(), dto3.hashCode());
  }

  @Test
  @DisplayName("Should handle special characters in strings")
  void shouldHandleSpecialCharactersInStrings() {
    DeviceStatusDto dto = new DeviceStatusDto();

    dto.setDeviceId("AC-Unit (Living Room)");
    dto.setMode("COOL & DRY");
    dto.setFanSpeed("AUTO-MODE");

    assertEquals("AC-Unit (Living Room)", dto.getDeviceId());
    assertEquals("COOL & DRY", dto.getMode());
    assertEquals("AUTO-MODE", dto.getFanSpeed());
  }
}
