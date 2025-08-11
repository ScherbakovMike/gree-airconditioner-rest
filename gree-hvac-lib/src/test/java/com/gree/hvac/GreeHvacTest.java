package com.gree.hvac;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.gree.hvac.client.HvacClient;
import com.gree.hvac.client.HvacClientOptions;
import com.gree.hvac.dto.DeviceInfo;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GreeHvacTest {

  private DeviceInfo mockDeviceInfo;
  private HvacClient mockClient;

  @BeforeEach
  void setUp() {
    mockDeviceInfo = new DeviceInfo();
    mockDeviceInfo.setId("192.168.1.100");
    mockDeviceInfo.setName("Test AC Unit");
    mockDeviceInfo.setIpAddress("192.168.1.100");

    mockClient = mock(HvacClient.class);
  }

  @Test
  void testGetVersion() {
    assertEquals("1.0.0", GreeHvac.getVersion());
  }

  @Test
  void testDiscoverDevices() {
    // This test only validates the method can be called without throwing exceptions
    // We don't wait for the result as it would cause network operations in CI/CD
    CompletableFuture<List<DeviceInfo>> result = GreeHvac.discoverDevices();

    assertNotNull(result);
    assertFalse(result.isDone()); // Should be running asynchronously

    // Cancel the future to avoid network operations
    result.cancel(true);
  }

  @Test
  void testDiscoverDevicesWithBroadcastAddress() {
    String broadcastAddress = "192.168.1.255";

    // This test only validates the method can be called without throwing exceptions
    CompletableFuture<List<DeviceInfo>> result = GreeHvac.discoverDevices(broadcastAddress);

    assertNotNull(result);
    assertFalse(result.isDone()); // Should be running asynchronously

    // Cancel the future to avoid network operations
    result.cancel(true);
  }

  @Test
  void testCreateClientWithHost() {
    String host = "192.168.1.100";
    HvacClient client = GreeHvac.createClient(host);

    assertNotNull(client);
    // Note: We can't easily test the internal state without making fields accessible
    // This test verifies that a client is created without throwing exceptions
  }

  @Test
  void testCreateClientWithOptions() {
    HvacClientOptions options = new HvacClientOptions("192.168.1.100");
    options.setPort(7000);
    options.setConnectTimeout(5000);

    HvacClient client = GreeHvac.createClient(options);

    assertNotNull(client);
    // Test verifies client creation with custom options
  }

  @Test
  void testCreateClientWithDeviceInfo() {
    HvacClient client = GreeHvac.createClient(mockDeviceInfo);

    assertNotNull(client);
    // Test verifies client creation from device info
  }

  @Test
  void testCreateClientWithNullDeviceInfo() {
    assertThrows(NullPointerException.class, () -> GreeHvac.createClient((DeviceInfo) null));
  }

  @Test
  void testCreateClientWithNullHost() {
    assertThrows(NullPointerException.class, () -> GreeHvac.createClient((String) null));
  }

  @Test
  void testCreateClientWithNullOptions() {
    assertThrows(NullPointerException.class, () -> GreeHvac.createClient((HvacClientOptions) null));
  }

  @Test
  void testDiscoverDevicesWithInvalidBroadcastAddress() {
    // Test with clearly invalid broadcast address
    CompletableFuture<List<DeviceInfo>> result = GreeHvac.discoverDevices("invalid.address");

    assertNotNull(result);
    assertFalse(result.isDone()); // Should be running asynchronously

    // Cancel to avoid waiting for network timeout in CI/CD
    result.cancel(true);
  }
}
