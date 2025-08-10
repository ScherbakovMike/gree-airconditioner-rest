package com.gree.hvac.dto;

import lombok.Data;

/** Device control parameters for sending commands to GREE HVAC device */
@Data
public class DeviceControl {

  /** Power state (true = on, false = off) */
  private Boolean power;

  /** Target temperature in Celsius (16-30) */
  private Integer temperature;

  /** Operation mode Valid values: AUTO, COOL, HEAT, DRY, FAN_ONLY */
  private String mode;

  /** Fan speed setting Valid values: AUTO, LOW, MEDIUM, HIGH */
  private String fanSpeed;

  /** Horizontal swing setting Valid values: DEFAULT, FULL, FIXED_LEFT, FIXED_RIGHT */
  private String swingHorizontal;

  /** Vertical swing setting Valid values: DEFAULT, FULL, FIXED_TOP, FIXED_BOTTOM */
  private String swingVertical;

  /** Display panel lights */
  private Boolean lights;

  /** Turbo mode (maximum cooling/heating) */
  private Boolean turbo;

  /** Quiet operation mode */
  private Boolean quiet;

  /** Health mode (air purification) */
  private Boolean health;

  /** Power saving mode */
  private Boolean powerSave;

  /** Sleep mode */
  private Boolean sleep;
}
