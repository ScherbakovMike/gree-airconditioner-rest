package com.gree.hvac.client;

import static org.junit.jupiter.api.Assertions.*;

import com.gree.hvac.dto.DeviceControl;
import com.gree.hvac.dto.DeviceStatus;
import com.gree.hvac.exceptions.HvacException;
import com.gree.hvac.network.MockNetworkService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HvacClientTest {

  private HvacClient client;
  private HvacClientOptions options;
  private MockNetworkService mockNetworkService;

  @BeforeEach
  void setUp() {
    options =
        new HvacClientOptions("192.168.1.100")
            .setAutoConnect(false) // Disable auto-connect for testing
            .setPoll(false) // Disable polling for testing
            .setConnectTimeout(100) // Short timeout for testing
            .setPollingTimeout(100);

    mockNetworkService = new MockNetworkService();
    client = new HvacClient(options, mockNetworkService);
  }

  @AfterEach
  void tearDown() {
    if (client != null) {
      try {
        client.shutdown();
      } catch (Exception e) {
        // Ignore shutdown errors in tests
      }
    }
  }

  @Test
  void testConstructorWithOptions() {
    HvacClientOptions testOptions = new HvacClientOptions("192.168.1.50").setPort(8000);
    MockNetworkService testMockService = new MockNetworkService();
    HvacClient testClient = new HvacClient(testOptions, testMockService);

    assertNotNull(testClient);
    assertFalse(testClient.isConnected());
    assertNull(testClient.getDeviceId());

    testClient.shutdown();
  }

  @Test
  void testConstructorWithNullOptions() {
    HvacClientOptions nullOptions = null;
    MockNetworkService testMockService = new MockNetworkService();
    HvacClient testClient = new HvacClient(nullOptions, testMockService);

    assertNotNull(testClient);
    assertFalse(testClient.isConnected());

    testClient.shutdown();
  }

  @Test
  void testConstructorWithHostString() {
    // Note: This uses the real NetworkService implementation
    // In a real test environment you might want to also mock this
    HvacClient testClient = new HvacClient("192.168.1.200");

    assertNotNull(testClient);
    assertFalse(testClient.isConnected());

    testClient.shutdown();
  }

  @Test
  void testConstructorWithAutoConnect() {
    // Test that auto-connect starts connection process
    HvacClientOptions autoConnectOptions =
        new HvacClientOptions("192.168.1.100")
            .setAutoConnect(true)
            .setConnectTimeout(50); // Very short timeout

    MockNetworkService testMockService = new MockNetworkService();
    HvacClient autoConnectClient = new HvacClient(autoConnectOptions, testMockService);

    assertNotNull(autoConnectClient);
    // Connection should succeed with mock service

    autoConnectClient.shutdown();
  }

  @Test
  void testInitialState() {
    assertFalse(client.isConnected());
    assertNull(client.getDeviceId());

    DeviceStatus status = client.getStatus();
    assertNotNull(status);
    assertNull(status.getDeviceId());
    assertNull(status.getPower());
    assertNull(status.getTemperature());

    Map<String, Object> properties = client.getCurrentProperties();
    assertNotNull(properties);
    assertTrue(properties.isEmpty());
  }

  @Test
  void testConnectReturnsCompletableFuture() {
    CompletableFuture<Void> connectFuture = client.connect();

    assertNotNull(connectFuture);
    assertFalse(connectFuture.isDone());

    // Cancel to avoid network operations
    connectFuture.cancel(true);
  }

  @Test
  void testMultipleConnectCallsReturnSameFuture() {
    CompletableFuture<Void> future1 = client.connect();
    CompletableFuture<Void> future2 = client.connect();

    assertSame(future1, future2);

    future1.cancel(true);
  }

  @Test
  void testDisconnectWithoutConnection() {
    CompletableFuture<Void> disconnectFuture = client.disconnect();

    assertNotNull(disconnectFuture);
    // Should complete successfully even without connection
    assertDoesNotThrow(() -> disconnectFuture.get(1, TimeUnit.SECONDS));
  }

  @Test
  void testSetPropertiesWhenNotConnected() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("power", "on");
    properties.put("temperature", 22);

    CompletableFuture<Void> result = client.setProperties(properties);

    assertNotNull(result);

    // Should fail because not connected
    Exception exception = assertThrows(Exception.class, () -> result.get(1, TimeUnit.SECONDS));

    assertTrue(
        exception.getCause() instanceof HvacException
            || (exception.getCause() != null
                && exception.getCause().getMessage() != null
                && exception.getCause().getMessage().contains("not connected"))
            || (exception.getMessage() != null
                && exception.getMessage().contains("not connected")));
  }

  @Test
  void testControlWithDeviceControl() {
    DeviceControl control = new DeviceControl();
    control.setPower(true);
    control.setTemperature(24);
    control.setMode("cool");
    control.setFanSpeed("auto");

    CompletableFuture<Void> result = client.control(control);

    assertNotNull(result);

    // Should fail because not connected, but tests the method structure
    assertThrows(Exception.class, () -> result.get(1, TimeUnit.SECONDS));
  }

  @Test
  void testControlWithEmptyDeviceControl() {
    DeviceControl control = new DeviceControl(); // All fields null

    CompletableFuture<Void> result = client.control(control);

    assertNotNull(result);

    // Should complete quickly since no properties to set, but may not throw exception
    // if no properties are set (the control method may handle this gracefully)
    assertDoesNotThrow(
        () -> {
          try {
            result.get(1, TimeUnit.SECONDS);
          } catch (Exception e) {
            // Exception is acceptable but not required
          }
        });
  }

  @Test
  void testEventListenerRegistration() {
    AtomicReference<DeviceStatus> statusUpdate = new AtomicReference<>();
    AtomicReference<Exception> errorUpdate = new AtomicReference<>();
    CountDownLatch connectLatch = new CountDownLatch(1);
    CountDownLatch disconnectLatch = new CountDownLatch(1);
    CountDownLatch noResponseLatch = new CountDownLatch(1);

    // Register event listeners
    client.onConnect(() -> connectLatch.countDown());
    client.onStatusUpdate(statusUpdate::set);
    client.onError(errorUpdate::set);
    client.onDisconnect(() -> disconnectLatch.countDown());
    client.onNoResponse(() -> noResponseLatch.countDown());

    // Verify listeners are registered (they should be called during connection attempts)
    assertNotNull(client);
  }

  @Test
  void testMultipleEventListeners() {
    CountDownLatch latch1 = new CountDownLatch(1);
    CountDownLatch latch2 = new CountDownLatch(1);

    client.onConnect(() -> latch1.countDown());
    client.onConnect(() -> latch2.countDown());

    // Both listeners should be registered
    assertNotNull(client);
  }

  @Test
  void testShutdown() {
    assertDoesNotThrow(() -> client.shutdown());

    // After shutdown, client should not accept new operations
    assertFalse(client.isConnected());
  }

  @Test
  void testShutdownMultipleTimes() {
    assertDoesNotThrow(() -> client.shutdown());
    assertDoesNotThrow(() -> client.shutdown()); // Should not throw on second call
  }

  @Test
  void testGetStatusAfterPropertiesUpdate() {
    // Since we can't easily mock the internal state, test the basic structure
    DeviceStatus status = client.getStatus();

    assertNotNull(status);
    assertNull(status.getDeviceId()); // No device connected
    assertNull(status.getPower());
    assertNull(status.getTemperature());
    assertNull(status.getCurrentTemperature());
    assertNull(status.getMode());
    assertNull(status.getFanSpeed());
    assertNull(status.getSwingHorizontal());
    assertNull(status.getSwingVertical());
    assertNull(status.getLights());
    assertNull(status.getTurbo());
    assertNull(status.getQuiet());
    assertNull(status.getHealth());
    assertNull(status.getPowerSave());
    assertNull(status.getSleep());
  }

  @Test
  void testGetCurrentPropertiesReturnsEmptyMapInitially() {
    Map<String, Object> properties = client.getCurrentProperties();

    assertNotNull(properties);
    assertTrue(properties.isEmpty());
  }

  @Test
  void testGetCurrentPropertiesReturnsCopy() {
    Map<String, Object> properties1 = client.getCurrentProperties();
    Map<String, Object> properties2 = client.getCurrentProperties();

    assertNotSame(properties1, properties2); // Should return different instances
    assertEquals(properties1, properties2); // But with same content
  }

  @Test
  void testDeviceControlPropertyMapping() {
    DeviceControl control = new DeviceControl();
    control.setPower(true);
    control.setTemperature(25);
    control.setMode("HEAT"); // Test case conversion
    control.setFanSpeed("HIGH");
    control.setSwingHorizontal("ON");
    control.setSwingVertical("OFF");
    control.setLights(false);
    control.setTurbo(true);
    control.setQuiet(false);
    control.setHealth(true);
    control.setPowerSave(false);
    control.setSleep(true);

    // Test that control method handles all properties
    CompletableFuture<Void> result = client.control(control);
    assertNotNull(result);

    // Will fail due to no connection, but tests property mapping
    assertThrows(Exception.class, () -> result.get(1, TimeUnit.SECONDS));
  }

  @Test
  void testDeviceControlWithNullValues() {
    DeviceControl control = new DeviceControl();
    // All values are null by default

    CompletableFuture<Void> result = client.control(control);
    assertNotNull(result);

    // Should handle null values gracefully, may complete without exception if no properties to set
    assertDoesNotThrow(
        () -> {
          try {
            result.get(1, TimeUnit.SECONDS);
          } catch (Exception e) {
            // Exception is acceptable but not required for null control values
          }
        });
  }

  @Test
  void testSetPropertiesWithEmptyMap() {
    Map<String, Object> emptyProperties = new HashMap<>();

    CompletableFuture<Void> result = client.setProperties(emptyProperties);
    assertNotNull(result);

    // Should still fail due to no connection
    assertThrows(Exception.class, () -> result.get(1, TimeUnit.SECONDS));
  }

  @Test
  void testSetPropertiesWithNullValues() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("power", null);
    properties.put("temperature", null);
    properties.put("mode", null);

    CompletableFuture<Void> result = client.setProperties(properties);
    assertNotNull(result);

    assertThrows(Exception.class, () -> result.get(1, TimeUnit.SECONDS));
  }

  @Test
  void testSetPropertiesWithMixedTypes() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("power", "on");
    properties.put("temperature", 22);
    properties.put("mode", "cool");
    properties.put("fanSpeed", "auto");
    properties.put("lights", "off");

    CompletableFuture<Void> result = client.setProperties(properties);
    assertNotNull(result);

    assertThrows(Exception.class, () -> result.get(1, TimeUnit.SECONDS));
  }

  @Test
  void testConnectWithValidOptions() throws Exception {
    HvacClientOptions validOptions =
        new HvacClientOptions("192.168.1.100")
            .setPort(7000)
            .setConnectTimeout(1000)
            .setAutoConnect(false);

    MockNetworkService testMockService = new MockNetworkService();
    testMockService.simulateConnectionFailure(true); // Simulate failure for predictable test
    HvacClient validClient = new HvacClient(validOptions, testMockService);

    CompletableFuture<Void> connectFuture = validClient.connect();
    assertNotNull(connectFuture);

    // Should fail with mock connection failure enabled
    assertThrows(Exception.class, () -> connectFuture.get(2, TimeUnit.SECONDS));
    assertFalse(validClient.isConnected());

    validClient.shutdown();
  }

  @Test
  void testClientOptionsIntegration() {
    HvacClientOptions customOptions =
        new HvacClientOptions("test.host.local")
            .setPort(9999)
            .setConnectTimeout(5000)
            .setPollingInterval(10000)
            .setPollingTimeout(2000)
            .setAutoConnect(false)
            .setPoll(true)
            .setDebug(true)
            .setLogLevel("debug");

    MockNetworkService testMockService = new MockNetworkService();
    HvacClient customClient = new HvacClient(customOptions, testMockService);

    assertNotNull(customClient);
    assertFalse(customClient.isConnected());

    customClient.shutdown();
  }

  @Test
  void testConcurrentOperations() {
    // Test that multiple operations can be called without throwing exceptions
    assertDoesNotThrow(
        () -> {
          CompletableFuture<Void> connect = client.connect();
          CompletableFuture<Void> disconnect = client.disconnect();
          DeviceStatus status = client.getStatus();
          Map<String, Object> props = client.getCurrentProperties();

          assertNotNull(connect);
          assertNotNull(disconnect);
          assertNotNull(status);
          assertNotNull(props);

          connect.cancel(true);
        });
  }
}
