package com.gree.airconditioner.dto.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DeviceInfoDto Tests")
class DeviceInfoDtoTest {

  @Test
  @DisplayName("Should create DeviceInfoDto with all properties")
  void shouldCreateDeviceInfoDtoWithAllProperties() {
    DeviceInfoDto dto = new DeviceInfoDto();

    // Set all properties
    dto.setId("device-001");
    dto.setName("Living Room AC");
    dto.setBrand("GREE");
    dto.setModel("KFR-35GW");
    dto.setVersion("1.0.0");
    dto.setMacAddress("AA:BB:CC:DD:EE:FF");
    dto.setIpAddress("192.168.1.100");
    dto.setConnected(true);
    dto.setStatus("Connected");

    // Verify all properties
    assertEquals("device-001", dto.getId());
    assertEquals("Living Room AC", dto.getName());
    assertEquals("GREE", dto.getBrand());
    assertEquals("KFR-35GW", dto.getModel());
    assertEquals("1.0.0", dto.getVersion());
    assertEquals("AA:BB:CC:DD:EE:FF", dto.getMacAddress());
    assertEquals("192.168.1.100", dto.getIpAddress());
    assertTrue(dto.isConnected());
    assertEquals("Connected", dto.getStatus());
  }

  @Test
  @DisplayName("Should handle null values")
  void shouldHandleNullValues() {
    DeviceInfoDto dto = new DeviceInfoDto();

    dto.setId(null);
    dto.setName(null);
    dto.setBrand(null);
    dto.setModel(null);
    dto.setVersion(null);
    dto.setMacAddress(null);
    dto.setIpAddress(null);
    dto.setStatus(null);

    assertNull(dto.getId());
    assertNull(dto.getName());
    assertNull(dto.getBrand());
    assertNull(dto.getModel());
    assertNull(dto.getVersion());
    assertNull(dto.getMacAddress());
    assertNull(dto.getIpAddress());
    assertNull(dto.getStatus());
    assertFalse(dto.isConnected()); // boolean defaults to false
  }

  @Test
  @DisplayName("Should handle empty strings")
  void shouldHandleEmptyStrings() {
    DeviceInfoDto dto = new DeviceInfoDto();

    dto.setId("");
    dto.setName("");
    dto.setBrand("");
    dto.setModel("");
    dto.setVersion("");
    dto.setMacAddress("");
    dto.setIpAddress("");
    dto.setStatus("");

    assertEquals("", dto.getId());
    assertEquals("", dto.getName());
    assertEquals("", dto.getBrand());
    assertEquals("", dto.getModel());
    assertEquals("", dto.getVersion());
    assertEquals("", dto.getMacAddress());
    assertEquals("", dto.getIpAddress());
    assertEquals("", dto.getStatus());
  }

  @Test
  @DisplayName("Should handle boolean connected state")
  void shouldHandleBooleanConnectedState() {
    DeviceInfoDto dto = new DeviceInfoDto();

    dto.setConnected(true);
    assertTrue(dto.isConnected());

    dto.setConnected(false);
    assertFalse(dto.isConnected());
  }

  @Test
  @DisplayName("Should have proper toString representation")
  void shouldHaveProperToStringRepresentation() {
    DeviceInfoDto dto = new DeviceInfoDto();
    dto.setId("test-id");
    dto.setName("Test AC");

    String toString = dto.toString();

    assertNotNull(toString);
    assertTrue(toString.contains("test-id"));
    assertTrue(toString.contains("Test AC"));
  }

  @Test
  @DisplayName("Should have proper equals and hashCode")
  void shouldHaveProperEqualsAndHashCode() {
    DeviceInfoDto dto1 = new DeviceInfoDto();
    dto1.setId("device-001");
    dto1.setName("Test AC");

    DeviceInfoDto dto2 = new DeviceInfoDto();
    dto2.setId("device-001");
    dto2.setName("Test AC");

    DeviceInfoDto dto3 = new DeviceInfoDto();
    dto3.setId("device-002");
    dto3.setName("Test AC");

    assertEquals(dto1, dto2);
    assertNotEquals(dto1, dto3);
    assertEquals(dto1.hashCode(), dto2.hashCode());
    assertNotEquals(dto1.hashCode(), dto3.hashCode());
  }

  @Test
  @DisplayName("Should handle special characters in strings")
  void shouldHandleSpecialCharactersInStrings() {
    DeviceInfoDto dto = new DeviceInfoDto();

    dto.setName("AC Unit (Living Room)");
    dto.setModel("KFR-35GW/01");
    dto.setStatus("Connected & Running");

    assertEquals("AC Unit (Living Room)", dto.getName());
    assertEquals("KFR-35GW/01", dto.getModel());
    assertEquals("Connected & Running", dto.getStatus());
  }
}
