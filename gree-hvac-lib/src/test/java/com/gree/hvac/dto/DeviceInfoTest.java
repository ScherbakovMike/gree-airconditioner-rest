package com.gree.hvac.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class DeviceInfoTest {

  @Test
  void testDefaultConstructor() {
    DeviceInfo deviceInfo = new DeviceInfo();

    assertNull(deviceInfo.getId());
    assertNull(deviceInfo.getName());
    assertNull(deviceInfo.getBrand());
    assertNull(deviceInfo.getModel());
    assertNull(deviceInfo.getVersion());
    assertNull(deviceInfo.getMacAddress());
    assertNull(deviceInfo.getIpAddress());
    assertFalse(deviceInfo.isConnected());
    assertNull(deviceInfo.getStatus());
  }

  @Test
  void testSetters() {
    DeviceInfo deviceInfo = new DeviceInfo();

    deviceInfo.setId("device123");
    deviceInfo.setName("Living Room AC");
    deviceInfo.setBrand("Gree");
    deviceInfo.setModel("GREE-AC-2023");
    deviceInfo.setVersion("v2.1.0");
    deviceInfo.setMacAddress("AA:BB:CC:DD:EE:FF");
    deviceInfo.setIpAddress("192.168.1.100");
    deviceInfo.setConnected(true);
    deviceInfo.setStatus("online");

    assertEquals("device123", deviceInfo.getId());
    assertEquals("Living Room AC", deviceInfo.getName());
    assertEquals("Gree", deviceInfo.getBrand());
    assertEquals("GREE-AC-2023", deviceInfo.getModel());
    assertEquals("v2.1.0", deviceInfo.getVersion());
    assertEquals("AA:BB:CC:DD:EE:FF", deviceInfo.getMacAddress());
    assertEquals("192.168.1.100", deviceInfo.getIpAddress());
    assertTrue(deviceInfo.isConnected());
    assertEquals("online", deviceInfo.getStatus());
  }

  @Test
  void testToString() {
    DeviceInfo deviceInfo = new DeviceInfo();
    deviceInfo.setId("device123");
    deviceInfo.setName("Living Room AC");
    deviceInfo.setBrand("Gree");
    deviceInfo.setModel("GREE-AC-2023");
    deviceInfo.setVersion("v2.1.0");
    deviceInfo.setMacAddress("AA:BB:CC:DD:EE:FF");
    deviceInfo.setIpAddress("192.168.1.100");
    deviceInfo.setConnected(true);
    deviceInfo.setStatus("online");

    String result = deviceInfo.toString();

    assertTrue(result.contains("DeviceInfo"));
    assertTrue(result.contains("id=device123"));
    assertTrue(result.contains("name=Living Room AC"));
    assertTrue(result.contains("brand=Gree"));
    assertTrue(result.contains("model=GREE-AC-2023"));
    assertTrue(result.contains("version=v2.1.0"));
    assertTrue(result.contains("macAddress=AA:BB:CC:DD:EE:FF"));
    assertTrue(result.contains("ipAddress=192.168.1.100"));
    assertTrue(result.contains("connected=true"));
    assertTrue(result.contains("status=online"));
  }

  @Test
  void testToStringWithNullValues() {
    DeviceInfo deviceInfo = new DeviceInfo();

    String result = deviceInfo.toString();

    assertTrue(result.contains("DeviceInfo"));
    assertTrue(result.contains("id=null"));
    assertTrue(result.contains("name=null"));
    assertTrue(result.contains("brand=null"));
    assertTrue(result.contains("model=null"));
    assertTrue(result.contains("version=null"));
    assertTrue(result.contains("macAddress=null"));
    assertTrue(result.contains("ipAddress=null"));
    assertTrue(result.contains("connected=false"));
    assertTrue(result.contains("status=null"));
  }

  @Test
  void testEqualsAndHashCode() {
    DeviceInfo deviceInfo1 = new DeviceInfo();
    deviceInfo1.setId("device123");
    deviceInfo1.setName("Living Room AC");
    deviceInfo1.setBrand("Gree");
    deviceInfo1.setModel("GREE-AC-2023");
    deviceInfo1.setVersion("v2.1.0");
    deviceInfo1.setMacAddress("AA:BB:CC:DD:EE:FF");
    deviceInfo1.setIpAddress("192.168.1.100");
    deviceInfo1.setConnected(true);
    deviceInfo1.setStatus("online");

    DeviceInfo deviceInfo2 = new DeviceInfo();
    deviceInfo2.setId("device123");
    deviceInfo2.setName("Living Room AC");
    deviceInfo2.setBrand("Gree");
    deviceInfo2.setModel("GREE-AC-2023");
    deviceInfo2.setVersion("v2.1.0");
    deviceInfo2.setMacAddress("AA:BB:CC:DD:EE:FF");
    deviceInfo2.setIpAddress("192.168.1.100");
    deviceInfo2.setConnected(true);
    deviceInfo2.setStatus("online");

    DeviceInfo deviceInfo3 = new DeviceInfo();
    deviceInfo3.setId("device456");
    deviceInfo3.setName("Living Room AC");
    deviceInfo3.setBrand("Gree");
    deviceInfo3.setModel("GREE-AC-2023");
    deviceInfo3.setVersion("v2.1.0");
    deviceInfo3.setMacAddress("AA:BB:CC:DD:EE:FF");
    deviceInfo3.setIpAddress("192.168.1.100");
    deviceInfo3.setConnected(true);
    deviceInfo3.setStatus("online");

    assertEquals(deviceInfo1, deviceInfo2);
    assertNotEquals(deviceInfo1, deviceInfo3);
    assertEquals(deviceInfo1.hashCode(), deviceInfo2.hashCode());
    assertNotEquals(deviceInfo1.hashCode(), deviceInfo3.hashCode());
  }

  @Test
  void testEqualsWithNull() {
    DeviceInfo deviceInfo = new DeviceInfo();
    deviceInfo.setId("device123");

    assertNotEquals(null, deviceInfo);
  }

  @Test
  void testEqualsWithDifferentClass() {
    DeviceInfo deviceInfo = new DeviceInfo();
    deviceInfo.setId("device123");
    String differentObject = "device123";

    assertNotEquals(deviceInfo, differentObject);
  }

  @Test
  void testEqualsWithDifferentId() {
    DeviceInfo deviceInfo1 = new DeviceInfo();
    deviceInfo1.setId("device123");
    deviceInfo1.setName("Living Room AC");

    DeviceInfo deviceInfo2 = new DeviceInfo();
    deviceInfo2.setId("device456");
    deviceInfo2.setName("Living Room AC");

    assertNotEquals(deviceInfo1, deviceInfo2);
  }

  @Test
  void testEqualsWithDifferentName() {
    DeviceInfo deviceInfo1 = new DeviceInfo();
    deviceInfo1.setId("device123");
    deviceInfo1.setName("Living Room AC");

    DeviceInfo deviceInfo2 = new DeviceInfo();
    deviceInfo2.setId("device123");
    deviceInfo2.setName("Bedroom AC");

    assertNotEquals(deviceInfo1, deviceInfo2);
  }

  @Test
  void testEqualsWithDifferentBrand() {
    DeviceInfo deviceInfo1 = new DeviceInfo();
    deviceInfo1.setId("device123");
    deviceInfo1.setName("Living Room AC");
    deviceInfo1.setBrand("Gree");

    DeviceInfo deviceInfo2 = new DeviceInfo();
    deviceInfo2.setId("device123");
    deviceInfo2.setName("Living Room AC");
    deviceInfo2.setBrand("Other");

    assertNotEquals(deviceInfo1, deviceInfo2);
  }

  @Test
  void testEqualsWithDifferentModel() {
    DeviceInfo deviceInfo1 = new DeviceInfo();
    deviceInfo1.setId("device123");
    deviceInfo1.setName("Living Room AC");
    deviceInfo1.setModel("GREE-AC-2023");

    DeviceInfo deviceInfo2 = new DeviceInfo();
    deviceInfo2.setId("device123");
    deviceInfo2.setName("Living Room AC");
    deviceInfo2.setModel("GREE-AC-2024");

    assertNotEquals(deviceInfo1, deviceInfo2);
  }

  @Test
  void testEqualsWithDifferentVersion() {
    DeviceInfo deviceInfo1 = new DeviceInfo();
    deviceInfo1.setId("device123");
    deviceInfo1.setName("Living Room AC");
    deviceInfo1.setVersion("v2.1.0");

    DeviceInfo deviceInfo2 = new DeviceInfo();
    deviceInfo2.setId("device123");
    deviceInfo2.setName("Living Room AC");
    deviceInfo2.setVersion("v2.2.0");

    assertNotEquals(deviceInfo1, deviceInfo2);
  }

  @Test
  void testEqualsWithDifferentMacAddress() {
    DeviceInfo deviceInfo1 = new DeviceInfo();
    deviceInfo1.setId("device123");
    deviceInfo1.setName("Living Room AC");
    deviceInfo1.setMacAddress("AA:BB:CC:DD:EE:FF");

    DeviceInfo deviceInfo2 = new DeviceInfo();
    deviceInfo2.setId("device123");
    deviceInfo2.setName("Living Room AC");
    deviceInfo2.setMacAddress("FF:EE:DD:CC:BB:AA");

    assertNotEquals(deviceInfo1, deviceInfo2);
  }

  @Test
  void testEqualsWithDifferentIpAddress() {
    DeviceInfo deviceInfo1 = new DeviceInfo();
    deviceInfo1.setId("device123");
    deviceInfo1.setName("Living Room AC");
    deviceInfo1.setIpAddress("192.168.1.100");

    DeviceInfo deviceInfo2 = new DeviceInfo();
    deviceInfo2.setId("device123");
    deviceInfo2.setName("Living Room AC");
    deviceInfo2.setIpAddress("192.168.1.101");

    assertNotEquals(deviceInfo1, deviceInfo2);
  }

  @Test
  void testEqualsWithDifferentConnectedStatus() {
    DeviceInfo deviceInfo1 = new DeviceInfo();
    deviceInfo1.setId("device123");
    deviceInfo1.setName("Living Room AC");
    deviceInfo1.setConnected(true);

    DeviceInfo deviceInfo2 = new DeviceInfo();
    deviceInfo2.setId("device123");
    deviceInfo2.setName("Living Room AC");
    deviceInfo2.setConnected(false);

    assertNotEquals(deviceInfo1, deviceInfo2);
  }

  @Test
  void testEqualsWithDifferentStatus() {
    DeviceInfo deviceInfo1 = new DeviceInfo();
    deviceInfo1.setId("device123");
    deviceInfo1.setName("Living Room AC");
    deviceInfo1.setStatus("online");

    DeviceInfo deviceInfo2 = new DeviceInfo();
    deviceInfo2.setId("device123");
    deviceInfo2.setName("Living Room AC");
    deviceInfo2.setStatus("offline");

    assertNotEquals(deviceInfo1, deviceInfo2);
  }

  @Test
  void testEqualsWithNullValues() {
    DeviceInfo deviceInfo1 = new DeviceInfo();
    deviceInfo1.setId("device123");

    DeviceInfo deviceInfo2 = new DeviceInfo();
    deviceInfo2.setId("device123");

    assertEquals(deviceInfo1, deviceInfo2);
    assertEquals(deviceInfo1.hashCode(), deviceInfo2.hashCode());
  }

  @Test
  void testEqualsWithMixedNullValues() {
    DeviceInfo deviceInfo1 = new DeviceInfo();
    deviceInfo1.setId("device123");
    deviceInfo1.setName("Living Room AC");
    deviceInfo1.setBrand("Gree");
    deviceInfo1.setModel(null);
    deviceInfo1.setVersion("v2.1.0");
    deviceInfo1.setMacAddress(null);
    deviceInfo1.setIpAddress("192.168.1.100");
    deviceInfo1.setConnected(true);
    deviceInfo1.setStatus(null);

    DeviceInfo deviceInfo2 = new DeviceInfo();
    deviceInfo2.setId("device123");
    deviceInfo2.setName("Living Room AC");
    deviceInfo2.setBrand("Gree");
    deviceInfo2.setModel(null);
    deviceInfo2.setVersion("v2.1.0");
    deviceInfo2.setMacAddress(null);
    deviceInfo2.setIpAddress("192.168.1.100");
    deviceInfo2.setConnected(true);
    deviceInfo2.setStatus(null);

    assertEquals(deviceInfo1, deviceInfo2);
    assertEquals(deviceInfo1.hashCode(), deviceInfo2.hashCode());
  }

  @Test
  void testLombokGeneratedMethods() {
    DeviceInfo deviceInfo = new DeviceInfo();

    // Test that Lombok generates all the necessary methods
    assertNotNull(deviceInfo.toString());
    assertNotNull(deviceInfo.hashCode());

    // Test that equals works with itself
    assertEquals(deviceInfo, deviceInfo);

    // Test that equals works with null
    assertNotEquals(null, deviceInfo);

    // Test that equals works with different class
    assertNotEquals("string", deviceInfo);
  }
}
