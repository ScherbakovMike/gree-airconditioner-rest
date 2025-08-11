package com.gree.hvac.protocol;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PropertyTransformerTest {

  private PropertyTransformer transformer;

  @BeforeEach
  void setUp() {
    transformer = new PropertyTransformer();
  }

  @Test
  void testFromVendorBasicProperties() {
    Map<String, Object> vendorProperties = new HashMap<>();
    vendorProperties.put("Pow", 1);
    vendorProperties.put("Mod", 2);
    vendorProperties.put("SetTem", 25);
    vendorProperties.put("WdSpd", 3);

    Map<String, Object> result = transformer.fromVendor(vendorProperties);

    assertEquals(4, result.size());
    assertEquals("on", result.get("power"));
    assertEquals("dry", result.get("mode"));
    assertEquals(25, result.get("temperature"));
    assertEquals("medium", result.get("fanSpeed"));
  }

  @Test
  void testToVendorBasicProperties() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("power", "on");
    properties.put("mode", "cool");
    properties.put("temperature", 22);
    properties.put("fanSpeed", "high");

    Map<String, Object> result = transformer.toVendor(properties);

    assertEquals(4, result.size());
    assertEquals(1, result.get("Pow"));
    assertEquals(1, result.get("Mod"));
    assertEquals(22, result.get("SetTem"));
    assertEquals(5, result.get("WdSpd"));
  }

  @Test
  void testFromVendorCurrentTemperature() {
    Map<String, Object> vendorProperties = new HashMap<>();
    vendorProperties.put("TemSen", 62); // 62 - 40 = 22°C

    Map<String, Object> result = transformer.fromVendor(vendorProperties);

    assertEquals(1, result.size());
    assertEquals(22, result.get("currentTemperature"));
  }

  @Test
  void testFromVendorCurrentTemperatureZero() {
    Map<String, Object> vendorProperties = new HashMap<>();
    vendorProperties.put("TemSen", 0);

    Map<String, Object> result = transformer.fromVendor(vendorProperties);

    assertEquals(1, result.size());
    assertEquals(0, result.get("currentTemperature"));
  }

  @Test
  void testToVendorCurrentTemperatureThrowsException() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("currentTemperature", 25);

    assertThrows(IllegalArgumentException.class, () -> transformer.toVendor(properties));
  }

  @Test
  void testFromVendorSwingProperties() {
    Map<String, Object> vendorProperties = new HashMap<>();
    vendorProperties.put("SwingLfRig", 2);
    vendorProperties.put("SwUpDn", 5);

    Map<String, Object> result = transformer.fromVendor(vendorProperties);

    assertEquals(2, result.size());
    assertEquals("fixedLeft", result.get("swingHor"));
    assertEquals("fixedMidBottom", result.get("swingVert"));
  }

  @Test
  void testToVendorSwingProperties() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("swingHor", "fixedRight");
    properties.put("swingVert", "swingMid");

    Map<String, Object> result = transformer.toVendor(properties);

    assertEquals(2, result.size());
    assertEquals(6, result.get("SwingLfRig"));
    assertEquals(9, result.get("SwUpDn"));
  }

  @Test
  void testFromVendorBooleanProperties() {
    Map<String, Object> vendorProperties = new HashMap<>();
    vendorProperties.put("Lig", 1);
    vendorProperties.put("Tur", 0);
    vendorProperties.put("Health", 1);
    vendorProperties.put("SvSt", 0);

    Map<String, Object> result = transformer.fromVendor(vendorProperties);

    assertEquals(4, result.size());
    assertEquals("on", result.get("lights"));
    assertEquals("off", result.get("turbo"));
    assertEquals("on", result.get("health"));
    assertEquals("off", result.get("powerSave"));
  }

  @Test
  void testToVendorBooleanProperties() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("lights", "on");
    properties.put("turbo", "off");
    properties.put("health", "on");
    properties.put("powerSave", "off");

    Map<String, Object> result = transformer.toVendor(properties);

    assertEquals(4, result.size());
    assertEquals(1, result.get("Lig"));
    assertEquals(0, result.get("Tur"));
    assertEquals(1, result.get("Health"));
    assertEquals(0, result.get("SvSt"));
  }

  @Test
  void testFromVendorUnknownProperty() {
    Map<String, Object> vendorProperties = new HashMap<>();
    vendorProperties.put("UnknownProp", "value");

    Map<String, Object> result = transformer.fromVendor(vendorProperties);

    assertEquals(0, result.size());
  }

  @Test
  void testToVendorUnknownProperty() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("unknownProperty", "value");

    Map<String, Object> result = transformer.toVendor(properties);

    assertEquals(0, result.size());
  }

  @Test
  void testFromVendorEmptyMap() {
    Map<String, Object> vendorProperties = new HashMap<>();

    Map<String, Object> result = transformer.fromVendor(vendorProperties);

    assertTrue(result.isEmpty());
  }

  @Test
  void testToVendorEmptyMap() {
    Map<String, Object> properties = new HashMap<>();

    Map<String, Object> result = transformer.toVendor(properties);

    assertTrue(result.isEmpty());
  }

  @Test
  void testFromVendorNullMap() {
    assertThrows(NullPointerException.class, () -> transformer.fromVendor(null));
  }

  @Test
  void testToVendorNullMap() {
    assertThrows(NullPointerException.class, () -> transformer.toVendor(null));
  }

  @Test
  void testArrayToVendor() {
    List<String> properties = Arrays.asList("power", "mode", "temperature", "fanSpeed");

    List<String> result = transformer.arrayToVendor(properties);

    assertEquals(4, result.size());
    assertEquals("Pow", result.get(0));
    assertEquals("Mod", result.get(1));
    assertEquals("SetTem", result.get(2));
    assertEquals("WdSpd", result.get(3));
  }

  @Test
  void testArrayToVendorWithUnknownProperties() {
    List<String> properties = Arrays.asList("power", "unknownProperty", "temperature");

    List<String> result = transformer.arrayToVendor(properties);

    assertEquals(2, result.size());
    assertEquals("Pow", result.get(0));
    assertEquals("SetTem", result.get(1));
  }

  @Test
  void testArrayToVendorEmptyList() {
    List<String> properties = new ArrayList<>();

    List<String> result = transformer.arrayToVendor(properties);

    assertTrue(result.isEmpty());
  }

  @Test
  void testArrayToVendorNullList() {
    assertThrows(NullPointerException.class, () -> transformer.arrayToVendor(null));
  }

  @Test
  void testFromVendorWithMixedTypes() {
    Map<String, Object> vendorProperties = new HashMap<>();
    vendorProperties.put("Pow", 1);
    vendorProperties.put("SetTem", 25);
    vendorProperties.put("TemSen", 62);
    vendorProperties.put("Mod", 2);

    Map<String, Object> result = transformer.fromVendor(vendorProperties);

    assertEquals(4, result.size());
    assertEquals("on", result.get("power"));
    assertEquals(25, result.get("temperature"));
    assertEquals(22, result.get("currentTemperature"));
    assertEquals("dry", result.get("mode"));
  }

  @Test
  void testToVendorWithMixedTypes() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("power", "on");
    properties.put("temperature", 22);
    properties.put("mode", "cool");
    properties.put("fanSpeed", "high");

    Map<String, Object> result = transformer.toVendor(properties);

    assertEquals(4, result.size());
    assertEquals(1, result.get("Pow"));
    assertEquals(22, result.get("SetTem"));
    assertEquals(1, result.get("Mod"));
    assertEquals(5, result.get("WdSpd"));
  }

  @Test
  void testFromVendorAllProperties() {
    Map<String, Object> vendorProperties = new HashMap<>();
    vendorProperties.put("Pow", 1);
    vendorProperties.put("Mod", 1);
    vendorProperties.put("TemUn", 0);
    vendorProperties.put("SetTem", 24);
    vendorProperties.put("TemSen", 64);
    vendorProperties.put("WdSpd", 4);
    vendorProperties.put("Air", 1);
    vendorProperties.put("Blo", 0);
    vendorProperties.put("Health", 1);
    vendorProperties.put("SwhSlp", 0);
    vendorProperties.put("Lig", 1);
    vendorProperties.put("SwingLfRig", 1);
    vendorProperties.put("SwUpDn", 1);
    vendorProperties.put("Quiet", 2);
    vendorProperties.put("Tur", 1);
    vendorProperties.put("SvSt", 0);
    vendorProperties.put("StHt", 1);

    Map<String, Object> result = transformer.fromVendor(vendorProperties);

    assertEquals(17, result.size());
    assertEquals("on", result.get("power"));
    assertEquals("cool", result.get("mode"));
    assertEquals("celsius", result.get("temperatureUnit"));
    assertEquals(24, result.get("temperature"));
    assertEquals(24, result.get("currentTemperature"));
    assertEquals("mediumHigh", result.get("fanSpeed"));
    assertEquals("inside", result.get("air"));
    assertEquals("off", result.get("blow"));
    assertEquals("on", result.get("health"));
    assertEquals("off", result.get("sleep"));
    assertEquals("on", result.get("lights"));
    assertEquals("full", result.get("swingHor"));
    assertEquals("full", result.get("swingVert"));
    assertEquals("mode2", result.get("quiet"));
    assertEquals("on", result.get("turbo"));
    assertEquals("off", result.get("powerSave"));
    assertEquals("on", result.get("safetyHeating"));
  }

  @Test
  void testToVendorAllProperties() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("power", "on");
    properties.put("mode", "heat");
    properties.put("temperatureUnit", "fahrenheit");
    properties.put("temperature", 24);
    // currentTemperature is read-only, so we don't include it
    properties.put("fanSpeed", "mediumHigh");
    properties.put("air", "inside");
    properties.put("blow", "off");
    properties.put("health", "on");
    properties.put("sleep", "off");
    properties.put("lights", "on");
    properties.put("swingHor", "full");
    properties.put("swingVert", "full");
    properties.put("quiet", "mode2");
    properties.put("turbo", "on");
    properties.put("powerSave", "off");
    properties.put("safetyHeating", "on");

    Map<String, Object> result = transformer.toVendor(properties);

    assertEquals(16, result.size()); // 16 properties (excluding currentTemperature)
    assertEquals(1, result.get("Pow"));
    assertEquals(4, result.get("Mod"));
    assertEquals(1, result.get("TemUn"));
    assertEquals(24, result.get("SetTem"));
    assertEquals(4, result.get("WdSpd"));
    assertEquals(1, result.get("Air"));
    assertEquals(0, result.get("Blo"));
    assertEquals(1, result.get("Health"));
    assertEquals(0, result.get("SwhSlp"));
    assertEquals(1, result.get("Lig"));
    assertEquals(1, result.get("SwingLfRig"));
    assertEquals(1, result.get("SwUpDn"));
    assertEquals(2, result.get("Quiet"));
    assertEquals(1, result.get("Tur"));
    assertEquals(0, result.get("SvSt"));
    assertEquals(1, result.get("StHt"));
  }
}
