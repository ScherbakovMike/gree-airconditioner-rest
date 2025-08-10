package com.gree.hvac.dto;

import lombok.Data;

/** Information about a discovered GREE HVAC device */
@Data
public class DeviceInfo {

  /** Device identifier (typically IP address) */
  private String id;

  /** Human-readable device name */
  private String name;

  /** Device brand (typically "Gree") */
  private String brand;

  /** Device model */
  private String model;

  /** Firmware version */
  private String version;

  /** Device MAC address */
  private String macAddress;

  /** Device IP address */
  private String ipAddress;

  /** Whether the device is currently connected */
  private boolean connected;

  /** Current connection status */
  private String status;
}
