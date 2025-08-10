package com.gree.hvac.client;

import com.gree.hvac.dto.DeviceControl;
import com.gree.hvac.dto.DeviceStatus;
import com.gree.hvac.exceptions.HvacException;
import com.gree.hvac.protocol.EncryptionService;
import com.gree.hvac.protocol.PropertyTransformer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;

/** GREE HVAC device client for controlling air conditioning units */
@Slf4j
public class HvacClient {

  private String deviceId; // Device MAC-address
  private DatagramSocket socket;
  private final HvacClientOptions options;
  private final Map<String, Object> properties = new ConcurrentHashMap<>();
  private final PropertyTransformer transformer = new PropertyTransformer();
  private EncryptionService encryptionService;

  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
  private ScheduledFuture<?> reconnectTask;
  private ScheduledFuture<?> pollingTask;
  private ScheduledFuture<?> statusTimeoutTask;

  private int reconnectAttempt = 1;
  private CompletableFuture<Void> connectFuture;
  private volatile boolean connected = false;

  // Event listeners
  private final List<Runnable> connectListeners = new ArrayList<>();
  private final List<Consumer<DeviceStatus>> statusUpdateListeners = new ArrayList<>();
  private final List<Consumer<Exception>> errorListeners = new ArrayList<>();
  private final List<Runnable> disconnectListeners = new ArrayList<>();
  private final List<Runnable> noResponseListeners = new ArrayList<>();

  /** Create HVAC client with configuration options */
  public HvacClient(HvacClientOptions options) {
    this.options = options != null ? options : new HvacClientOptions();
    this.encryptionService = new EncryptionService();

    log.info("Initialized HVAC client for host: {}", this.options.getHost());

    if (this.options.isAutoConnect()) {
      CompletableFuture.runAsync(
          () -> {
            try {
              connect().get();
            } catch (Exception e) {
              notifyError(e);
            }
          });
    }
  }

  /** Create HVAC client with host address */
  public HvacClient(String host) {
    this(new HvacClientOptions(host));
  }

  /** Connect to HVAC device */
  public CompletableFuture<Void> connect() {
    log.info("Connecting to HVAC device at {}:{}", options.getHost(), options.getPort());

    if (connectFuture != null && !connectFuture.isDone()) {
      return connectFuture;
    }

    // Reset connection state for new connection attempt
    connected = false;
    connectFuture = new CompletableFuture<>();

    try {
      socket = new DatagramSocket();
      socket.setBroadcast(true);

      // Start listening for responses
      startListening();

      // Initialize connection
      initialize();

    } catch (Exception e) {
      connectFuture.completeExceptionally(e);
    }

    return connectFuture;
  }

  /** Disconnect from HVAC device */
  public CompletableFuture<Void> disconnect() {
    log.info("Disconnecting from HVAC device");

    return CompletableFuture.runAsync(
        () -> {
          dispose();
          if (socket != null && !socket.isClosed()) {
            socket.close();
            socket = null;
          }
          connected = false;
          notifyDisconnect();
          log.info("Disconnected from HVAC device");
        });
  }

  /** Set device properties */
  public CompletableFuture<Void> setProperties(Map<String, Object> properties) {
    return CompletableFuture.runAsync(
        () -> {
          try {
            if (!connected) {
              throw new HvacException("Client is not connected to the HVAC device");
            }

            Map<String, Object> vendorProperties = transformer.toVendor(properties);

            JSONObject request = new JSONObject();
            request.put("opt", new JSONArray(vendorProperties.keySet()));
            request.put("p", new JSONArray(vendorProperties.values()));
            request.put("t", "cmd");

            log.debug("Setting properties: {}", properties.keySet());
            sendRequest(request);

          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
  }

  /** Control device with DeviceControl object */
  public CompletableFuture<Void> control(DeviceControl control) {
    return CompletableFuture.runAsync(
        () -> {
          try {
            Map<String, Object> properties = new HashMap<>();

            if (control.getPower() != null) {
              properties.put("power", control.getPower() ? "on" : "off");
            }

            if (control.getTemperature() != null) {
              properties.put("temperature", control.getTemperature());
            }

            if (control.getMode() != null) {
              properties.put("mode", control.getMode().toLowerCase());
            }

            if (control.getFanSpeed() != null) {
              properties.put("fanSpeed", control.getFanSpeed().toLowerCase());
            }

            if (control.getSwingHorizontal() != null) {
              properties.put("swingHor", control.getSwingHorizontal().toLowerCase());
            }

            if (control.getSwingVertical() != null) {
              properties.put("swingVert", control.getSwingVertical().toLowerCase());
            }

            if (control.getLights() != null) {
              properties.put("lights", control.getLights() ? "on" : "off");
            }

            if (control.getTurbo() != null) {
              properties.put("turbo", control.getTurbo() ? "on" : "off");
            }

            if (control.getQuiet() != null) {
              properties.put("quiet", control.getQuiet() ? "on" : "off");
            }

            if (control.getHealth() != null) {
              properties.put("health", control.getHealth() ? "on" : "off");
            }

            if (control.getPowerSave() != null) {
              properties.put("powerSave", control.getPowerSave() ? "on" : "off");
            }

            if (control.getSleep() != null) {
              properties.put("sleep", control.getSleep() ? "on" : "off");
            }

            if (properties.isEmpty()) {
              log.warn("No properties to update");
              return;
            }

            setProperties(properties).get();

          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
  }

  /** Get current device properties as DeviceStatus */
  public DeviceStatus getStatus() {
    Map<String, Object> currentProperties = getCurrentProperties();

    DeviceStatus status = new DeviceStatus();
    status.setDeviceId(deviceId);
    status.setPower("on".equals(currentProperties.get("power")));
    status.setTemperature((Integer) currentProperties.get("temperature"));
    status.setCurrentTemperature((Integer) currentProperties.get("currentTemperature"));
    status.setMode((String) currentProperties.get("mode"));
    status.setFanSpeed((String) currentProperties.get("fanSpeed"));
    status.setSwingHorizontal((String) currentProperties.get("swingHor"));
    status.setSwingVertical((String) currentProperties.get("swingVert"));
    status.setLights("on".equals(currentProperties.get("lights")));
    status.setTurbo("on".equals(currentProperties.get("turbo")));
    status.setQuiet("on".equals(currentProperties.get("quiet")));
    status.setHealth("on".equals(currentProperties.get("health")));
    status.setPowerSave("on".equals(currentProperties.get("powerSave")));
    status.setSleep("on".equals(currentProperties.get("sleep")));

    return status;
  }

  /** Get current device properties as raw map */
  public Map<String, Object> getCurrentProperties() {
    return new HashMap<>(transformer.fromVendor(properties));
  }

  /** Check if client is connected */
  public boolean isConnected() {
    return connected;
  }

  /** Get device ID */
  public String getDeviceId() {
    return deviceId;
  }

  // Event listener registration methods
  public void onConnect(Runnable listener) {
    connectListeners.add(listener);
  }

  public void onStatusUpdate(Consumer<DeviceStatus> listener) {
    statusUpdateListeners.add(listener);
  }

  public void onError(Consumer<Exception> listener) {
    errorListeners.add(listener);
  }

  public void onDisconnect(Runnable listener) {
    disconnectListeners.add(listener);
  }

  public void onNoResponse(Runnable listener) {
    noResponseListeners.add(listener);
  }

  /** Shutdown client and cleanup resources */
  public void shutdown() {
    log.info("Shutting down HVAC client");
    dispose();
    scheduler.shutdown();
    try {
      if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
        scheduler.shutdownNow();
      }
    } catch (InterruptedException e) {
      scheduler.shutdownNow();
    }
  }

  // Private implementation methods
  private void initialize() {
    dispose();

    try {
      encryptionService = new EncryptionService();
      log.debug("Starting device scan (attempt {})", reconnectAttempt);

      JSONObject scanMessage = new JSONObject();
      scanMessage.put("t", "scan");
      socketSend(scanMessage);

      scheduleReconnect();

    } catch (Exception e) {
      scheduleReconnect();
      throw new RuntimeException(e);
    }
  }

  private void scheduleReconnect() {
    reconnectTask =
        scheduler.schedule(
            () -> {
              log.warn("Connect timeout, reconnect (timeout: {}ms)", options.getConnectTimeout());
              reconnectAttempt++;
              try {
                initialize();
              } catch (Exception e) {
                notifyError(e);
              }
            },
            options.getConnectTimeout(),
            TimeUnit.MILLISECONDS);
  }

  private void startListening() {
    CompletableFuture.runAsync(
        () -> {
          byte[] buffer = new byte[1024];
          DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

          while (!socket.isClosed()) {
            try {
              socket.receive(packet);
              byte[] data = Arrays.copyOf(packet.getData(), packet.getLength());
              handleResponse(data);
            } catch (Exception e) {
              if (!socket.isClosed()) {
                log.error("Error receiving response", e);
                notifyError(e);
              }
            }
          }
        });
  }

  private void socketSend(JSONObject message) throws Exception {
    if (socket == null || socket.isClosed()) {
      throw new HvacException("Client is not connected to the HVAC device");
    }

    log.debug("Sending message: {}", message);
    byte[] data = message.toString().getBytes(StandardCharsets.UTF_8);

    InetAddress address = InetAddress.getByName(options.getHost());
    DatagramPacket packet = new DatagramPacket(data, data.length, address, options.getPort());

    socket.send(packet);
  }

  private void sendRequest(JSONObject message) throws Exception {
    log.debug("Sending request: {}", message);

    EncryptionService.EncryptedMessage encrypted = encryptionService.encrypt(message);

    JSONObject packedMessage = new JSONObject();
    packedMessage.put("cid", "app");
    packedMessage.put("i", 0);
    packedMessage.put("t", "pack");
    packedMessage.put("uid", 0);
    packedMessage.put("pack", encrypted.getPayload());
    if (encrypted.getTag() != null) {
      packedMessage.put("tag", encrypted.getTag());
    }

    socketSend(packedMessage);
  }

  private void handleResponse(byte[] buffer) {
    try {
      String jsonString = new String(buffer, StandardCharsets.UTF_8);
      JSONObject message = new JSONObject(jsonString);

      log.debug("Handling response: {}", message);

      JSONObject pack = encryptionService.decrypt(message);

      String type = pack.optString("t");

      switch (type) {
        case "dev":
          handleHandshakeResponse(pack);
          break;
        case "bindok":
          handleBindingConfirmationResponse();
          break;
        case "dat":
          handleStatusResponse(pack);
          break;
        case "res":
          handleUpdateConfirmResponse(pack);
          break;
        default:
          log.warn("Unknown message type: {}", type);
          break;
      }

    } catch (Exception e) {
      log.error("Error handling response", e);
      notifyError(e);
    }
  }

  private void handleHandshakeResponse(JSONObject message) {
    deviceId = message.optString("cid");
    if (deviceId.isEmpty()) {
      deviceId = message.optString("mac");
    }

    log.info("Device handshake successful, device ID: {}", deviceId);

    try {
      sendBindRequest(1);
    } catch (Exception e) {
      notifyError(e);
    }
  }

  private void sendBindRequest(int attempt) throws Exception {
    log.info("Binding start (attempt {})", attempt);

    JSONObject bindMessage = new JSONObject();
    bindMessage.put("mac", deviceId);
    bindMessage.put("t", "bind");
    bindMessage.put("uid", 0);

    EncryptionService.EncryptedMessage encrypted = encryptionService.encrypt(bindMessage);

    JSONObject packedMessage = new JSONObject();
    packedMessage.put("cid", "app");
    packedMessage.put("i", 1);
    packedMessage.put("t", "pack");
    packedMessage.put("uid", 0);
    packedMessage.put("pack", encrypted.getPayload());
    if (encrypted.getTag() != null) {
      packedMessage.put("tag", encrypted.getTag());
    }

    socketSend(packedMessage);

    // Critical: Schedule a second bind attempt if first fails (working version behavior)
    if (attempt == 1) {
      scheduler.schedule(
          () -> {
            try {
              log.warn("Binding attempt timed out");
              sendBindRequest(2);
            } catch (Exception e) {
              notifyError(e);
            }
          },
          500,
          TimeUnit.MILLISECONDS);
    }
  }

  private void handleBindingConfirmationResponse() {
    log.info("Binding successful, connected to device");

    // Cancel any pending reconnection attempts
    if (reconnectTask != null) {
      reconnectTask.cancel(false);
      reconnectTask = null;
    }

    // Mark as connected
    connected = true;

    try {
      // Request initial status
      requestStatus();

      // Start polling if enabled
      if (options.isPoll()) {
        log.debug("Starting status polling every {}ms", options.getPollingInterval());
        pollingTask =
            scheduler.scheduleAtFixedRate(
                () -> {
                  try {
                    requestStatus();
                  } catch (Exception e) {
                    notifyError(e);
                  }
                },
                options.getPollingInterval(),
                options.getPollingInterval(),
                TimeUnit.MILLISECONDS);
      }

      // Notify listeners and complete the connection future
      notifyConnect();
      if (connectFuture != null && !connectFuture.isDone()) {
        connectFuture.complete(null);
        log.info("Connection established successfully");
      }

    } catch (Exception e) {
      log.error("Error during connection finalization", e);
      if (connectFuture != null && !connectFuture.isDone()) {
        connectFuture.completeExceptionally(e);
      }
      notifyError(e);
    }
  }

  private void requestStatus() throws Exception {
    log.debug("Requesting device status");

    List<String> propertyNames =
        Arrays.asList(
            "power",
            "mode",
            "temperatureUnit",
            "temperature",
            "currentTemperature",
            "fanSpeed",
            "air",
            "blow",
            "health",
            "sleep",
            "lights",
            "swingHor",
            "swingVert",
            "quiet",
            "turbo",
            "powerSave",
            "safetyHeating");

    JSONObject statusMessage = new JSONObject();
    statusMessage.put("cols", new JSONArray(transformer.arrayToVendor(propertyNames)));
    statusMessage.put("mac", deviceId);
    statusMessage.put("t", "status");

    sendRequest(statusMessage);

    // Set status timeout
    statusTimeoutTask =
        scheduler.schedule(
            () -> {
              log.warn("Status request timeout ({}ms)", options.getPollingTimeout());
              properties.clear();
              notifyNoResponse();
            },
            options.getPollingTimeout(),
            TimeUnit.MILLISECONDS);
  }

  private void handleStatusResponse(JSONObject pack) {
    log.debug("Received status response");

    if (statusTimeoutTask != null) {
      statusTimeoutTask.cancel(false);
    }

    Map<String, Object> oldProperties = new HashMap<>(properties);

    JSONArray cols = pack.getJSONArray("cols");
    JSONArray dat = pack.getJSONArray("dat");

    Map<String, Object> newProperties = new HashMap<>();
    for (int i = 0; i < cols.length() && i < dat.length(); i++) {
      String col = cols.getString(i);
      Object value = dat.get(i);
      newProperties.put(col, value);
      properties.put(col, value);
    }

    // Check for changes and notify listeners
    if (!newProperties.equals(oldProperties)) {
      DeviceStatus status = getStatus();
      statusUpdateListeners.forEach(listener -> listener.accept(status));
    }
  }

  private void handleUpdateConfirmResponse(JSONObject pack) {
    log.debug("Received update confirmation");

    JSONArray opt = pack.getJSONArray("opt");
    JSONArray values = pack.has("val") ? pack.getJSONArray("val") : pack.getJSONArray("p");

    Map<String, Object> updatedProperties = new HashMap<>();
    for (int i = 0; i < opt.length() && i < values.length(); i++) {
      String property = opt.getString(i);
      Object value = values.get(i);
      properties.put(property, value);
      updatedProperties.put(property, value);
    }

    log.info("Properties updated successfully: {}", updatedProperties.keySet());
  }

  private void dispose() {
    if (pollingTask != null) {
      pollingTask.cancel(false);
    }
    if (reconnectTask != null) {
      reconnectTask.cancel(false);
    }
    if (statusTimeoutTask != null) {
      statusTimeoutTask.cancel(false);
    }
  }

  // Event notification methods
  private void notifyConnect() {
    connectListeners.forEach(Runnable::run);
  }

  private void notifyError(Exception error) {
    errorListeners.forEach(listener -> listener.accept(error));
  }

  private void notifyDisconnect() {
    disconnectListeners.forEach(Runnable::run);
  }

  private void notifyNoResponse() {
    noResponseListeners.forEach(Runnable::run);
  }
}
