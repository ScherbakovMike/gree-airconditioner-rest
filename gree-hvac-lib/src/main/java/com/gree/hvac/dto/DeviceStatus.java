package com.gree.hvac.dto;

import lombok.Data;

/** Current status and properties of a GREE HVAC device */
@Data
public class DeviceStatus {

  /** Device identifier */
  private String deviceId;

  /** Power state (true = on, false = off) */
  private Boolean power;

  /** Target temperature in Celsius (16-30) */
  private Integer temperature;

  /** Current temperature reading from device */
  private Integer currentTemperature;

  /** Operation mode (AUTO, COOL, HEAT, DRY, FAN_ONLY) */
  private String mode;

  /** Fan speed (AUTO, LOW, MEDIUM, HIGH) */
  private String fanSpeed;

  /** Horizontal swing setting */
  private String swingHorizontal;

  /** Vertical swing setting */
  private String swingVertical;

  /** Display lights state */
  private Boolean lights;

  /** Turbo mode state */
  private Boolean turbo;

  /** Quiet mode state */
  private Boolean quiet;

  /** Health mode state */
  private Boolean health;

  /** Power save mode state */
  private Boolean powerSave;

  /** Sleep mode state */
  private Boolean sleep;
}
