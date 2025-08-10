package com.gree.hvac;

import com.gree.hvac.client.HvacClient;
import com.gree.hvac.client.HvacClientOptions;
import com.gree.hvac.discovery.HvacDiscovery;
import com.gree.hvac.dto.DeviceInfo;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Main facade class for GREE HVAC library
 *
 * <p>This class provides convenient static methods for common operations: - Device discovery -
 * Client creation - Library version information
 */
public final class GreeHvac {

  public static final String VERSION = "1.0.0";

  private GreeHvac() {
    // Utility class
  }

  /**
   * Discover GREE HVAC devices on the network
   *
   * @return CompletableFuture containing list of discovered devices
   */
  public static CompletableFuture<List<DeviceInfo>> discoverDevices() {
    return HvacDiscovery.discoverDevices();
  }

  /**
   * Discover GREE HVAC devices on specific broadcast address
   *
   * @param broadcastAddress the broadcast address to scan
   * @return CompletableFuture containing list of discovered devices
   */
  public static CompletableFuture<List<DeviceInfo>> discoverDevices(String broadcastAddress) {
    return HvacDiscovery.discoverDevices(broadcastAddress);
  }

  /**
   * Create HVAC client for specific device
   *
   * @param host the device IP address or hostname
   * @return HvacClient instance
   */
  public static HvacClient createClient(String host) {
    return new HvacClient(host);
  }

  /**
   * Create HVAC client with custom options
   *
   * @param options client configuration options
   * @return HvacClient instance
   */
  public static HvacClient createClient(HvacClientOptions options) {
    return new HvacClient(options);
  }

  /**
   * Create HVAC client for discovered device
   *
   * @param deviceInfo discovered device information
   * @return HvacClient instance
   */
  public static HvacClient createClient(DeviceInfo deviceInfo) {
    return new HvacClient(deviceInfo.getIpAddress());
  }

  /**
   * Get library version
   *
   * @return version string
   */
  public static String getVersion() {
    return VERSION;
  }
}
