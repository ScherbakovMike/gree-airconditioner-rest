package com.gree.hvac.protocol;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.Test;

class PropertyValueTest {

  @Test
  void testPowerConstants() {
    assertEquals("off", PropertyValue.Power.OFF);
    assertEquals("on", PropertyValue.Power.ON);
  }

  @Test
  void testModeConstants() {
    assertEquals("auto", PropertyValue.Mode.AUTO);
    assertEquals("cool", PropertyValue.Mode.COOL);
    assertEquals("dry", PropertyValue.Mode.DRY);
    assertEquals("fan_only", PropertyValue.Mode.FAN_ONLY);
    assertEquals("heat", PropertyValue.Mode.HEAT);
  }

  @Test
  void testTemperatureUnitConstants() {
    assertEquals("celsius", PropertyValue.TemperatureUnit.CELSIUS);
    assertEquals("fahrenheit", PropertyValue.TemperatureUnit.FAHRENHEIT);
  }

  @Test
  void testFanSpeedConstants() {
    assertEquals("auto", PropertyValue.FanSpeed.AUTO);
    assertEquals("low", PropertyValue.FanSpeed.LOW);
    assertEquals("mediumLow", PropertyValue.FanSpeed.MEDIUM_LOW);
    assertEquals("medium", PropertyValue.FanSpeed.MEDIUM);
    assertEquals("mediumHigh", PropertyValue.FanSpeed.MEDIUM_HIGH);
    assertEquals("high", PropertyValue.FanSpeed.HIGH);
  }

  @Test
  void testAirConstants() {
    assertEquals("off", PropertyValue.Air.OFF);
    assertEquals("inside", PropertyValue.Air.INSIDE);
    assertEquals("outside", PropertyValue.Air.OUTSIDE);
    assertEquals("mode3", PropertyValue.Air.MODE3);
  }

  @Test
  void testBlowConstants() {
    assertEquals("off", PropertyValue.Blow.OFF);
    assertEquals("on", PropertyValue.Blow.ON);
  }

  @Test
  void testHealthConstants() {
    assertEquals("off", PropertyValue.Health.OFF);
    assertEquals("on", PropertyValue.Health.ON);
  }

  @Test
  void testSleepConstants() {
    assertEquals("off", PropertyValue.Sleep.OFF);
    assertEquals("on", PropertyValue.Sleep.ON);
  }

  @Test
  void testLightsConstants() {
    assertEquals("off", PropertyValue.Lights.OFF);
    assertEquals("on", PropertyValue.Lights.ON);
  }

  @Test
  void testSwingHorConstants() {
    assertEquals("default", PropertyValue.SwingHor.DEFAULT);
    assertEquals("full", PropertyValue.SwingHor.FULL);
    assertEquals("fixedLeft", PropertyValue.SwingHor.FIXED_LEFT);
    assertEquals("fixedMidLeft", PropertyValue.SwingHor.FIXED_MID_LEFT);
    assertEquals("fixedMid", PropertyValue.SwingHor.FIXED_MID);
    assertEquals("fixedMidRight", PropertyValue.SwingHor.FIXED_MID_RIGHT);
    assertEquals("fixedRight", PropertyValue.SwingHor.FIXED_RIGHT);
    assertEquals("fullAlt", PropertyValue.SwingHor.FULL_ALT);
  }

  @Test
  void testSwingVertConstants() {
    assertEquals("default", PropertyValue.SwingVert.DEFAULT);
    assertEquals("full", PropertyValue.SwingVert.FULL);
    assertEquals("fixedTop", PropertyValue.SwingVert.FIXED_TOP);
    assertEquals("fixedMidTop", PropertyValue.SwingVert.FIXED_MID_TOP);
    assertEquals("fixedMid", PropertyValue.SwingVert.FIXED_MID);
    assertEquals("fixedMidBottom", PropertyValue.SwingVert.FIXED_MID_BOTTOM);
    assertEquals("fixedBottom", PropertyValue.SwingVert.FIXED_BOTTOM);
    assertEquals("swingBottom", PropertyValue.SwingVert.SWING_BOTTOM);
    assertEquals("swingMidBottom", PropertyValue.SwingVert.SWING_MID_BOTTOM);
    assertEquals("swingMid", PropertyValue.SwingVert.SWING_MID);
    assertEquals("swingMidTop", PropertyValue.SwingVert.SWING_MID_TOP);
    assertEquals("swingTop", PropertyValue.SwingVert.SWING_TOP);
  }

  @Test
  void testQuietConstants() {
    assertEquals("off", PropertyValue.Quiet.OFF);
    assertEquals("mode1", PropertyValue.Quiet.MODE1);
    assertEquals("mode2", PropertyValue.Quiet.MODE2);
    assertEquals("mode3", PropertyValue.Quiet.MODE3);
  }

  @Test
  void testTurboConstants() {
    assertEquals("off", PropertyValue.Turbo.OFF);
    assertEquals("on", PropertyValue.Turbo.ON);
  }

  @Test
  void testPowerSaveConstants() {
    assertEquals("off", PropertyValue.PowerSave.OFF);
    assertEquals("on", PropertyValue.PowerSave.ON);
  }

  @Test
  void testSafetyHeatingConstants() {
    assertEquals("off", PropertyValue.SafetyHeating.OFF);
    assertEquals("on", PropertyValue.SafetyHeating.ON);
  }

  @Test
  void testGetVendorValues() {
    Map<String, Map<String, Integer>> vendorValues = PropertyValue.getVendorValues();

    assertNotNull(vendorValues);
    assertFalse(vendorValues.isEmpty());

    // Test power mapping
    Map<String, Integer> powerValues = vendorValues.get("power");
    assertNotNull(powerValues);
    assertEquals(0, powerValues.get(PropertyValue.Power.OFF));
    assertEquals(1, powerValues.get(PropertyValue.Power.ON));

    // Test mode mapping
    Map<String, Integer> modeValues = vendorValues.get("mode");
    assertNotNull(modeValues);
    assertEquals(0, modeValues.get(PropertyValue.Mode.AUTO));
    assertEquals(1, modeValues.get(PropertyValue.Mode.COOL));
    assertEquals(2, modeValues.get(PropertyValue.Mode.DRY));
    assertEquals(3, modeValues.get(PropertyValue.Mode.FAN_ONLY));
    assertEquals(4, modeValues.get(PropertyValue.Mode.HEAT));

    // Test temperature unit mapping
    Map<String, Integer> tempUnitValues = vendorValues.get("temperatureUnit");
    assertNotNull(tempUnitValues);
    assertEquals(0, tempUnitValues.get(PropertyValue.TemperatureUnit.CELSIUS));
    assertEquals(1, tempUnitValues.get(PropertyValue.TemperatureUnit.FAHRENHEIT));

    // Test temperature mapping (16-30 Celsius)
    Map<String, Integer> temperatureValues = vendorValues.get("temperature");
    assertNotNull(temperatureValues);
    assertEquals(0, temperatureValues.get("16"));
    assertEquals(5, temperatureValues.get("21"));
    assertEquals(14, temperatureValues.get("30"));

    // Test current temperature mapping (0-50 Celsius)
    Map<String, Integer> currentTemperatureValues = vendorValues.get("currentTemperature");
    assertNotNull(currentTemperatureValues);
    assertEquals(0, currentTemperatureValues.get("0"));
    assertEquals(22, currentTemperatureValues.get("22"));
    assertEquals(50, currentTemperatureValues.get("50"));

    // Test fan speed mapping
    Map<String, Integer> fanSpeedValues = vendorValues.get("fanSpeed");
    assertNotNull(fanSpeedValues);
    assertEquals(0, fanSpeedValues.get(PropertyValue.FanSpeed.AUTO));
    assertEquals(1, fanSpeedValues.get(PropertyValue.FanSpeed.LOW));
    assertEquals(2, fanSpeedValues.get(PropertyValue.FanSpeed.MEDIUM_LOW));
    assertEquals(3, fanSpeedValues.get(PropertyValue.FanSpeed.MEDIUM));
    assertEquals(4, fanSpeedValues.get(PropertyValue.FanSpeed.MEDIUM_HIGH));
    assertEquals(5, fanSpeedValues.get(PropertyValue.FanSpeed.HIGH));

    // Test air mapping
    Map<String, Integer> airValues = vendorValues.get("air");
    assertNotNull(airValues);
    assertEquals(0, airValues.get(PropertyValue.Air.OFF));
    assertEquals(1, airValues.get(PropertyValue.Air.INSIDE));
    assertEquals(2, airValues.get(PropertyValue.Air.OUTSIDE));
    assertEquals(3, airValues.get(PropertyValue.Air.MODE3));

    // Test blow mapping
    Map<String, Integer> blowValues = vendorValues.get("blow");
    assertNotNull(blowValues);
    assertEquals(0, blowValues.get(PropertyValue.Blow.OFF));
    assertEquals(1, blowValues.get(PropertyValue.Blow.ON));

    // Test health mapping
    Map<String, Integer> healthValues = vendorValues.get("health");
    assertNotNull(healthValues);
    assertEquals(0, healthValues.get(PropertyValue.Health.OFF));
    assertEquals(1, healthValues.get(PropertyValue.Health.ON));

    // Test sleep mapping
    Map<String, Integer> sleepValues = vendorValues.get("sleep");
    assertNotNull(sleepValues);
    assertEquals(0, sleepValues.get(PropertyValue.Sleep.OFF));
    assertEquals(1, sleepValues.get(PropertyValue.Sleep.ON));

    // Test lights mapping
    Map<String, Integer> lightsValues = vendorValues.get("lights");
    assertNotNull(lightsValues);
    assertEquals(0, lightsValues.get(PropertyValue.Lights.OFF));
    assertEquals(1, lightsValues.get(PropertyValue.Lights.ON));

    // Test swing horizontal mapping
    Map<String, Integer> swingHorValues = vendorValues.get("swingHor");
    assertNotNull(swingHorValues);
    assertEquals(0, swingHorValues.get(PropertyValue.SwingHor.DEFAULT));
    assertEquals(1, swingHorValues.get(PropertyValue.SwingHor.FULL));
    assertEquals(2, swingHorValues.get(PropertyValue.SwingHor.FIXED_LEFT));
    assertEquals(3, swingHorValues.get(PropertyValue.SwingHor.FIXED_MID_LEFT));
    assertEquals(4, swingHorValues.get(PropertyValue.SwingHor.FIXED_MID));
    assertEquals(5, swingHorValues.get(PropertyValue.SwingHor.FIXED_MID_RIGHT));
    assertEquals(6, swingHorValues.get(PropertyValue.SwingHor.FIXED_RIGHT));
    assertEquals(7, swingHorValues.get(PropertyValue.SwingHor.FULL_ALT));

    // Test swing vertical mapping
    Map<String, Integer> swingVertValues = vendorValues.get("swingVert");
    assertNotNull(swingVertValues);
    assertEquals(0, swingVertValues.get(PropertyValue.SwingVert.DEFAULT));
    assertEquals(1, swingVertValues.get(PropertyValue.SwingVert.FULL));
    assertEquals(2, swingVertValues.get(PropertyValue.SwingVert.FIXED_TOP));
    assertEquals(3, swingVertValues.get(PropertyValue.SwingVert.FIXED_MID_TOP));
    assertEquals(4, swingVertValues.get(PropertyValue.SwingVert.FIXED_MID));
    assertEquals(5, swingVertValues.get(PropertyValue.SwingVert.FIXED_MID_BOTTOM));
    assertEquals(6, swingVertValues.get(PropertyValue.SwingVert.FIXED_BOTTOM));
    assertEquals(7, swingVertValues.get(PropertyValue.SwingVert.SWING_BOTTOM));
    assertEquals(8, swingVertValues.get(PropertyValue.SwingVert.SWING_MID_BOTTOM));
    assertEquals(9, swingVertValues.get(PropertyValue.SwingVert.SWING_MID));
    assertEquals(10, swingVertValues.get(PropertyValue.SwingVert.SWING_MID_TOP));
    assertEquals(11, swingVertValues.get(PropertyValue.SwingVert.SWING_TOP));

    // Test quiet mapping
    Map<String, Integer> quietValues = vendorValues.get("quiet");
    assertNotNull(quietValues);
    assertEquals(0, quietValues.get(PropertyValue.Quiet.OFF));
    assertEquals(1, quietValues.get(PropertyValue.Quiet.MODE1));
    assertEquals(2, quietValues.get(PropertyValue.Quiet.MODE2));
    assertEquals(3, quietValues.get(PropertyValue.Quiet.MODE3));

    // Test turbo mapping
    Map<String, Integer> turboValues = vendorValues.get("turbo");
    assertNotNull(turboValues);
    assertEquals(0, turboValues.get(PropertyValue.Turbo.OFF));
    assertEquals(1, turboValues.get(PropertyValue.Turbo.ON));

    // Test power save mapping
    Map<String, Integer> powerSaveValues = vendorValues.get("powerSave");
    assertNotNull(powerSaveValues);
    assertEquals(0, powerSaveValues.get(PropertyValue.PowerSave.OFF));
    assertEquals(1, powerSaveValues.get(PropertyValue.PowerSave.ON));

    // Test safety heating mapping
    Map<String, Integer> safetyHeatingValues = vendorValues.get("safetyHeating");
    assertNotNull(safetyHeatingValues);
    assertEquals(0, safetyHeatingValues.get(PropertyValue.SafetyHeating.OFF));
    assertEquals(1, safetyHeatingValues.get(PropertyValue.SafetyHeating.ON));
  }

  @Test
  void testVendorValuesImmutability() {
    Map<String, Map<String, Integer>> vendorValues = PropertyValue.getVendorValues();

    // Verify that the returned map is not modifiable
    assertThrows(
        UnsupportedOperationException.class,
        () -> {
          vendorValues.put("test", Map.of("test", 1));
        });

    // Verify that the inner maps are also not modifiable
    Map<String, Integer> powerValues = vendorValues.get("power");
    assertThrows(
        UnsupportedOperationException.class,
        () -> {
          powerValues.put("test", 999);
        });
  }

  @Test
  void testVendorValuesConsistency() {
    Map<String, Map<String, Integer>> vendorValues = PropertyValue.getVendorValues();

    // Verify that all expected property types are present
    assertTrue(vendorValues.containsKey("power"));
    assertTrue(vendorValues.containsKey("mode"));
    assertTrue(vendorValues.containsKey("temperatureUnit"));
    assertTrue(vendorValues.containsKey("temperature"));
    assertTrue(vendorValues.containsKey("currentTemperature"));
    assertTrue(vendorValues.containsKey("fanSpeed"));
    assertTrue(vendorValues.containsKey("air"));
    assertTrue(vendorValues.containsKey("blow"));
    assertTrue(vendorValues.containsKey("health"));
    assertTrue(vendorValues.containsKey("sleep"));
    assertTrue(vendorValues.containsKey("lights"));
    assertTrue(vendorValues.containsKey("swingHor"));
    assertTrue(vendorValues.containsKey("swingVert"));
    assertTrue(vendorValues.containsKey("quiet"));
    assertTrue(vendorValues.containsKey("turbo"));
    assertTrue(vendorValues.containsKey("powerSave"));
    assertTrue(vendorValues.containsKey("safetyHeating"));

    // Verify that there are exactly 17 property types
    assertEquals(17, vendorValues.size());
  }
}
