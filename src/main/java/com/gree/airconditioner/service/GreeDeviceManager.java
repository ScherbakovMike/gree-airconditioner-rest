package com.gree.airconditioner.service;

import com.gree.airconditioner.dto.api.DeviceInfoDto;
import com.gree.airconditioner.dto.api.DeviceStatusDto;
import com.gree.airconditioner.dto.api.DeviceControlDto;
import com.gree.airconditioner.gree.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GreeDeviceManager {
    
    private final Map<String, Client> connectedClients = new ConcurrentHashMap<>();
    private final Map<String, DeviceInfoDto> discoveredDevices = new ConcurrentHashMap<>();
    
    /**
     * Discover GREE devices on the network
     */
    public CompletableFuture<List<DeviceInfoDto>> discoverDevices() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Starting device discovery...");
                List<DeviceInfoDto> devices = new ArrayList<>();
                
                devices.addAll(findDevicesOnAllNetworkInterfaces());
                
                log.info("Device discovery completed. Found {} devices", devices.size());
                return devices;
                
            } catch (Exception e) {
                log.error("Error during device discovery", e);
                return new ArrayList<>();
            }
        });
    }
    
    private List<DeviceInfoDto> findDevicesOnAllNetworkInterfaces() {
        List<DeviceInfoDto> allDevices = new ArrayList<>();
        
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                
                if (!networkInterface.isLoopback() && networkInterface.isUp()) {
                    for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                        InetAddress broadcast = interfaceAddress.getBroadcast();
                        if (broadcast != null) {
                            log.info("Scanning network interface: {} with broadcast: {}", 
                                    networkInterface.getName(), broadcast.getHostAddress());
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
    
    private List<DeviceInfoDto> findDevicesOnBroadcastAddress(InetAddress broadcastAddress) {
        List<DeviceInfoDto> devices = new ArrayList<>();
        
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(3000);
            
            // Send scan command
            String scanCommand = "{\"t\":\"scan\"}";
            byte[] scanData = scanCommand.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(
                scanData, scanData.length, broadcastAddress, 7000);
            
            log.info("Sending scan command to: {}", broadcastAddress.getHostAddress());
            socket.send(sendPacket);
            
            long endTime = System.currentTimeMillis() + 3000;
            byte[] receiveData = new byte[1024];
            
            while (System.currentTimeMillis() < endTime) {
                try {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    socket.receive(receivePacket);
                    
                    String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    log.info("Received response from {}: {}", 
                            receivePacket.getAddress().getHostAddress(), response);
                    
                    DeviceInfoDto device = parseDeviceResponse(response, receivePacket.getAddress());
                    if (device != null) {
                        devices.add(device);
                        discoveredDevices.put(device.getIpAddress(), device);
                        log.info("Added device: {} at {}", device.getName(), device.getIpAddress());
                    }
                    
                } catch (SocketTimeoutException e) {
                    break;
                } catch (Exception e) {
                    log.debug("Error processing response packet: {}", e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("Error during device discovery on broadcast {}: {}", 
                    broadcastAddress.getHostAddress(), e.getMessage());
        }
        
        return devices;
    }
    
    private DeviceInfoDto parseDeviceResponse(String response, InetAddress sourceAddress) {
        try {
            // Parse JSON response
            org.json.JSONObject json = new org.json.JSONObject(response);
            
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
            
            org.json.JSONObject deviceData = new org.json.JSONObject(decryptedData);
            
            if (!"dev".equals(deviceData.optString("t"))) {
                return null;
            }
            
            DeviceInfoDto device = new DeviceInfoDto();
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
    
    private String decryptPackData(String encryptedData) {
        try {
            // Generic key used for discovery
            String genericKey = "a3K8Bx%2r8Y7#xDh";
            
            byte[] encrypted = java.util.Base64.getDecoder().decode(encryptedData);
            
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/ECB/PKCS5Padding");
            javax.crypto.spec.SecretKeySpec keySpec = new javax.crypto.spec.SecretKeySpec(
                genericKey.getBytes(StandardCharsets.UTF_8), "AES");
            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, keySpec);
            
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.debug("Error decrypting pack data: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Get list of all discovered devices
     */
    public List<DeviceInfoDto> getDevices() {
        return new ArrayList<>(discoveredDevices.values());
    }
    
    /**
     * Connect to a specific device
     */
    public CompletableFuture<Boolean> connectToDevice(String deviceId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (connectedClients.containsKey(deviceId)) {
                    log.info("Device {} is already connected", deviceId);
                    return true;
                }
                
                DeviceInfoDto deviceInfo = discoveredDevices.get(deviceId);
                if (deviceInfo == null) {
                    log.error("Device {} not found in discovered devices", deviceId);
                    return false;
                }
                
                log.info("Connecting to device: {}", deviceId);
                
                ClientOptions options = new ClientOptions(deviceId)
                        .setPort(7000)
                        .setConnectTimeout(5000)
                        .setAutoConnect(false)
                        .setPoll(false)
                        .setDebug(false)
                        .setLogLevel("info");
                
                Client client = new Client(options);
                
                // Setup event listeners
                client.onConnect(() -> {
                    log.info("Successfully connected to device: {}", deviceId);
                    deviceInfo.setConnected(true);
                    deviceInfo.setStatus("Connected");
                });
                
                client.onDisconnect(() -> {
                    log.info("Disconnected from device: {}", deviceId);
                    deviceInfo.setConnected(false);
                    deviceInfo.setStatus("Disconnected");
                    connectedClients.remove(deviceId);
                });
                
                client.onError((error) -> {
                    log.error("Error from device {}: {}", deviceId, error.getMessage());
                });
                
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
    
    /**
     * Disconnect from a specific device
     */
    public CompletableFuture<Boolean> disconnectFromDevice(String deviceId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Client client = connectedClients.get(deviceId);
                if (client == null) {
                    log.warn("Device {} is not connected", deviceId);
                    return false;
                }
                
                log.info("Disconnecting from device: {}", deviceId);
                client.disconnect().get();
                connectedClients.remove(deviceId);
                
                DeviceInfoDto deviceInfo = discoveredDevices.get(deviceId);
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
    
    /**
     * Get current status of a device
     */
    public CompletableFuture<DeviceStatusDto> getDeviceStatus(String deviceId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Client client = connectedClients.get(deviceId);
                if (client == null) {
                    throw new RuntimeException("Device " + deviceId + " is not connected");
                }
                
                Map<String, Object> properties = client.getProperties();
                
                DeviceStatusDto status = new DeviceStatusDto();
                status.setDeviceId(deviceId);
                status.setPower(PropertyValue.Power.ON.equals(properties.get("power")));
                status.setTemperature((Integer) properties.get("temperature"));
                status.setCurrentTemperature((Integer) properties.get("currentTemperature"));
                status.setMode(properties.get("mode") != null ? properties.get("mode").toString() : null);
                status.setFanSpeed(properties.get("fanSpeed") != null ? properties.get("fanSpeed").toString() : null);
                status.setSwingHorizontal(properties.get("swingHor") != null ? properties.get("swingHor").toString() : null);
                status.setSwingVertical(properties.get("swingVert") != null ? properties.get("swingVert").toString() : null);
                status.setLights(PropertyValue.Lights.ON.equals(properties.get("lights")));
                status.setTurbo(Boolean.TRUE.equals(properties.get("turbo")));
                status.setQuiet(Boolean.TRUE.equals(properties.get("quiet")));
                status.setHealth(Boolean.TRUE.equals(properties.get("health")));
                status.setPowerSave(Boolean.TRUE.equals(properties.get("powerSave")));
                status.setSleep(Boolean.TRUE.equals(properties.get("sleep")));
                
                return status;
                
            } catch (Exception e) {
                log.error("Failed to get status for device {}: {}", deviceId, e.getMessage());
                throw new RuntimeException("Failed to get device status: " + e.getMessage());
            }
        });
    }
    
    /**
     * Control device properties
     */
    public CompletableFuture<Boolean> controlDevice(String deviceId, DeviceControlDto control) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Client client = connectedClients.get(deviceId);
                if (client == null) {
                    throw new RuntimeException("Device " + deviceId + " is not connected");
                }
                
                log.info("Controlling device {}: {}", deviceId, control);
                
                Map<String, Object> properties = new HashMap<>();
                
                if (control.getPower() != null) {
                    properties.put("power", control.getPower() ? PropertyValue.Power.ON : PropertyValue.Power.OFF);
                }
                
                if (control.getTemperature() != null) {
                    properties.put("temperature", control.getTemperature());
                }
                
                if (control.getMode() != null) {
                    properties.put("mode", parseMode(control.getMode()));
                }
                
                if (control.getFanSpeed() != null) {
                    properties.put("fanSpeed", parseFanSpeed(control.getFanSpeed()));
                }
                
                if (control.getSwingHorizontal() != null) {
                    properties.put("swingHor", parseSwingHorizontal(control.getSwingHorizontal()));
                }
                
                if (control.getSwingVertical() != null) {
                    properties.put("swingVert", parseSwingVertical(control.getSwingVertical()));
                }
                
                if (control.getLights() != null) {
                    properties.put("lights", control.getLights() ? PropertyValue.Lights.ON : PropertyValue.Lights.OFF);
                }
                
                if (control.getTurbo() != null) {
                    properties.put("turbo", control.getTurbo());
                }
                
                if (control.getQuiet() != null) {
                    properties.put("quiet", control.getQuiet());
                }
                
                if (control.getHealth() != null) {
                    properties.put("health", control.getHealth());
                }
                
                if (control.getPowerSave() != null) {
                    properties.put("powerSave", control.getPowerSave());
                }
                
                if (control.getSleep() != null) {
                    properties.put("sleep", control.getSleep());
                }
                
                if (properties.isEmpty()) {
                    log.warn("No properties to update for device {}", deviceId);
                    return false;
                }
                
                client.setProperties(properties).get();
                log.info("Successfully updated properties for device {}", deviceId);
                return true;
                
            } catch (Exception e) {
                log.error("Failed to control device {}: {}", deviceId, e.getMessage());
                throw new RuntimeException("Failed to control device: " + e.getMessage());
            }
        });
    }
    
    private Object parseMode(String mode) {
        switch (mode.toUpperCase()) {
            case "AUTO": return PropertyValue.Mode.AUTO;
            case "COOL": return PropertyValue.Mode.COOL;
            case "HEAT": return PropertyValue.Mode.HEAT;
            case "DRY": return PropertyValue.Mode.DRY;
            case "FAN_ONLY": return PropertyValue.Mode.FAN_ONLY;
            default: throw new IllegalArgumentException("Invalid mode: " + mode);
        }
    }
    
    private Object parseFanSpeed(String fanSpeed) {
        switch (fanSpeed.toUpperCase()) {
            case "AUTO": return PropertyValue.FanSpeed.AUTO;
            case "LOW": return PropertyValue.FanSpeed.LOW;
            case "MEDIUM": return PropertyValue.FanSpeed.MEDIUM;
            case "HIGH": return PropertyValue.FanSpeed.HIGH;
            default: throw new IllegalArgumentException("Invalid fan speed: " + fanSpeed);
        }
    }
    
    private Object parseSwingHorizontal(String swing) {
        switch (swing.toUpperCase()) {
            case "DEFAULT": return PropertyValue.SwingHor.DEFAULT;
            case "FULL": return PropertyValue.SwingHor.FULL;
            case "FIXED_LEFT": return PropertyValue.SwingHor.FIXED_LEFT;
            case "FIXED_RIGHT": return PropertyValue.SwingHor.FIXED_RIGHT;
            default: throw new IllegalArgumentException("Invalid horizontal swing: " + swing);
        }
    }
    
    private Object parseSwingVertical(String swing) {
        switch (swing.toUpperCase()) {
            case "DEFAULT": return PropertyValue.SwingVert.DEFAULT;
            case "FULL": return PropertyValue.SwingVert.FULL;
            case "FIXED_TOP": return PropertyValue.SwingVert.FIXED_TOP;
            case "FIXED_BOTTOM": return PropertyValue.SwingVert.FIXED_BOTTOM;
            default: throw new IllegalArgumentException("Invalid vertical swing: " + swing);
        }
    }
    
    /**
     * Cleanup - disconnect all devices
     */
    public void shutdown() {
        log.info("Shutting down device manager...");
        connectedClients.values().forEach(client -> {
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
}