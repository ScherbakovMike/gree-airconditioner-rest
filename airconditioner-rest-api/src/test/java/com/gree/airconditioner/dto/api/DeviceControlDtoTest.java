package com.gree.airconditioner.dto.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DeviceControlDto Tests")
class DeviceControlDtoTest {

  @Test
  @DisplayName("Should create DeviceControlDto with all properties")
  void shouldCreateDeviceControlDtoWithAllProperties() {
    DeviceControlDto dto = new DeviceControlDto();

    // Set all properties
    dto.setPower(true);
    dto.setTemperature(22);
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
    assertTrue(dto.getPower());
    assertEquals(22, dto.getTemperature());
    assertEquals("COOL", dto.getMode());
    assertEquals("AUTO", dto.getFanSpeed());
    assertEquals("DEFAULT", dto.getSwingHorizontal());
    assertEquals("DEFAULT", dto.getSwingVertical());
    assertTrue(dto.getLights());
    assertFalse(dto.getTurbo());
    assertFalse(dto.getQuiet());
    assertFalse(dto.getHealth());
    assertFalse(dto.getPowerSave());
    assertFalse(dto.getSleep());
  }

  @Test
  @DisplayName("Should handle null values")
  void shouldHandleNullValues() {
    DeviceControlDto dto = new DeviceControlDto();

    dto.setPower(null);
    dto.setTemperature(null);
    dto.setMode(null);
    dto.setFanSpeed(null);
    dto.setSwingHorizontal(null);
    dto.setSwingVertical(null);
    dto.setLights(null);
    dto.setTurbo(null);
    dto.setQuiet(null);
    dto.setHealth(null);
    dto.setPowerSave(null);
    dto.setSleep(null);

    assertNull(dto.getPower());
    assertNull(dto.getTemperature());
    assertNull(dto.getMode());
    assertNull(dto.getFanSpeed());
    assertNull(dto.getSwingHorizontal());
    assertNull(dto.getSwingVertical());
    assertNull(dto.getLights());
    assertNull(dto.getTurbo());
    assertNull(dto.getQuiet());
    assertNull(dto.getHealth());
    assertNull(dto.getPowerSave());
    assertNull(dto.getSleep());
  }

  @Test
  @DisplayName("Should handle temperature boundary values")
  void shouldHandleTemperatureBoundaryValues() {
    DeviceControlDto dto = new DeviceControlDto();

    // Test minimum temperature
    dto.setTemperature(16);
    assertEquals(16, dto.getTemperature());

    // Test maximum temperature
    dto.setTemperature(30);
    assertEquals(30, dto.getTemperature());

    // Test middle temperature
    dto.setTemperature(23);
    assertEquals(23, dto.getTemperature());
  }

  @Test
  @DisplayName("Should handle valid operation modes")
  void shouldHandleValidOperationModes() {
    DeviceControlDto dto = new DeviceControlDto();

    String[] validModes = {"AUTO", "COOL", "HEAT", "DRY", "FAN_ONLY"};

    for (String mode : validModes) {
      dto.setMode(mode);
      assertEquals(mode, dto.getMode());
    }
  }

  @Test
  @DisplayName("Should handle valid fan speeds")
  void shouldHandleValidFanSpeeds() {
    DeviceControlDto dto = new DeviceControlDto();

    String[] validFanSpeeds = {"AUTO", "LOW", "MEDIUM", "HIGH"};

    for (String fanSpeed : validFanSpeeds) {
      dto.setFanSpeed(fanSpeed);
      assertEquals(fanSpeed, dto.getFanSpeed());
    }
  }

  @Test
  @DisplayName("Should handle valid horizontal swing values")
  void shouldHandleValidHorizontalSwingValues() {
    DeviceControlDto dto = new DeviceControlDto();

    String[] validSwingHorizontal = {"DEFAULT", "FULL", "FIXED_LEFT", "FIXED_RIGHT"};

    for (String swing : validSwingHorizontal) {
      dto.setSwingHorizontal(swing);
      assertEquals(swing, dto.getSwingHorizontal());
    }
  }

  @Test
  @DisplayName("Should handle valid vertical swing values")
  void shouldHandleValidVerticalSwingValues() {
    DeviceControlDto dto = new DeviceControlDto();

    String[] validSwingVertical = {"DEFAULT", "FULL", "FIXED_TOP", "FIXED_BOTTOM"};

    for (String swing : validSwingVertical) {
      dto.setSwingVertical(swing);
      assertEquals(swing, dto.getSwingVertical());
    }
  }

  @Test
  @DisplayName("Should handle boolean properties")
  void shouldHandleBooleanProperties() {
    DeviceControlDto dto = new DeviceControlDto();

    // Test power
    dto.setPower(true);
    assertTrue(dto.getPower());
    dto.setPower(false);
    assertFalse(dto.getPower());

    // Test lights
    dto.setLights(true);
    assertTrue(dto.getLights());
    dto.setLights(false);
    assertFalse(dto.getLights());

    // Test turbo
    dto.setTurbo(true);
    assertTrue(dto.getTurbo());
    dto.setTurbo(false);
    assertFalse(dto.getTurbo());

    // Test quiet
    dto.setQuiet(true);
    assertTrue(dto.getQuiet());
    dto.setQuiet(false);
    assertFalse(dto.getQuiet());

    // Test health
    dto.setHealth(true);
    assertTrue(dto.getHealth());
    dto.setHealth(false);
    assertFalse(dto.getHealth());

    // Test powerSave
    dto.setPowerSave(true);
    assertTrue(dto.getPowerSave());
    dto.setPowerSave(false);
    assertFalse(dto.getPowerSave());

    // Test sleep
    dto.setSleep(true);
    assertTrue(dto.getSleep());
    dto.setSleep(false);
    assertFalse(dto.getSleep());
  }

  @Test
  @DisplayName("Should handle case sensitivity in mode and fan speed")
  void shouldHandleCaseSensitivityInModeAndFanSpeed() {
    DeviceControlDto dto = new DeviceControlDto();

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
    DeviceControlDto dto = new DeviceControlDto();
    dto.setPower(true);
    dto.setTemperature(22);
    dto.setMode("COOL");

    String toString = dto.toString();

    assertNotNull(toString);
    assertTrue(toString.contains("true"));
    assertTrue(toString.contains("22"));
    assertTrue(toString.contains("COOL"));
  }

  @Test
  @DisplayName("Should have proper equals and hashCode")
  void shouldHaveProperEqualsAndHashCode() {
    DeviceControlDto dto1 = new DeviceControlDto();
    dto1.setPower(true);
    dto1.setTemperature(22);
    dto1.setMode("COOL");

    DeviceControlDto dto2 = new DeviceControlDto();
    dto2.setPower(true);
    dto2.setTemperature(22);
    dto2.setMode("COOL");

    DeviceControlDto dto3 = new DeviceControlDto();
    dto3.setPower(false);
    dto3.setTemperature(22);
    dto3.setMode("COOL");

    assertEquals(dto1, dto2);
    assertNotEquals(dto1, dto3);
    assertEquals(dto1.hashCode(), dto2.hashCode());
    assertNotEquals(dto1.hashCode(), dto3.hashCode());
  }

  @Test
  @DisplayName("Should handle edge case temperature values")
  void shouldHandleEdgeCaseTemperatureValues() {
    DeviceControlDto dto = new DeviceControlDto();

    // Test extreme values (though these might not be valid in practice)
    dto.setTemperature(-100);
    assertEquals(-100, dto.getTemperature());

    dto.setTemperature(100);
    assertEquals(100, dto.getTemperature());

    dto.setTemperature(0);
    assertEquals(0, dto.getTemperature());
  }
}
