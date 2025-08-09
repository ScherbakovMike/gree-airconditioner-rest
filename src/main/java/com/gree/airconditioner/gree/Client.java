package com.gree.airconditioner.gree;

import com.gree.airconditioner.gree.exceptions.ClientException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Control GREE HVAC device by getting and setting its properties
 */
public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private String cid; // Device MAC-address
    private DatagramSocket socket;
    private final ClientOptions options;
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
    private final List<Consumer<Map<String, Object>>> updateListeners = new ArrayList<>();
    private final List<Consumer<Map<String, Object>>> successListeners = new ArrayList<>();
    private final List<Consumer<Exception>> errorListeners = new ArrayList<>();
    private final List<Runnable> disconnectListeners = new ArrayList<>();
    private final List<Runnable> noResponseListeners = new ArrayList<>();

    public Client(ClientOptions options) {
        this.options = options != null ? options : new ClientOptions();
        this.encryptionService = new EncryptionService();
        
        logger.info("Init with options: host={}, port={}, autoConnect={}",
                this.options.getHost(), this.options.getPort(), this.options.isAutoConnect());

        if (this.options.isAutoConnect()) {
            CompletableFuture.runAsync(() -> {
                try {
                    connect().get();
                } catch (Exception e) {
                    notifyError(e);
                }
            });
        }
    }

    public Client(String host) {
        this(new ClientOptions(host));
    }

    public Client() {
        this(new ClientOptions());
    }

    /**
     * Connect to HVAC device and start polling status changes by default
     */
    public CompletableFuture<Void> connect() {
        logger.info("Connecting to {}:{}", options.getHost(), options.getPort());
        
        if (connectFuture != null && !connectFuture.isDone()) {
            return connectFuture;
        }

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

    /**
     * Disconnect from HVAC device and stop status polling
     */
    public CompletableFuture<Void> disconnect() {
        logger.info("Disconnecting");
        
        return CompletableFuture.runAsync(() -> {
            dispose();
            if (socket != null && !socket.isClosed()) {
                socket.close();
                socket = null;
            }
            connected = false;
            notifyDisconnect();
            logger.info("Disconnected");
        });
    }

    /**
     * Set device property
     */
    public CompletableFuture<Void> setProperty(Property property, Object value) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(property.getValue(), value);
        return setProperties(properties);
    }

    /**
     * Set device property
     */
    public CompletableFuture<Void> setProperty(String property, Object value) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(property, value);
        return setProperties(properties);
    }

    /**
     * Set a list of device properties at once by one request
     */
    public CompletableFuture<Void> setProperties(Map<String, Object> properties) {
        return CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> vendorProperties = transformer.toVendor(properties);
                
                JSONObject request = new JSONObject();
                request.put("opt", new JSONArray(vendorProperties.keySet()));
                request.put("p", new JSONArray(vendorProperties.values()));
                request.put("t", "cmd");

                logger.info("Update request for properties: {}", properties.keySet());
                sendRequest(request);
                
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Get device MAC-address
     */
    public String getDeviceId() {
        return cid;
    }

    /**
     * Get current device properties
     */
    public Map<String, Object> getProperties() {
        return new HashMap<>(transformer.fromVendor(properties));
    }

    // Event listener methods
    public void onConnect(Runnable listener) {
        connectListeners.add(listener);
    }

    public void onUpdate(Consumer<Map<String, Object>> listener) {
        updateListeners.add(listener);
    }

    public void onSuccess(Consumer<Map<String, Object>> listener) {
        successListeners.add(listener);
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

    // Private implementation methods
    private void initialize() {
        dispose();
        
        try {
            encryptionService = new EncryptionService();
            logger.info("Scan start (attempt {})", reconnectAttempt);
            
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
        reconnectTask = scheduler.schedule(() -> {
            logger.warn("Connect timeout, reconnect (timeout: {}ms)", options.getConnectTimeout());
            reconnectAttempt++;
            try {
                initialize();
            } catch (Exception e) {
                notifyError(e);
            }
        }, options.getConnectTimeout(), TimeUnit.MILLISECONDS);
    }

    private void startListening() {
        CompletableFuture.runAsync(() -> {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            
            while (!socket.isClosed()) {
                try {
                    socket.receive(packet);
                    byte[] data = Arrays.copyOf(packet.getData(), packet.getLength());
                    handleResponse(data);
                } catch (Exception e) {
                    if (!socket.isClosed()) {
                        logger.error("Error receiving response", e);
                        notifyError(e);
                    }
                }
            }
        });
    }

    private void socketSend(JSONObject message) throws Exception {
        if (socket == null || socket.isClosed()) {
            throw new ClientException("Client is not connected to the HVAC");
        }

        logger.debug("Socket send: {}", message);
        byte[] data = message.toString().getBytes(StandardCharsets.UTF_8);
        
        InetAddress address = InetAddress.getByName(options.getHost());
        DatagramPacket packet = new DatagramPacket(data, data.length, address, options.getPort());
        
        socket.send(packet);
    }

    private void sendRequest(JSONObject message) throws Exception {
        logger.debug("Send request: {}", message);
        
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

    private void sendBindRequest(int attempt) throws Exception {
        logger.info("Binding start (attempt {})", attempt);
        
        JSONObject bindMessage = new JSONObject();
        bindMessage.put("mac", cid);
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
    }

    private void requestStatus() throws Exception {
        logger.info("Status request");
        
        List<String> propertyNames = Arrays.asList(
                "power", "mode", "temperatureUnit", "temperature", "currentTemperature",
                "fanSpeed", "air", "blow", "health", "sleep", "lights", 
                "swingHor", "swingVert", "quiet", "turbo", "powerSave", "safetyHeating"
        );
        
        JSONObject statusMessage = new JSONObject();
        statusMessage.put("cols", new JSONArray(transformer.arrayToVendor(propertyNames)));
        statusMessage.put("mac", cid);
        statusMessage.put("t", "status");
        
        sendRequest(statusMessage);
        
        // Set status timeout
        statusTimeoutTask = scheduler.schedule(() -> {
            logger.warn("Status request timeout ({}ms)", options.getPollingTimeout());
            properties.clear();
            notifyNoResponse();
        }, options.getPollingTimeout(), TimeUnit.MILLISECONDS);
    }

    private void handleResponse(byte[] buffer) {
        try {
            String jsonString = new String(buffer, StandardCharsets.UTF_8);
            JSONObject message = new JSONObject(jsonString);
            
            logger.debug("Handle response: {}", message);
            
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
                    logger.warn("Unknown message type: {}", type);
                    break;
            }
            
        } catch (Exception e) {
            logger.error("Response handle error", e);
            notifyError(e);
        }
    }

    private void handleHandshakeResponse(JSONObject message) {
        cid = message.optString("cid");
        if (cid.isEmpty()) {
            cid = message.optString("mac");
        }
        
        logger.info("Scan success, device ID: {}", cid);
        
        try {
            sendBindRequest(1);
            scheduler.schedule(() -> {
                try {
                    logger.warn("Binding attempt timed out");
                    sendBindRequest(2);
                } catch (Exception e) {
                    notifyError(e);
                }
            }, 500, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            notifyError(e);
        }
    }

    private void handleBindingConfirmationResponse() {
        logger.info("Binding success (connected) to host: {}", options.getHost());
        
        if (reconnectTask != null) {
            reconnectTask.cancel(false);
        }
        
        connected = true;
        
        try {
            requestStatus();
            
            if (options.isPoll()) {
                logger.info("Schedule status polling every {}ms", options.getPollingInterval());
                pollingTask = scheduler.scheduleAtFixedRate(() -> {
                    try {
                        requestStatus();
                    } catch (Exception e) {
                        notifyError(e);
                    }
                }, options.getPollingInterval(), options.getPollingInterval(), TimeUnit.MILLISECONDS);
            }
            
            notifyConnect();
            if (connectFuture != null) {
                connectFuture.complete(null);
            }
            
        } catch (Exception e) {
            notifyError(e);
        }
    }

    private void handleStatusResponse(JSONObject pack) {
        logger.info("Status response");
        
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
        
        // Check for changes
        Map<String, Object> changes = getChanges(oldProperties, newProperties);
        if (!changes.isEmpty()) {
            notifyUpdate(transformer.fromVendor(changes));
        }
    }

    private void handleUpdateConfirmResponse(JSONObject pack) {
        logger.info("Update response");
        
        JSONArray opt = pack.getJSONArray("opt");
        JSONArray values = pack.has("val") ? pack.getJSONArray("val") : pack.getJSONArray("p");
        
        Map<String, Object> updatedProperties = new HashMap<>();
        for (int i = 0; i < opt.length() && i < values.length(); i++) {
            String property = opt.getString(i);
            Object value = values.get(i);
            properties.put(property, value);
            updatedProperties.put(property, value);
        }
        
        notifySuccess(transformer.fromVendor(updatedProperties));
    }

    private Map<String, Object> getChanges(Map<String, Object> oldProps, Map<String, Object> newProps) {
        Map<String, Object> changes = new HashMap<>();
        for (Map.Entry<String, Object> entry : newProps.entrySet()) {
            String key = entry.getKey();
            Object newValue = entry.getValue();
            Object oldValue = oldProps.get(key);
            
            if (!Objects.equals(oldValue, newValue)) {
                changes.put(key, newValue);
            }
        }
        return changes;
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

    private void notifyUpdate(Map<String, Object> updatedProperties) {
        updateListeners.forEach(listener -> listener.accept(updatedProperties));
    }

    private void notifySuccess(Map<String, Object> updatedProperties) {
        successListeners.forEach(listener -> listener.accept(updatedProperties));
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

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
}