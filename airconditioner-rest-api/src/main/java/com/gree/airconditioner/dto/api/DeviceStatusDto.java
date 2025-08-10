package com.gree.airconditioner.dto.api;

import lombok.Data;

@Data
public class DeviceStatusDto {
  private String deviceId;
  private boolean power;
  private Integer temperature;
  private Integer currentTemperature;
  private String mode;
  private String fanSpeed;
  private String swingHorizontal;
  private String swingVertical;
  private boolean lights;
  private boolean turbo;
  private boolean quiet;
  private boolean health;
  private boolean powerSave;
  private boolean sleep;
}
