package com.gree.hvac.client;

import com.gree.hvac.dto.DeviceControl;
import com.gree.hvac.dto.DeviceStatus;
import com.gree.hvac.exceptions.HvacException;
import com.gree.hvac.network.NetworkService;
import com.gree.hvac.network.NetworkServiceImpl;
import com.gree.hvac.network.NetworkSocket;
import com.gree.hvac.protocol.EncryptionService;
import com.gree.hvac.protocol.PropertyTransformer;
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

  // Property name constants
  private static final String PROPERTY_POWER = "power";
  private static final String PROPERTY_TEMPERATURE = "temperature";
  private static final String PROPERTY_MODE = "mode";
  private static final String PROPERTY_FAN_SPEED = "fanSpeed";
  private static final String PROPERTY_SWING_HOR = "swingHor";
  private static final String PROPERTY_SWING_VERT = "swingVert";
  private static final String PROPERTY_LIGHTS = "lights";
  private static final String PROPERTY_TURBO = "turbo";
  private static final String PROPERTY_QUIET = "quiet";
  private static final String PROPERTY_HEALTH = "health";
  private static final String PROPERTY_POWER_SAVE = "powerSave";
  private static final String PROPERTY_SLEEP = "sleep";
  private static final String PROPERTY_CURRENT_TEMPERATURE = "currentTemperature";

  // Value constants
  private static final String VALUE_ON = "on";
  private static final String VALUE_OFF = "off";

  // Additional property constants for status request
  private static final String PROPERTY_TEMPERATURE_UNIT = "temperatureUnit";
  private static final String PROPERTY_AIR = "air";
  private static final String PROPERTY_BLOW = "blow";
  private static final String PROPERTY_SAFETY_HEATING = "safetyHeating";

  private String deviceId; // Device MAC-address
  private NetworkSocket socket;
  private final HvacClientOptions options;
  private final NetworkService networkService;
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
    this(options, new NetworkServiceImpl());
  }

  /** Create HVAC client with configuration options and network service (for testing) */
  public HvacClient(HvacClientOptions options, NetworkService networkService) {
    this(options, networkService, new EncryptionService());
  }

  /** Create HVAC client with all dependencies for testing */
  public HvacClient(
      HvacClientOptions options,
      NetworkService networkService,
      EncryptionService encryptionService) {
    this.options = options != null ? options : new HvacClientOptions();
    this.networkService = networkService;
    this.encryptionService = encryptionService;

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
    this(new HvacClientOptions(host), new NetworkServiceImpl());
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
      socket = networkService.createSocket(0);

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
          if (socket != null && !networkService.isClosed(socket)) {
            networkService.closeSocket(socket);
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
            Map<String, Object> properties = buildControlProperties(control);

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

  /** Build properties map from DeviceControl object */
  private Map<String, Object> buildControlProperties(DeviceControl control) {
    Map<String, Object> properties = new HashMap<>();

    addBooleanProperty(properties, PROPERTY_POWER, control.getPower());
    addValueProperty(properties, PROPERTY_TEMPERATURE, control.getTemperature());
    addLowerCaseStringProperty(properties, PROPERTY_MODE, control.getMode());
    addLowerCaseStringProperty(properties, PROPERTY_FAN_SPEED, control.getFanSpeed());
    addLowerCaseStringProperty(properties, PROPERTY_SWING_HOR, control.getSwingHorizontal());
    addLowerCaseStringProperty(properties, PROPERTY_SWING_VERT, control.getSwingVertical());
    addBooleanProperty(properties, PROPERTY_LIGHTS, control.getLights());
    addBooleanProperty(properties, PROPERTY_TURBO, control.getTurbo());
    addBooleanProperty(properties, PROPERTY_QUIET, control.getQuiet());
    addBooleanProperty(properties, PROPERTY_HEALTH, control.getHealth());
    addBooleanProperty(properties, PROPERTY_POWER_SAVE, control.getPowerSave());
    addBooleanProperty(properties, PROPERTY_SLEEP, control.getSleep());

    return properties;
  }

  /** Add boolean property as on/off value */
  private void addBooleanProperty(Map<String, Object> properties, String key, Boolean value) {
    if (value != null) {
      properties.put(key, value ? VALUE_ON : VALUE_OFF);
    }
  }

  /** Add string property with lowercase conversion */
  private void addLowerCaseStringProperty(
      Map<String, Object> properties, String key, String value) {
    if (value != null) {
      properties.put(key, value.toLowerCase());
    }
  }

  /** Add property value as-is */
  private void addValueProperty(Map<String, Object> properties, String key, Object value) {
    if (value != null) {
      properties.put(key, value);
    }
  }

  /** Get current device properties as DeviceStatus */
  public DeviceStatus getStatus() {
    Map<String, Object> currentProperties = getCurrentProperties();

    DeviceStatus status = new DeviceStatus();
    status.setDeviceId(deviceId);

    // Handle boolean properties - only set if the property exists
    Object powerValue = currentProperties.get(PROPERTY_POWER);
    status.setPower(powerValue != null ? VALUE_ON.equals(powerValue) : null);

    status.setTemperature((Integer) currentProperties.get(PROPERTY_TEMPERATURE));
    status.setCurrentTemperature((Integer) currentProperties.get(PROPERTY_CURRENT_TEMPERATURE));
    status.setMode((String) currentProperties.get(PROPERTY_MODE));
    status.setFanSpeed((String) currentProperties.get(PROPERTY_FAN_SPEED));
    status.setSwingHorizontal((String) currentProperties.get(PROPERTY_SWING_HOR));
    status.setSwingVertical((String) currentProperties.get(PROPERTY_SWING_VERT));

    // Handle other boolean properties
    Object lightsValue = currentProperties.get(PROPERTY_LIGHTS);
    status.setLights(lightsValue != null ? VALUE_ON.equals(lightsValue) : null);

    Object turboValue = currentProperties.get(PROPERTY_TURBO);
    status.setTurbo(turboValue != null ? VALUE_ON.equals(turboValue) : null);

    Object quietValue = currentProperties.get(PROPERTY_QUIET);
    status.setQuiet(quietValue != null ? VALUE_ON.equals(quietValue) : null);

    Object healthValue = currentProperties.get(PROPERTY_HEALTH);
    status.setHealth(healthValue != null ? VALUE_ON.equals(healthValue) : null);

    Object powerSaveValue = currentProperties.get(PROPERTY_POWER_SAVE);
    status.setPowerSave(powerSaveValue != null ? VALUE_ON.equals(powerSaveValue) : null);

    Object sleepValue = currentProperties.get(PROPERTY_SLEEP);
    status.setSleep(sleepValue != null ? VALUE_ON.equals(sleepValue) : null);

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
    networkService.startListening(socket, this::handleResponse);
  }

  private void socketSend(JSONObject message) throws Exception {
    if (socket == null || networkService.isClosed(socket)) {
      throw new HvacException("Client is not connected to the HVAC device");
    }

    log.debug("Sending message: {}", message);
    byte[] data = message.toString().getBytes(StandardCharsets.UTF_8);

    InetAddress address = networkService.resolveAddress(options.getHost());
    networkService.sendData(socket, data, address, options.getPort());
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
            PROPERTY_POWER,
            PROPERTY_MODE,
            PROPERTY_TEMPERATURE_UNIT,
            PROPERTY_TEMPERATURE,
            PROPERTY_CURRENT_TEMPERATURE,
            PROPERTY_FAN_SPEED,
            PROPERTY_AIR,
            PROPERTY_BLOW,
            PROPERTY_HEALTH,
            PROPERTY_SLEEP,
            PROPERTY_LIGHTS,
            PROPERTY_SWING_HOR,
            PROPERTY_SWING_VERT,
            PROPERTY_QUIET,
            PROPERTY_TURBO,
            PROPERTY_POWER_SAVE,
            PROPERTY_SAFETY_HEATING);

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
