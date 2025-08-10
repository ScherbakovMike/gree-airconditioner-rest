package com.gree.airconditioner.dto.api;

import lombok.Data;

@Data
public class DeviceInfoDto {
  private String id;
  private String name;
  private String brand;
  private String model;
  private String version;
  private String macAddress;
  private String ipAddress;
  private boolean connected;
  private String status;
}
