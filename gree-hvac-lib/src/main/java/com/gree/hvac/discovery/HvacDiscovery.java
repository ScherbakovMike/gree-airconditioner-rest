package com.gree.hvac.discovery;

import com.gree.hvac.dto.DeviceInfo;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

/** Service for discovering GREE HVAC devices on the network */
@Slf4j
public class HvacDiscovery {

  private static final int DISCOVERY_PORT = 7000;
  private static final int DISCOVERY_TIMEOUT = 3000;
  private static final String DISCOVERY_COMMAND = "{\"t\":\"scan\"}";

  /** Discover GREE HVAC devices on all network interfaces */
  public static CompletableFuture<List<DeviceInfo>> discoverDevices() {
    return CompletableFuture.supplyAsync(
        () -> {
          log.info("Starting HVAC device discovery");
          List<DeviceInfo> devices = new ArrayList<>();

          try {
            devices.addAll(findDevicesOnAllNetworkInterfaces());
          } catch (Exception e) {
            log.error("Error during device discovery", e);
          }

          log.info("Device discovery completed. Found {} devices", devices.size());
          return devices;
        });
  }

  /** Discover GREE HVAC devices on a specific broadcast address */
  public static CompletableFuture<List<DeviceInfo>> discoverDevices(String broadcastAddress) {
    return CompletableFuture.supplyAsync(
        () -> {
          log.info("Starting HVAC device discovery on {}", broadcastAddress);
          List<DeviceInfo> devices = new ArrayList<>();

          try {
            InetAddress broadcast = InetAddress.getByName(broadcastAddress);
            devices.addAll(findDevicesOnBroadcastAddress(broadcast));
          } catch (Exception e) {
            log.error("Error during device discovery on {}", broadcastAddress, e);
          }

          log.info(
              "Device discovery completed on {}. Found {} devices",
              broadcastAddress,
              devices.size());
          return devices;
        });
  }

  private static List<DeviceInfo> findDevicesOnAllNetworkInterfaces() {
    List<DeviceInfo> allDevices = new ArrayList<>();

    try {
      Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
      while (interfaces.hasMoreElements()) {
        NetworkInterface networkInterface = interfaces.nextElement();

        if (!networkInterface.isLoopback() && networkInterface.isUp()) {
          for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
            InetAddress broadcast = interfaceAddress.getBroadcast();
            if (broadcast != null) {
              log.debug(
                  "Scanning network interface: {} with broadcast: {}",
                  networkInterface.getName(),
                  broadcast.getHostAddress());
              allDevices.addAll(findDevicesOnBroadcastAddress(broadcast));
            }
          }
        }
      }
    } catch (Exception e) {
      log.error("Error enumerating network interfaces", e);
    }

    return allDevices;
  }

  private static List<DeviceInfo> findDevicesOnBroadcastAddress(InetAddress broadcastAddress) {
    List<DeviceInfo> devices = new ArrayList<>();

    try (DatagramSocket socket = new DatagramSocket()) {
      socket.setBroadcast(true);
      socket.setSoTimeout(DISCOVERY_TIMEOUT);

      // Send scan command
      byte[] scanData = DISCOVERY_COMMAND.getBytes(StandardCharsets.UTF_8);
      DatagramPacket sendPacket =
          new DatagramPacket(scanData, scanData.length, broadcastAddress, DISCOVERY_PORT);

      log.debug("Sending scan command to: {}", broadcastAddress.getHostAddress());
      socket.send(sendPacket);

      long endTime = System.currentTimeMillis() + DISCOVERY_TIMEOUT;
      byte[] receiveData = new byte[1024];

      while (System.currentTimeMillis() < endTime) {
        try {
          DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
          socket.receive(receivePacket);

          String response =
              new String(
                  receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8);
          log.debug(
              "Received response from {}: {}",
              receivePacket.getAddress().getHostAddress(),
              response);

          DeviceInfo device = parseDeviceResponse(response, receivePacket.getAddress());
          if (device != null) {
            devices.add(device);
            log.info("Discovered device: {} at {}", device.getName(), device.getIpAddress());
          }

        } catch (SocketTimeoutException e) {
          break;
        } catch (Exception e) {
          log.debug("Error processing response packet: {}", e.getMessage());
        }
      }

    } catch (Exception e) {
      log.error(
          "Error during device discovery on broadcast {}: {}",
          broadcastAddress.getHostAddress(),
          e.getMessage());
    }

    return devices;
  }

  private static DeviceInfo parseDeviceResponse(String response, InetAddress sourceAddress) {
    try {
      // Parse JSON response
      JSONObject json = new JSONObject(response);

      if (!"pack".equals(json.optString("t"))) {
        return null;
      }

      String encryptedPack = json.optString("pack");
      if (encryptedPack == null || encryptedPack.isEmpty()) {
        return null;
      }

      // Decrypt the pack using the generic key
      String decryptedData = decryptPackData(encryptedPack);
      if (decryptedData == null) {
        return null;
      }

      JSONObject deviceData = new JSONObject(decryptedData);

      if (!"dev".equals(deviceData.optString("t"))) {
        return null;
      }

      DeviceInfo device = new DeviceInfo();
      device.setId(sourceAddress.getHostAddress());
      device.setName(deviceData.optString("name", "Unknown Device"));
      device.setBrand("Gree");
      device.setModel("Unknown");
      device.setVersion(deviceData.optString("ver", "Unknown"));
      device.setMacAddress(deviceData.optString("mac", ""));
      device.setIpAddress(sourceAddress.getHostAddress());
      device.setConnected(false);
      device.setStatus("Discovered");

      return device;

    } catch (Exception e) {
      log.debug("Error parsing device response: {}", e.getMessage());
      return null;
    }
  }

  /**
   * Decrypt discovery packet data using GREE protocol encryption. Note: Uses AES/ECB mode as
   * required by GREE device firmware. This is a protocol requirement and cannot be changed.
   */
  private static String decryptPackData(String encryptedData) {
    try {
      // Generic key used for discovery
      String genericKey = "a3K8Bx%2r8Y7#xDh";

      byte[] encrypted = Base64.getDecoder().decode(encryptedData);

      // SonarQube: AES/ECB required for GREE protocol - cannot use secure mode
      javax.crypto.Cipher cipher =
          javax.crypto.Cipher.getInstance("AES/ECB/PKCS5Padding"); // NOSONAR
      javax.crypto.spec.SecretKeySpec keySpec =
          new javax.crypto.spec.SecretKeySpec(genericKey.getBytes(StandardCharsets.UTF_8), "AES");
      cipher.init(javax.crypto.Cipher.DECRYPT_MODE, keySpec);

      byte[] decrypted = cipher.doFinal(encrypted);
      return new String(decrypted, StandardCharsets.UTF_8);

    } catch (Exception e) {
      log.debug("Error decrypting pack data: {}", e.getMessage());
      return null;
    }
  }
}
