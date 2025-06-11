package com.gree.airconditioner;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GreeAirconditionerDevice {

  private DeviceInfo deviceInfo;
  private ConnectionInfo connectionInfo;

  public GreeAirconditionerDevice(DeviceInfo device, ConnectionInfo connection) {
    this.deviceInfo = device;
    this.connectionInfo = connection;
  }

  public DeviceInfo getDeviceInfo() {
    return deviceInfo;
  }

  public ConnectionInfo getConnectionInfo() {
    return connectionInfo;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    GreeAirconditionerDevice that = (GreeAirconditionerDevice) o;
    return Objects.equals(connectionInfo, that.connectionInfo);
  }

  @Override
  public int hashCode() {

    return Objects.hash(connectionInfo);
  }
}
