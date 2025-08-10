package com.gree.hvac.protocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Transforms device properties from vendor names to human friendly names and back */
public class PropertyTransformer {
  private static final Map<String, String> PROPERTY_VENDOR_CODES = new HashMap<>();
  private static final Map<String, String> REVERSED_PROPERTIES = new HashMap<>();

  // Property name constant
  private static final String CURRENT_TEMPERATURE = "currentTemperature";

  static {
    PROPERTY_VENDOR_CODES.put("power", "Pow");
    PROPERTY_VENDOR_CODES.put("mode", "Mod");
    PROPERTY_VENDOR_CODES.put("temperatureUnit", "TemUn");
    PROPERTY_VENDOR_CODES.put("temperature", "SetTem");
    PROPERTY_VENDOR_CODES.put(CURRENT_TEMPERATURE, "TemSen");
    PROPERTY_VENDOR_CODES.put("fanSpeed", "WdSpd");
    PROPERTY_VENDOR_CODES.put("air", "Air");
    PROPERTY_VENDOR_CODES.put("blow", "Blo");
    PROPERTY_VENDOR_CODES.put("health", "Health");
    PROPERTY_VENDOR_CODES.put("sleep", "SwhSlp");
    PROPERTY_VENDOR_CODES.put("lights", "Lig");
    PROPERTY_VENDOR_CODES.put("swingHor", "SwingLfRig");
    PROPERTY_VENDOR_CODES.put("swingVert", "SwUpDn");
    PROPERTY_VENDOR_CODES.put("quiet", "Quiet");
    PROPERTY_VENDOR_CODES.put("turbo", "Tur");
    PROPERTY_VENDOR_CODES.put("powerSave", "SvSt");
    PROPERTY_VENDOR_CODES.put("safetyHeating", "StHt");

    // Create reversed mapping
    for (Map.Entry<String, String> entry : PROPERTY_VENDOR_CODES.entrySet()) {
      REVERSED_PROPERTIES.put(entry.getValue(), entry.getKey());
    }
  }

  /** Transforms device properties from vendor names to human friendly names */
  public Map<String, Object> fromVendor(Map<String, Object> properties) {
    Map<String, Object> result = new HashMap<>();
    for (Map.Entry<String, Object> entry : properties.entrySet()) {
      String property = entry.getKey();
      Object value = entry.getValue();

      String reversedProperty = REVERSED_PROPERTIES.get(property);
      if (reversedProperty != null) {
        result.put(reversedProperty, valueFromVendor(reversedProperty, value));
      }
    }
    return result;
  }

  /** Transforms device properties from human friendly names to vendor names */
  public Map<String, Object> toVendor(Map<String, Object> properties) {
    Map<String, Object> result = new HashMap<>();
    for (Map.Entry<String, Object> entry : properties.entrySet()) {
      String property = entry.getKey();
      Object value = entry.getValue();

      String vendorProperty = PROPERTY_VENDOR_CODES.get(property);
      if (vendorProperty != null) {
        result.put(vendorProperty, valueToVendor(property, value));
      }
    }
    return result;
  }

  /** Transform property array to vendor codes */
  public List<String> arrayToVendor(List<String> properties) {
    List<String> result = new ArrayList<>();
    for (String property : properties) {
      String vendorCode = PROPERTY_VENDOR_CODES.get(property);
      if (vendorCode != null) {
        result.add(vendorCode);
      }
    }
    return result;
  }

  private Object valueFromVendor(String property, Object value) {
    // Special handling for currentTemperature
    if (CURRENT_TEMPERATURE.equals(property) && value instanceof Number number) {
      int intValue = number.intValue();
      if (intValue != 0) {
        return intValue - 40; // Temperature from AC should be transformed by subtracting 40
      }
      return intValue;
    }

    // Handle string values that need to be converted from vendor codes
    Map<String, Map<String, Integer>> vendorValues = PropertyValue.getVendorValues();
    Map<String, Integer> propertyValues = vendorValues.get(property);

    if (propertyValues != null && value instanceof Number number) {
      int intValue = number.intValue();
      // Find the string key for this vendor value
      for (Map.Entry<String, Integer> entry : propertyValues.entrySet()) {
        if (entry.getValue().equals(intValue)) {
          return entry.getKey();
        }
      }
    }

    return value;
  }

  private Object valueToVendor(String property, Object value) {
    // Special handling for currentTemperature (read-only)
    if (CURRENT_TEMPERATURE.equals(property)) {
      throw new IllegalArgumentException("Cannot set read-only property currentTemperature");
    }

    // Handle string values that need to be converted to vendor codes
    Map<String, Map<String, Integer>> vendorValues = PropertyValue.getVendorValues();
    Map<String, Integer> propertyValues = vendorValues.get(property);

    if (propertyValues != null && value instanceof String) {
      Integer vendorValue = propertyValues.get(value);
      if (vendorValue != null) {
        return vendorValue;
      }
    }

    return value;
  }
}
