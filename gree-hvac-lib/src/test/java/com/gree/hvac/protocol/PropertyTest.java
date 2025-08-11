package com.gree.hvac.protocol;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PropertyTest {

  @Test
  void testAllPropertyValues() {
    Property[] properties = Property.values();
    assertEquals(17, properties.length);

    // Test that all properties have non-null values
    for (Property property : properties) {
      assertNotNull(property.getValue());
      assertFalse(property.getValue().isEmpty());
    }
  }

  @Test
  void testPowerProperty() {
    Property power = Property.POWER;
    assertEquals("power", power.getValue());
    assertEquals("power", power.toString());
  }

  @Test
  void testModeProperty() {
    Property mode = Property.MODE;
    assertEquals("mode", mode.getValue());
    assertEquals("mode", mode.toString());
  }

  @Test
  void testTemperatureUnitProperty() {
    Property tempUnit = Property.TEMPERATURE_UNIT;
    assertEquals("temperatureUnit", tempUnit.getValue());
    assertEquals("temperatureUnit", tempUnit.toString());
  }

  @Test
  void testTemperatureProperty() {
    Property temp = Property.TEMPERATURE;
    assertEquals("temperature", temp.getValue());
    assertEquals("temperature", temp.toString());
  }

  @Test
  void testCurrentTemperatureProperty() {
    Property currentTemp = Property.CURRENT_TEMPERATURE;
    assertEquals("currentTemperature", currentTemp.getValue());
    assertEquals("currentTemperature", currentTemp.toString());
  }

  @Test
  void testFanSpeedProperty() {
    Property fanSpeed = Property.FAN_SPEED;
    assertEquals("fanSpeed", fanSpeed.getValue());
    assertEquals("fanSpeed", fanSpeed.toString());
  }

  @Test
  void testAirProperty() {
    Property air = Property.AIR;
    assertEquals("air", air.getValue());
    assertEquals("air", air.toString());
  }

  @Test
  void testBlowProperty() {
    Property blow = Property.BLOW;
    assertEquals("blow", blow.getValue());
    assertEquals("blow", blow.toString());
  }

  @Test
  void testHealthProperty() {
    Property health = Property.HEALTH;
    assertEquals("health", health.getValue());
    assertEquals("health", health.toString());
  }

  @Test
  void testSleepProperty() {
    Property sleep = Property.SLEEP;
    assertEquals("sleep", sleep.getValue());
    assertEquals("sleep", sleep.toString());
  }

  @Test
  void testLightsProperty() {
    Property lights = Property.LIGHTS;
    assertEquals("lights", lights.getValue());
    assertEquals("lights", lights.toString());
  }

  @Test
  void testSwingHorProperty() {
    Property swingHor = Property.SWING_HOR;
    assertEquals("swingHor", swingHor.getValue());
    assertEquals("swingHor", swingHor.toString());
  }

  @Test
  void testSwingVertProperty() {
    Property swingVert = Property.SWING_VERT;
    assertEquals("swingVert", swingVert.getValue());
    assertEquals("swingVert", swingVert.toString());
  }

  @Test
  void testQuietProperty() {
    Property quiet = Property.QUIET;
    assertEquals("quiet", quiet.getValue());
    assertEquals("quiet", quiet.toString());
  }

  @Test
  void testTurboProperty() {
    Property turbo = Property.TURBO;
    assertEquals("turbo", turbo.getValue());
    assertEquals("turbo", turbo.toString());
  }

  @Test
  void testPowerSaveProperty() {
    Property powerSave = Property.POWER_SAVE;
    assertEquals("powerSave", powerSave.getValue());
    assertEquals("powerSave", powerSave.toString());
  }

  @Test
  void testSafetyHeatingProperty() {
    Property safetyHeating = Property.SAFETY_HEATING;
    assertEquals("safetyHeating", safetyHeating.getValue());
    assertEquals("safetyHeating", safetyHeating.toString());
  }

  @Test
  void testFromStringWithValidValues() {
    assertEquals(Property.POWER, Property.fromString("power"));
    assertEquals(Property.MODE, Property.fromString("mode"));
    assertEquals(Property.TEMPERATURE, Property.fromString("temperature"));
    assertEquals(Property.FAN_SPEED, Property.fromString("fanSpeed"));
  }

  @Test
  void testFromStringWithCaseInsensitiveValues() {
    assertEquals(Property.POWER, Property.fromString("POWER"));
    assertEquals(Property.MODE, Property.fromString("Mode"));
    assertEquals(Property.TEMPERATURE, Property.fromString("TEMPERATURE"));
    assertEquals(Property.FAN_SPEED, Property.fromString("FANSPEED"));
  }

  @Test
  void testFromStringWithMixedCase() {
    assertEquals(Property.TEMPERATURE_UNIT, Property.fromString("TemperatureUnit"));
    assertEquals(Property.CURRENT_TEMPERATURE, Property.fromString("CurrentTemperature"));
    assertEquals(Property.SWING_HOR, Property.fromString("SwingHor"));
    assertEquals(Property.SWING_VERT, Property.fromString("SwingVert"));
  }

  @Test
  void testFromStringWithInvalidValues() {
    assertThrows(IllegalArgumentException.class, () -> Property.fromString("invalid"));
    assertThrows(IllegalArgumentException.class, () -> Property.fromString(""));
    assertThrows(IllegalArgumentException.class, () -> Property.fromString("temp"));
    assertThrows(IllegalArgumentException.class, () -> Property.fromString("fan"));
  }

  @Test
  void testFromStringWithNullValue() {
    assertThrows(IllegalArgumentException.class, () -> Property.fromString(null));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "power",
        "mode",
        "temperature",
        "fanSpeed",
        "air",
        "blow",
        "health",
        "sleep",
        "lights",
        "swingHor",
        "swingVert",
        "quiet",
        "turbo",
        "powerSave",
        "safetyHeating"
      })
  void testFromStringWithAllValidValues(String value) {
    Property property = Property.fromString(value);
    assertNotNull(property);
    assertEquals(value, property.getValue());
  }

  @Test
  void testEnumOrdinalValues() {
    // Test that enum ordinals are as expected (0-based index)
    assertEquals(0, Property.POWER.ordinal());
    assertEquals(1, Property.MODE.ordinal());
    assertEquals(2, Property.TEMPERATURE_UNIT.ordinal());
    assertEquals(3, Property.TEMPERATURE.ordinal());
    assertEquals(4, Property.CURRENT_TEMPERATURE.ordinal());
    assertEquals(5, Property.FAN_SPEED.ordinal());
    assertEquals(6, Property.AIR.ordinal());
    assertEquals(7, Property.BLOW.ordinal());
    assertEquals(8, Property.HEALTH.ordinal());
    assertEquals(9, Property.SLEEP.ordinal());
    assertEquals(10, Property.LIGHTS.ordinal());
    assertEquals(11, Property.SWING_HOR.ordinal());
    assertEquals(12, Property.SWING_VERT.ordinal());
    assertEquals(13, Property.QUIET.ordinal());
    assertEquals(14, Property.TURBO.ordinal());
    assertEquals(15, Property.POWER_SAVE.ordinal());
    assertEquals(16, Property.SAFETY_HEATING.ordinal());
  }

  @Test
  void testValueOfMethod() {
    // Test the built-in valueOf method
    assertEquals(Property.POWER, Property.valueOf("POWER"));
    assertEquals(Property.MODE, Property.valueOf("MODE"));
    assertEquals(Property.TEMPERATURE, Property.valueOf("TEMPERATURE"));

    // valueOf is case-sensitive and throws IllegalArgumentException for invalid values
    assertThrows(IllegalArgumentException.class, () -> Property.valueOf("power"));
    assertThrows(IllegalArgumentException.class, () -> Property.valueOf("invalid"));
  }
}
