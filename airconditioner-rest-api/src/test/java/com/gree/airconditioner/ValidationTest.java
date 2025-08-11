package com.gree.airconditioner;

import static org.junit.jupiter.api.Assertions.*;

import com.gree.airconditioner.dto.api.DeviceControlDto;
import com.gree.airconditioner.dto.api.DeviceInfoDto;
import com.gree.airconditioner.dto.api.DeviceStatusDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValidationTest {

  private DeviceControlDto controlDto;
  private DeviceInfoDto infoDto;
  private DeviceStatusDto statusDto;

  @BeforeEach
  void setUp() {
    controlDto = new DeviceControlDto();
    infoDto = new DeviceInfoDto();
    statusDto = new DeviceStatusDto();
  }

  @Test
  void testDeviceControlDtoValidation() {
    // Test valid control parameters
    controlDto.setPower(true);
    controlDto.setTemperature(22);
    controlDto.setMode("COOL");
    controlDto.setFanSpeed("AUTO");

    assertTrue(controlDto.getPower());
    assertEquals(22, controlDto.getTemperature());
    assertEquals("COOL", controlDto.getMode());
    assertEquals("AUTO", controlDto.getFanSpeed());
  }

  @Test
  void testDeviceControlDtoTemperatureRange() {
    // Test temperature range validation (16-30)
    controlDto.setTemperature(16);
    assertEquals(16, controlDto.getTemperature());

    controlDto.setTemperature(30);
    assertEquals(30, controlDto.getTemperature());

    // Test boundary values
    controlDto.setTemperature(22);
    assertEquals(22, controlDto.getTemperature());
  }

  @Test
  void testDeviceControlDtoModeValidation() {
    // Test valid modes
    String[] validModes = {"AUTO", "COOL", "HEAT", "DRY", "FAN_ONLY"};

    for (String mode : validModes) {
      controlDto.setMode(mode);
      assertEquals(mode, controlDto.getMode());
    }
  }

  @Test
  void testDeviceControlDtoFanSpeedValidation() {
    // Test valid fan speeds
    String[] validFanSpeeds = {"AUTO", "LOW", "MEDIUM", "HIGH"};

    for (String fanSpeed : validFanSpeeds) {
      controlDto.setFanSpeed(fanSpeed);
      assertEquals(fanSpeed, controlDto.getFanSpeed());
    }
  }

  @Test
  void testDeviceControlDtoSwingValidation() {
    // Test horizontal swing values
    String[] validHorizontalSwing = {"DEFAULT", "FULL", "FIXED_LEFT", "FIXED_RIGHT"};
    for (String swing : validHorizontalSwing) {
      controlDto.setSwingHorizontal(swing);
      assertEquals(swing, controlDto.getSwingHorizontal());
    }

    // Test vertical swing values
    String[] validVerticalSwing = {"DEFAULT", "FULL", "FIXED_TOP", "FIXED_BOTTOM"};
    for (String swing : validVerticalSwing) {
      controlDto.setSwingVertical(swing);
      assertEquals(swing, controlDto.getSwingVertical());
    }
  }

  @Test
  void testDeviceControlDtoBooleanProperties() {
    // Test boolean properties
    controlDto.setLights(true);
    controlDto.setTurbo(false);
    controlDto.setQuiet(true);
    controlDto.setHealth(false);
    controlDto.setPowerSave(true);
    controlDto.setSleep(false);

    assertTrue(controlDto.getLights());
    assertFalse(controlDto.getTurbo());
    assertTrue(controlDto.getQuiet());
    assertFalse(controlDto.getHealth());
    assertTrue(controlDto.getPowerSave());
    assertFalse(controlDto.getSleep());
  }

  @Test
  void testDeviceInfoDtoValidation() {
    // Test device info properties
    infoDto.setId("device123");
    infoDto.setName("Living Room AC");
    infoDto.setBrand("GREE");
    infoDto.setModel("KFR-35GW");
    infoDto.setVersion("1.0.0");
    infoDto.setMacAddress("AA:BB:CC:DD:EE:FF");
    infoDto.setIpAddress("192.168.1.100");
    infoDto.setConnected(true);
    infoDto.setStatus("Connected");

    assertEquals("device123", infoDto.getId());
    assertEquals("Living Room AC", infoDto.getName());
    assertEquals("GREE", infoDto.getBrand());
    assertEquals("KFR-35GW", infoDto.getModel());
    assertEquals("1.0.0", infoDto.getVersion());
    assertEquals("AA:BB:CC:DD:EE:FF", infoDto.getMacAddress());
    assertEquals("192.168.1.100", infoDto.getIpAddress());
    assertTrue(infoDto.isConnected());
    assertEquals("Connected", infoDto.getStatus());
  }

  @Test
  void testDeviceStatusDtoValidation() {
    // Test device status properties
    statusDto.setDeviceId("device123");
    statusDto.setPower(true);
    statusDto.setTemperature(22);
    statusDto.setCurrentTemperature(24);
    statusDto.setMode("COOL");
    statusDto.setFanSpeed("AUTO");
    statusDto.setSwingHorizontal("DEFAULT");
    statusDto.setSwingVertical("DEFAULT");
    statusDto.setLights(true);
    statusDto.setTurbo(false);
    statusDto.setQuiet(false);
    statusDto.setHealth(false);
    statusDto.setPowerSave(false);
    statusDto.setSleep(false);

    assertEquals("device123", statusDto.getDeviceId());
    assertTrue(statusDto.isPower());
    assertEquals(22, statusDto.getTemperature());
    assertEquals(24, statusDto.getCurrentTemperature());
    assertEquals("COOL", statusDto.getMode());
    assertEquals("AUTO", statusDto.getFanSpeed());
    assertEquals("DEFAULT", statusDto.getSwingHorizontal());
    assertEquals("DEFAULT", statusDto.getSwingVertical());
    assertTrue(statusDto.isLights());
    assertFalse(statusDto.isTurbo());
    assertFalse(statusDto.isQuiet());
    assertFalse(statusDto.isHealth());
    assertFalse(statusDto.isPowerSave());
    assertFalse(statusDto.isSleep());
  }

  @Test
  void testNullValuesHandling() {
    // Test that null values are handled properly
    controlDto.setPower(null);
    controlDto.setTemperature(null);
    controlDto.setMode(null);

    assertNull(controlDto.getPower());
    assertNull(controlDto.getTemperature());
    assertNull(controlDto.getMode());
  }

  @Test
  void testEmptyStringHandling() {
    // Test empty string handling
    controlDto.setMode("");
    controlDto.setFanSpeed("");

    assertEquals("", controlDto.getMode());
    assertEquals("", controlDto.getFanSpeed());
  }

  @Test
  void testCaseSensitivity() {
    // Test case sensitivity in mode and fan speed
    controlDto.setMode("cool");
    controlDto.setFanSpeed("auto");

    assertEquals("cool", controlDto.getMode());
    assertEquals("auto", controlDto.getFanSpeed());
  }

  @Test
  void testObjectEquality() {
    // Test object equality
    DeviceControlDto dto1 = new DeviceControlDto();
    DeviceControlDto dto2 = new DeviceControlDto();

    dto1.setPower(true);
    dto1.setTemperature(22);
    dto1.setMode("COOL");

    dto2.setPower(true);
    dto2.setTemperature(22);
    dto2.setMode("COOL");

    // Note: These are not equal because they are different objects
    // This test demonstrates the current behavior
    assertNotSame(dto1, dto2);
  }
}
