package com.gree.airconditioner.service;

import com.gree.airconditioner.dto.api.DeviceControlDto;
import com.gree.airconditioner.dto.api.DeviceInfoDto;
import com.gree.airconditioner.dto.api.DeviceStatusDto;
import com.gree.hvac.GreeHvac;
import com.gree.hvac.client.HvacClient;
import com.gree.hvac.client.HvacClientOptions;
import com.gree.hvac.dto.DeviceControl;
import com.gree.hvac.dto.DeviceInfo;
import com.gree.hvac.dto.DeviceStatus;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Service layer that bridges REST API DTOs with GREE HVAC library */
@Slf4j
@Service
public class HvacDeviceService {

  private final Map<String, HvacClient> connectedClients = new ConcurrentHashMap<>();
  private final Map<String, DeviceInfo> discoveredDevices = new ConcurrentHashMap<>();

  /** Discover GREE devices on the network */
  public CompletableFuture<List<DeviceInfoDto>> discoverDevices() {
    return GreeHvac.discoverDevices()
        .thenApply(
            devices -> {
              // Cache discovered devices
              devices.forEach(device -> discoveredDevices.put(device.getId(), device));

              // Convert to API DTOs
              return devices.stream().map(this::convertToApiDto).collect(Collectors.toList());
            });
  }

  /** Get all discovered devices */
  public List<DeviceInfoDto> getDevices() {
    return discoveredDevices.values().stream()
        .map(this::convertToApiDto)
        .collect(Collectors.toList());
  }

  /** Connect to a specific device */
  public CompletableFuture<Boolean> connectToDevice(String deviceId) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            if (connectedClients.containsKey(deviceId)) {
              log.info("Device {} is already connected", deviceId);
              return true;
            }

            DeviceInfo deviceInfo = discoveredDevices.get(deviceId);
            if (deviceInfo == null) {
              log.error("Device {} not found in discovered devices", deviceId);
              return false;
            }

            log.info("Connecting to device: {}", deviceId);

            // Create client with autoConnect disabled to prevent race conditions
            HvacClient client =
                GreeHvac.createClient(
                    new HvacClientOptions(deviceInfo.getIpAddress())
                        .setAutoConnect(false)
                        .setPoll(true)
                        .setPollingInterval(5000));

            // Setup event listeners
            client.onConnect(
                () -> {
                  log.info("Successfully connected to device: {}", deviceId);
                  deviceInfo.setConnected(true);
                  deviceInfo.setStatus("Connected");
                });

            client.onDisconnect(
                () -> {
                  log.info("Disconnected from device: {}", deviceId);
                  deviceInfo.setConnected(false);
                  deviceInfo.setStatus("Disconnected");
                  connectedClients.remove(deviceId);
                });

            client.onError(
                error -> log.error("Error from device {}: {}", deviceId, error.getMessage()));

            // Connect to the device
            client.connect().get();
            connectedClients.put(deviceId, client);

            log.info("Device {} connected successfully", deviceId);
            return true;

          } catch (Exception e) {
            log.error("Failed to connect to device {}: {}", deviceId, e.getMessage());
            return false;
          }
        });
  }

  /** Disconnect from a specific device */
  public CompletableFuture<Boolean> disconnectFromDevice(String deviceId) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            HvacClient client = connectedClients.get(deviceId);
            if (client == null) {
              log.warn("Device {} is not connected", deviceId);
              return false;
            }

            log.info("Disconnecting from device: {}", deviceId);
            client.disconnect().get();
            client.shutdown();
            connectedClients.remove(deviceId);

            DeviceInfo deviceInfo = discoveredDevices.get(deviceId);
            if (deviceInfo != null) {
              deviceInfo.setConnected(false);
              deviceInfo.setStatus("Disconnected");
            }

            return true;

          } catch (Exception e) {
            log.error("Failed to disconnect from device {}: {}", deviceId, e.getMessage());
            return false;
          }
        });
  }

  /** Get current status of a device */
  public CompletableFuture<DeviceStatusDto> getDeviceStatus(String deviceId) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            HvacClient client = connectedClients.get(deviceId);
            if (client == null) {
              throw new RuntimeException("Device " + deviceId + " is not connected");
            }

            DeviceStatus status = client.getStatus();
            return convertToApiDto(status);

          } catch (Exception e) {
            log.error("Failed to get status for device {}: {}", deviceId, e.getMessage());
            throw new RuntimeException("Failed to get device status: " + e.getMessage());
          }
        });
  }

  /** Control device properties */
  public CompletableFuture<Boolean> controlDevice(String deviceId, DeviceControlDto controlDto) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            HvacClient client = connectedClients.get(deviceId);
            if (client == null) {
              throw new RuntimeException("Device " + deviceId + " is not connected");
            }

            log.info("Controlling device {}: {}", deviceId, controlDto);

            DeviceControl control = convertFromApiDto(controlDto);
            client.control(control).get();

            log.info("Successfully controlled device: {}", deviceId);
            return true;

          } catch (Exception e) {
            log.error("Failed to control device {}: {}", deviceId, e.getMessage());
            throw new RuntimeException("Failed to control device: " + e.getMessage());
          }
        });
  }

  /** Cleanup - disconnect all devices */
  public void shutdown() {
    log.info("Shutting down HVAC device service...");
    connectedClients
        .values()
        .forEach(
            client -> {
              try {
                client.disconnect().get();
                client.shutdown();
              } catch (Exception e) {
                log.error("Error disconnecting client during shutdown", e);
              }
            });
    connectedClients.clear();
    discoveredDevices.clear();
  }

  // Conversion methods between library DTOs and API DTOs
  private DeviceInfoDto convertToApiDto(DeviceInfo deviceInfo) {
    DeviceInfoDto dto = new DeviceInfoDto();
    dto.setId(deviceInfo.getId());
    dto.setName(deviceInfo.getName());
    dto.setBrand(deviceInfo.getBrand());
    dto.setModel(deviceInfo.getModel());
    dto.setVersion(deviceInfo.getVersion());
    dto.setMacAddress(deviceInfo.getMacAddress());
    dto.setIpAddress(deviceInfo.getIpAddress());
    dto.setConnected(deviceInfo.isConnected());
    dto.setStatus(deviceInfo.getStatus());
    return dto;
  }

  private DeviceStatusDto convertToApiDto(DeviceStatus status) {
    DeviceStatusDto dto = new DeviceStatusDto();
    dto.setDeviceId(status.getDeviceId());
    dto.setPower(status.getPower());
    dto.setTemperature(status.getTemperature());
    dto.setCurrentTemperature(status.getCurrentTemperature());
    dto.setMode(status.getMode());
    dto.setFanSpeed(status.getFanSpeed());
    dto.setSwingHorizontal(status.getSwingHorizontal());
    dto.setSwingVertical(status.getSwingVertical());
    dto.setLights(status.getLights());
    dto.setTurbo(status.getTurbo());
    dto.setQuiet(status.getQuiet());
    dto.setHealth(status.getHealth());
    dto.setPowerSave(status.getPowerSave());
    dto.setSleep(status.getSleep());
    return dto;
  }

  private DeviceControl convertFromApiDto(DeviceControlDto dto) {
    DeviceControl control = new DeviceControl();
    control.setPower(dto.getPower());
    control.setTemperature(dto.getTemperature());
    control.setMode(dto.getMode());
    control.setFanSpeed(dto.getFanSpeed());
    control.setSwingHorizontal(dto.getSwingHorizontal());
    control.setSwingVertical(dto.getSwingVertical());
    control.setLights(dto.getLights());
    control.setTurbo(dto.getTurbo());
    control.setQuiet(dto.getQuiet());
    control.setHealth(dto.getHealth());
    control.setPowerSave(dto.getPowerSave());
    control.setSleep(dto.getSleep());
    return control;
  }
}
