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
    HvacDiscovery discovery = new HvacDiscovery();
    return discovery.discoverDevices();
  }

  /**
   * Discover GREE HVAC devices on specific broadcast address
   *
   * @param broadcastAddress the broadcast address to scan
   * @return CompletableFuture containing list of discovered devices
   */
  public static CompletableFuture<List<DeviceInfo>> discoverDevices(String broadcastAddress) {
    HvacDiscovery discovery = new HvacDiscovery();
    return discovery.discoverDevices(broadcastAddress);
  }

  /**
   * Create HVAC client for specific device
   *
   * @param host the device IP address or hostname
   * @return HvacClient instance
   * @throws NullPointerException if host is null
   */
  public static HvacClient createClient(String host) {
    if (host == null) {
      throw new NullPointerException("Host cannot be null");
    }
    return new HvacClient(host);
  }

  /**
   * Create HVAC client with custom options
   *
   * @param options client configuration options
   * @return HvacClient instance
   * @throws NullPointerException if options is null
   */
  public static HvacClient createClient(HvacClientOptions options) {
    if (options == null) {
      throw new NullPointerException("Options cannot be null");
    }
    return new HvacClient(options);
  }

  /**
   * Create HVAC client for discovered device
   *
   * @param deviceInfo discovered device information
   * @return HvacClient instance
   * @throws NullPointerException if deviceInfo is null
   */
  public static HvacClient createClient(DeviceInfo deviceInfo) {
    if (deviceInfo == null) {
      throw new NullPointerException("DeviceInfo cannot be null");
    }
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
