package com.gree.hvac.protocol;

/** Device properties constants */
public enum Property {
  POWER("power"),
  MODE("mode"),
  TEMPERATURE_UNIT("temperatureUnit"),
  TEMPERATURE("temperature"),
  CURRENT_TEMPERATURE("currentTemperature"),
  FAN_SPEED("fanSpeed"),
  AIR("air"),
  BLOW("blow"),
  HEALTH("health"),
  SLEEP("sleep"),
  LIGHTS("lights"),
  SWING_HOR("swingHor"),
  SWING_VERT("swingVert"),
  QUIET("quiet"),
  TURBO("turbo"),
  POWER_SAVE("powerSave"),
  SAFETY_HEATING("safetyHeating");

  private final String value;

  Property(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }

  public static Property fromString(String text) {
    for (Property property : Property.values()) {
      if (property.value.equalsIgnoreCase(text)) {
        return property;
      }
    }
    throw new IllegalArgumentException("No constant with text " + text + " found");
  }
}
