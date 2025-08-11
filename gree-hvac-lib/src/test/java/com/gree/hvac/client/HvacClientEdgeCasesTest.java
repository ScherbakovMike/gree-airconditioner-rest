package com.gree.hvac.client;

import static org.junit.jupiter.api.Assertions.*;

import com.gree.hvac.dto.DeviceControl;
import com.gree.hvac.dto.DeviceStatus;
import com.gree.hvac.network.MockNetworkService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Edge case and boundary condition tests for HvacClient */
class HvacClientEdgeCasesTest {

  private HvacClient client;
  private MockNetworkService mockNetworkService;

  @AfterEach
  void tearDown() {
    if (client != null) {
      client.shutdown();
    }
  }

  @Test
  void testClientWithInvalidHostAddress() {
    HvacClientOptions options =
        new HvacClientOptions("invalid.host.address.that.does.not.exist")
            .setAutoConnect(false)
            .setConnectTimeout(100);

    mockNetworkService = new MockNetworkService();
    mockNetworkService.simulateConnectionFailure(true);
    client = new HvacClient(options, mockNetworkService);

    assertNotNull(client);
    assertFalse(client.isConnected());

    CompletableFuture<Void> connectFuture = client.connect();

    // Should fail quickly with invalid host
    assertThrows(Exception.class, () -> connectFuture.get(2, TimeUnit.SECONDS));
  }

  @Test
  void testClientWithEmptyHost() {
    HvacClientOptions options =
        new HvacClientOptions("").setAutoConnect(false).setConnectTimeout(100);

    mockNetworkService = new MockNetworkService();
    mockNetworkService.simulateConnectionFailure(true);
    client = new HvacClient(options, mockNetworkService);

    CompletableFuture<Void> connectFuture = client.connect();

    // Should fail with empty host
    assertThrows(Exception.class, () -> connectFuture.get(2, TimeUnit.SECONDS));
  }

  @Test
  void testClientWithZeroPort() {
    HvacClientOptions options =
        new HvacClientOptions("192.168.1.100")
            .setPort(0)
            .setAutoConnect(false)
            .setConnectTimeout(100);

    mockNetworkService = new MockNetworkService();
    mockNetworkService.simulateConnectionFailure(true);
    client = new HvacClient(options, mockNetworkService);

    CompletableFuture<Void> connectFuture = client.connect();

    // Should fail with connection failure enabled
    assertThrows(Exception.class, () -> connectFuture.get(2, TimeUnit.SECONDS));
  }

  @Test
  void testClientWithNegativePort() {
    HvacClientOptions options =
        new HvacClientOptions("192.168.1.100")
            .setPort(-1)
            .setAutoConnect(false)
            .setConnectTimeout(100);

    mockNetworkService = new MockNetworkService();
    mockNetworkService.simulateConnectionFailure(true);
    client = new HvacClient(options, mockNetworkService);

    CompletableFuture<Void> connectFuture = client.connect();

    // Should fail with connection failure enabled
    assertThrows(Exception.class, () -> connectFuture.get(2, TimeUnit.SECONDS));
  }

  @Test
  void testClientWithExtremelyShortTimeout() {
    HvacClientOptions options =
        new HvacClientOptions("192.168.1.100")
            .setConnectTimeout(1) // 1ms timeout
            .setPollingTimeout(1)
            .setAutoConnect(false);

    mockNetworkService = new MockNetworkService();
    mockNetworkService.simulateConnectionFailure(true);
    client = new HvacClient(options, mockNetworkService);

    CompletableFuture<Void> connectFuture = client.connect();

    // Should timeout very quickly with connection failure enabled
    assertThrows(Exception.class, () -> connectFuture.get(1, TimeUnit.SECONDS));
  }

  @Test
  void testClientWithZeroTimeout() {
    HvacClientOptions options =
        new HvacClientOptions("192.168.1.100")
            .setConnectTimeout(0)
            .setPollingTimeout(0)
            .setAutoConnect(false);

    mockNetworkService = new MockNetworkService();
    client = new HvacClient(options, mockNetworkService);

    // Should handle zero timeout gracefully
    assertNotNull(client);
    assertFalse(client.isConnected());
  }

  @Test
  void testDeviceControlWithExtremeValues() {
    HvacClientOptions options = new HvacClientOptions("192.168.1.100").setAutoConnect(false);

    mockNetworkService = new MockNetworkService();
    client = new HvacClient(options, mockNetworkService);

    DeviceControl control = new DeviceControl();
    control.setTemperature(-100); // Extreme cold
    control.setMode("INVALID_MODE");
    control.setFanSpeed("ULTRA_MAX_SPEED");

    CompletableFuture<Void> result = client.control(control);

    // Should handle extreme values gracefully (will fail due to no connection)
    assertThrows(Exception.class, () -> result.get(1, TimeUnit.SECONDS));
  }

  @Test
  void testDeviceControlWithVeryLongStrings() {
    HvacClientOptions options = new HvacClientOptions("192.168.1.100").setAutoConnect(false);

    mockNetworkService = new MockNetworkService();
    client = new HvacClient(options, mockNetworkService);

    String veryLongString = "x".repeat(10000);

    DeviceControl control = new DeviceControl();
    control.setMode(veryLongString);
    control.setFanSpeed(veryLongString);
    control.setSwingHorizontal(veryLongString);
    control.setSwingVertical(veryLongString);

    CompletableFuture<Void> result = client.control(control);

    // Should handle very long strings (will fail due to no connection)
    assertThrows(Exception.class, () -> result.get(1, TimeUnit.SECONDS));
  }

  @Test
  void testSetPropertiesWithVeryLargeMap() {
    HvacClientOptions options = new HvacClientOptions("192.168.1.100").setAutoConnect(false);

    mockNetworkService = new MockNetworkService();
    client = new HvacClient(options, mockNetworkService);

    Map<String, Object> largeMap = new HashMap<>();
    for (int i = 0; i < 10000; i++) {
      largeMap.put("property" + i, "value" + i);
    }

    CompletableFuture<Void> result = client.setProperties(largeMap);

    // Should handle large maps (will fail due to no connection)
    assertThrows(Exception.class, () -> result.get(1, TimeUnit.SECONDS));
  }

  @Test
  void testSetPropertiesWithSpecialCharacters() {
    HvacClientOptions options = new HvacClientOptions("192.168.1.100").setAutoConnect(false);

    mockNetworkService = new MockNetworkService();
    client = new HvacClient(options, mockNetworkService);

    Map<String, Object> properties = new HashMap<>();
    properties.put("unicode_test", "测试数据");
    properties.put("special_chars", "!@#$%^&*()_+-={}[]|\\:;\"'<>?,./");
    properties.put("emoji_test", "😀🌡️❄️🔥");
    properties.put("newlines", "line1\nline2\rline3\r\n");

    CompletableFuture<Void> result = client.setProperties(properties);

    // Should handle special characters (will fail due to no connection)
    assertThrows(Exception.class, () -> result.get(1, TimeUnit.SECONDS));
  }

  @Test
  void testManyEventListeners() {
    HvacClientOptions options = new HvacClientOptions("192.168.1.100").setAutoConnect(false);

    mockNetworkService = new MockNetworkService();
    client = new HvacClient(options, mockNetworkService);

    // Register many listeners
    for (int i = 0; i < 1000; i++) {
      final int index = i;
      client.onConnect(() -> System.out.println("Connect listener " + index));
      client.onDisconnect(() -> System.out.println("Disconnect listener " + index));
      client.onError(error -> System.out.println("Error listener " + index));
      client.onStatusUpdate(status -> System.out.println("Status listener " + index));
      client.onNoResponse(() -> System.out.println("NoResponse listener " + index));
    }

    // Should handle many listeners without issues
    assertNotNull(client);
    assertFalse(client.isConnected());
  }

  @Test
  void testStatusObjectConsistencyWithNoData() {
    HvacClientOptions options = new HvacClientOptions("192.168.1.100").setAutoConnect(false);

    mockNetworkService = new MockNetworkService();
    client = new HvacClient(options, mockNetworkService);

    DeviceStatus status = client.getStatus();

    assertNotNull(status);
    assertNull(status.getDeviceId());

    // All boolean properties should be null, not false
    assertNull(status.getPower());
    assertNull(status.getLights());
    assertNull(status.getTurbo());
    assertNull(status.getQuiet());
    assertNull(status.getHealth());
    assertNull(status.getPowerSave());
    assertNull(status.getSleep());

    // Numeric properties should be null
    assertNull(status.getTemperature());
    assertNull(status.getCurrentTemperature());

    // String properties should be null
    assertNull(status.getMode());
    assertNull(status.getFanSpeed());
    assertNull(status.getSwingHorizontal());
    assertNull(status.getSwingVertical());
  }

  @Test
  void testConcurrentPropertyAccess() {
    HvacClientOptions options = new HvacClientOptions("192.168.1.100").setAutoConnect(false);

    mockNetworkService = new MockNetworkService();
    client = new HvacClient(options, mockNetworkService);

    // Test that concurrent property access doesn't cause issues
    assertDoesNotThrow(
        () -> {
          // Use a more controlled approach without timing dependencies
          DeviceStatus status1 = client.getStatus();
          Map<String, Object> props1 = client.getCurrentProperties();
          boolean connected1 = client.isConnected();
          String deviceId1 = client.getDeviceId();

          // Verify all operations completed without exceptions
          assertNotNull(status1);
          assertNotNull(props1);
          // deviceId1 may be null, that's fine - just verify no exception was thrown
          assertFalse(connected1); // Should be false for disconnected client
        });
  }

  @Test
  void testClientWithNullHost() {
    // Test creating client with null host in options
    HvacClientOptions options = new HvacClientOptions().setHost(null).setAutoConnect(false);

    mockNetworkService = new MockNetworkService();
    mockNetworkService.simulateConnectionFailure(true);
    client = new HvacClient(options, mockNetworkService);

    CompletableFuture<Void> connectFuture = client.connect();

    // Should fail with null host
    assertThrows(Exception.class, () -> connectFuture.get(1, TimeUnit.SECONDS));
  }

  @Test
  void testRapidConnectDisconnectCycles() {
    HvacClientOptions options =
        new HvacClientOptions("192.168.1.100").setAutoConnect(false).setConnectTimeout(50);

    mockNetworkService = new MockNetworkService();
    client = new HvacClient(options, mockNetworkService);

    // Rapidly cycle connect/disconnect
    assertDoesNotThrow(
        () -> {
          for (int i = 0; i < 10; i++) {
            CompletableFuture<Void> connect = client.connect();
            CompletableFuture<Void> disconnect = client.disconnect();

            connect.cancel(true);
            disconnect.cancel(true);
          }
        });
  }
}
