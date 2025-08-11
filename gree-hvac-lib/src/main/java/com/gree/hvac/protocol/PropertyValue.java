package com.gree.hvac.protocol;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Device property value constants */
public class PropertyValue {

  public static class Power {
    public static final String OFF = "off";
    public static final String ON = "on";
  }

  public static class Mode {
    public static final String AUTO = "auto";
    public static final String COOL = "cool";
    public static final String DRY = "dry";
    public static final String FAN_ONLY = "fan_only";
    public static final String HEAT = "heat";
  }

  public static class TemperatureUnit {
    public static final String CELSIUS = "celsius";
    public static final String FAHRENHEIT = "fahrenheit";
  }

  public static class FanSpeed {
    public static final String AUTO = "auto";
    public static final String LOW = "low";
    public static final String MEDIUM_LOW = "mediumLow";
    public static final String MEDIUM = "medium";
    public static final String MEDIUM_HIGH = "mediumHigh";
    public static final String HIGH = "high";
  }

  public static class Air {
    public static final String OFF = "off";
    public static final String INSIDE = "inside";
    public static final String OUTSIDE = "outside";
    public static final String MODE3 = "mode3";
  }

  public static class Blow {
    public static final String OFF = "off";
    public static final String ON = "on";
  }

  public static class Health {
    public static final String OFF = "off";
    public static final String ON = "on";
  }

  public static class Sleep {
    public static final String OFF = "off";
    public static final String ON = "on";
  }

  public static class Lights {
    public static final String OFF = "off";
    public static final String ON = "on";
  }

  public static class SwingHor {
    public static final String DEFAULT = "default";
    public static final String FULL = "full";
    public static final String FIXED_LEFT = "fixedLeft";
    public static final String FIXED_MID_LEFT = "fixedMidLeft";
    public static final String FIXED_MID = "fixedMid";
    public static final String FIXED_MID_RIGHT = "fixedMidRight";
    public static final String FIXED_RIGHT = "fixedRight";
    public static final String FULL_ALT = "fullAlt";
  }

  public static class SwingVert {
    public static final String DEFAULT = "default";
    public static final String FULL = "full";
    public static final String FIXED_TOP = "fixedTop";
    public static final String FIXED_MID_TOP = "fixedMidTop";
    public static final String FIXED_MID = "fixedMid";
    public static final String FIXED_MID_BOTTOM = "fixedMidBottom";
    public static final String FIXED_BOTTOM = "fixedBottom";
    public static final String SWING_BOTTOM = "swingBottom";
    public static final String SWING_MID_BOTTOM = "swingMidBottom";
    public static final String SWING_MID = "swingMid";
    public static final String SWING_MID_TOP = "swingMidTop";
    public static final String SWING_TOP = "swingTop";
  }

  public static class Quiet {
    public static final String OFF = "off";
    public static final String MODE1 = "mode1";
    public static final String MODE2 = "mode2";
    public static final String MODE3 = "mode3";
  }

  public static class Turbo {
    public static final String OFF = "off";
    public static final String ON = "on";
  }

  public static class PowerSave {
    public static final String OFF = "off";
    public static final String ON = "on";
  }

  public static class SafetyHeating {
    public static final String OFF = "off";
    public static final String ON = "on";
  }

  // Vendor value mappings
  private static final Map<String, Map<String, Integer>> VENDOR_VALUES = new HashMap<>();

  static {
    Map<String, Integer> power = new HashMap<>();
    power.put(Power.OFF, 0);
    power.put(Power.ON, 1);
    VENDOR_VALUES.put("power", power);

    Map<String, Integer> mode = new HashMap<>();
    mode.put(Mode.AUTO, 0);
    mode.put(Mode.COOL, 1);
    mode.put(Mode.DRY, 2);
    mode.put(Mode.FAN_ONLY, 3);
    mode.put(Mode.HEAT, 4);
    VENDOR_VALUES.put("mode", mode);

    Map<String, Integer> temperatureUnit = new HashMap<>();
    temperatureUnit.put(TemperatureUnit.CELSIUS, 0);
    temperatureUnit.put(TemperatureUnit.FAHRENHEIT, 1);
    VENDOR_VALUES.put("temperatureUnit", temperatureUnit);

    // Temperature values (16-30 Celsius, mapped to 0-14)
    Map<String, Integer> temperature = new HashMap<>();
    for (int i = 16; i <= 30; i++) {
      temperature.put(String.valueOf(i), i - 16);
    }
    VENDOR_VALUES.put("temperature", temperature);

    // Current temperature values (0-50 Celsius, mapped to 0-50)
    Map<String, Integer> currentTemperature = new HashMap<>();
    for (int i = 0; i <= 50; i++) {
      currentTemperature.put(String.valueOf(i), i);
    }
    VENDOR_VALUES.put("currentTemperature", currentTemperature);

    Map<String, Integer> fanSpeed = new HashMap<>();
    fanSpeed.put(FanSpeed.AUTO, 0);
    fanSpeed.put(FanSpeed.LOW, 1);
    fanSpeed.put(FanSpeed.MEDIUM_LOW, 2);
    fanSpeed.put(FanSpeed.MEDIUM, 3);
    fanSpeed.put(FanSpeed.MEDIUM_HIGH, 4);
    fanSpeed.put(FanSpeed.HIGH, 5);
    VENDOR_VALUES.put("fanSpeed", fanSpeed);

    Map<String, Integer> air = new HashMap<>();
    air.put(Air.OFF, 0);
    air.put(Air.INSIDE, 1);
    air.put(Air.OUTSIDE, 2);
    air.put(Air.MODE3, 3);
    VENDOR_VALUES.put("air", air);

    Map<String, Integer> blow = new HashMap<>();
    blow.put(Blow.OFF, 0);
    blow.put(Blow.ON, 1);
    VENDOR_VALUES.put("blow", blow);

    Map<String, Integer> health = new HashMap<>();
    health.put(Health.OFF, 0);
    health.put(Health.ON, 1);
    VENDOR_VALUES.put("health", health);

    Map<String, Integer> sleep = new HashMap<>();
    sleep.put(Sleep.OFF, 0);
    sleep.put(Sleep.ON, 1);
    VENDOR_VALUES.put("sleep", sleep);

    Map<String, Integer> lights = new HashMap<>();
    lights.put(Lights.OFF, 0);
    lights.put(Lights.ON, 1);
    VENDOR_VALUES.put("lights", lights);

    Map<String, Integer> swingHor = new HashMap<>();
    swingHor.put(SwingHor.DEFAULT, 0);
    swingHor.put(SwingHor.FULL, 1);
    swingHor.put(SwingHor.FIXED_LEFT, 2);
    swingHor.put(SwingHor.FIXED_MID_LEFT, 3);
    swingHor.put(SwingHor.FIXED_MID, 4);
    swingHor.put(SwingHor.FIXED_MID_RIGHT, 5);
    swingHor.put(SwingHor.FIXED_RIGHT, 6);
    swingHor.put(SwingHor.FULL_ALT, 7);
    VENDOR_VALUES.put("swingHor", swingHor);

    Map<String, Integer> swingVert = new HashMap<>();
    swingVert.put(SwingVert.DEFAULT, 0);
    swingVert.put(SwingVert.FULL, 1);
    swingVert.put(SwingVert.FIXED_TOP, 2);
    swingVert.put(SwingVert.FIXED_MID_TOP, 3);
    swingVert.put(SwingVert.FIXED_MID, 4);
    swingVert.put(SwingVert.FIXED_MID_BOTTOM, 5);
    swingVert.put(SwingVert.FIXED_BOTTOM, 6);
    swingVert.put(SwingVert.SWING_BOTTOM, 7);
    swingVert.put(SwingVert.SWING_MID_BOTTOM, 8);
    swingVert.put(SwingVert.SWING_MID, 9);
    swingVert.put(SwingVert.SWING_MID_TOP, 10);
    swingVert.put(SwingVert.SWING_TOP, 11);
    VENDOR_VALUES.put("swingVert", swingVert);

    Map<String, Integer> quiet = new HashMap<>();
    quiet.put(Quiet.OFF, 0);
    quiet.put(Quiet.MODE1, 1);
    quiet.put(Quiet.MODE2, 2);
    quiet.put(Quiet.MODE3, 3);
    VENDOR_VALUES.put("quiet", quiet);

    Map<String, Integer> turbo = new HashMap<>();
    turbo.put(Turbo.OFF, 0);
    turbo.put(Turbo.ON, 1);
    VENDOR_VALUES.put("turbo", turbo);

    Map<String, Integer> powerSave = new HashMap<>();
    powerSave.put(PowerSave.OFF, 0);
    powerSave.put(PowerSave.ON, 1);
    VENDOR_VALUES.put("powerSave", powerSave);

    Map<String, Integer> safetyHeating = new HashMap<>();
    safetyHeating.put(SafetyHeating.OFF, 0);
    safetyHeating.put(SafetyHeating.ON, 1);
    VENDOR_VALUES.put("safetyHeating", safetyHeating);
  }

  public static Map<String, Map<String, Integer>> getVendorValues() {
    // Return immutable maps to prevent modification
    Map<String, Map<String, Integer>> immutableVendorValues = new HashMap<>();
    for (Map.Entry<String, Map<String, Integer>> entry : VENDOR_VALUES.entrySet()) {
      immutableVendorValues.put(entry.getKey(), Collections.unmodifiableMap(entry.getValue()));
    }
    return Collections.unmodifiableMap(immutableVendorValues);
  }
}
