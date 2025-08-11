package com.gree.hvac.discovery;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.gree.hvac.dto.DeviceInfo;
import java.net.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HvacDiscoveryTest {

  @Mock private NetworkService networkService;
  @Mock private SocketService socketService;
  @Mock private TimeService timeService;
  @Mock private CryptoService cryptoService;
  @Mock private DatagramSocket socket;
  @Mock private NetworkInterface networkInterface;
  @Mock private InterfaceAddress interfaceAddress;

  private HvacDiscovery hvacDiscovery;

  @BeforeEach
  void setUp() {
    hvacDiscovery = new HvacDiscovery(networkService, socketService, timeService, cryptoService);
  }

  @Test
  void testDiscoverDevicesOnAllInterfaces() throws Exception {
    // Arrange
    InetAddress broadcastAddress = InetAddress.getByName("192.168.1.255");
    Enumeration<NetworkInterface> interfaces = Collections.enumeration(List.of(networkInterface));

    when(networkService.getNetworkInterfaces()).thenReturn(interfaces);
    when(networkInterface.isLoopback()).thenReturn(false);
    when(networkInterface.isUp()).thenReturn(true);
    when(networkInterface.getName()).thenReturn("eth0");
    when(networkInterface.getInterfaceAddresses()).thenReturn(List.of(interfaceAddress));
    when(interfaceAddress.getBroadcast()).thenReturn(broadcastAddress);

    when(socketService.createSocket()).thenReturn(socket);
    when(timeService.getCurrentTimeMillis())
        .thenReturn(0L, 1000L, 4000L); // Start, during loop, after timeout

    DatagramPacket responsePacket = createMockResponsePacket();
    when(socketService.receivePacket(eq(socket), any(byte[].class))).thenReturn(responsePacket);

    String decryptedData =
        "{\"t\":\"dev\",\"name\":\"TestDevice\",\"ver\":\"1.0\",\"mac\":\"AA:BB:CC:DD:EE:FF\"}";
    when(cryptoService.decryptPackData(anyString())).thenReturn(decryptedData);

    // Act
    CompletableFuture<List<DeviceInfo>> result = hvacDiscovery.discoverDevices();
    List<DeviceInfo> devices = result.get();

    // Assert
    assertEquals(1, devices.size());
    DeviceInfo device = devices.get(0);
    assertEquals("TestDevice", device.getName());
    assertEquals("192.168.1.100", device.getIpAddress());
    assertEquals("AA:BB:CC:DD:EE:FF", device.getMacAddress());
    assertEquals("Gree", device.getBrand());

    verify(networkService).getNetworkInterfaces();
    verify(socketService).createSocket();
    verify(socketService).sendPacket(eq(socket), any(byte[].class), eq(broadcastAddress), eq(7000));
  }

  @Test
  void testDiscoverDevicesOnSpecificBroadcastAddress() throws Exception {
    // Arrange
    String broadcastAddr = "192.168.1.255";
    InetAddress broadcastAddress = InetAddress.getByName(broadcastAddr);

    when(networkService.getByName(broadcastAddr)).thenReturn(broadcastAddress);
    when(socketService.createSocket()).thenReturn(socket);
    when(timeService.getCurrentTimeMillis()).thenReturn(0L, 1000L, 4000L);

    DatagramPacket responsePacket = createMockResponsePacket();
    when(socketService.receivePacket(eq(socket), any(byte[].class))).thenReturn(responsePacket);

    String decryptedData =
        "{\"t\":\"dev\",\"name\":\"TestDevice\",\"ver\":\"1.0\",\"mac\":\"AA:BB:CC:DD:EE:FF\"}";
    when(cryptoService.decryptPackData(anyString())).thenReturn(decryptedData);

    // Act
    CompletableFuture<List<DeviceInfo>> result = hvacDiscovery.discoverDevices(broadcastAddr);
    List<DeviceInfo> devices = result.get();

    // Assert
    assertEquals(1, devices.size());
    assertEquals("TestDevice", devices.get(0).getName());

    verify(networkService).getByName(broadcastAddr);
    verify(socketService).sendPacket(eq(socket), any(byte[].class), eq(broadcastAddress), eq(7000));
  }

  @Test
  void testDiscoverDevicesHandlesNetworkException() throws Exception {
    // Arrange
    when(networkService.getNetworkInterfaces()).thenThrow(new RuntimeException("Network error"));

    // Act
    CompletableFuture<List<DeviceInfo>> result = hvacDiscovery.discoverDevices();
    List<DeviceInfo> devices = result.get();

    // Assert
    assertTrue(devices.isEmpty());
    verify(networkService).getNetworkInterfaces();
  }

  @Test
  void testDiscoverDevicesHandlesSocketException() throws Exception {
    // Arrange
    String broadcastAddr = "192.168.1.255";
    InetAddress broadcastAddress = InetAddress.getByName(broadcastAddr);

    when(networkService.getByName(broadcastAddr)).thenReturn(broadcastAddress);
    when(socketService.createSocket()).thenThrow(new RuntimeException("Socket error"));

    // Act
    CompletableFuture<List<DeviceInfo>> result = hvacDiscovery.discoverDevices(broadcastAddr);
    List<DeviceInfo> devices = result.get();

    // Assert
    assertTrue(devices.isEmpty());
    verify(socketService).createSocket();
  }

  @Test
  void testDiscoverDevicesIgnoresLoopbackInterfaces() throws Exception {
    // Arrange
    Enumeration<NetworkInterface> interfaces = Collections.enumeration(List.of(networkInterface));

    when(networkService.getNetworkInterfaces()).thenReturn(interfaces);
    when(networkInterface.isLoopback()).thenReturn(true);

    // Act
    CompletableFuture<List<DeviceInfo>> result = hvacDiscovery.discoverDevices();
    List<DeviceInfo> devices = result.get();

    // Assert
    assertTrue(devices.isEmpty());
    verify(networkInterface).isLoopback();
    verify(socketService, never()).createSocket();
  }

  @Test
  void testDiscoverDevicesIgnoresDownInterfaces() throws Exception {
    // Arrange
    Enumeration<NetworkInterface> interfaces = Collections.enumeration(List.of(networkInterface));

    when(networkService.getNetworkInterfaces()).thenReturn(interfaces);
    when(networkInterface.isLoopback()).thenReturn(false);
    when(networkInterface.isUp()).thenReturn(false);

    // Act
    CompletableFuture<List<DeviceInfo>> result = hvacDiscovery.discoverDevices();
    List<DeviceInfo> devices = result.get();

    // Assert
    assertTrue(devices.isEmpty());
    verify(networkInterface).isUp();
    verify(socketService, never()).createSocket();
  }

  @Test
  void testDiscoverDevicesHandlesSocketTimeout() throws Exception {
    // Arrange
    String broadcastAddr = "192.168.1.255";
    InetAddress broadcastAddress = InetAddress.getByName(broadcastAddr);

    when(networkService.getByName(broadcastAddr)).thenReturn(broadcastAddress);
    when(socketService.createSocket()).thenReturn(socket);
    when(timeService.getCurrentTimeMillis()).thenReturn(0L, 1000L);
    when(socketService.receivePacket(eq(socket), any(byte[].class)))
        .thenThrow(new SocketTimeoutException("Timeout"));

    // Act
    CompletableFuture<List<DeviceInfo>> result = hvacDiscovery.discoverDevices(broadcastAddr);
    List<DeviceInfo> devices = result.get();

    // Assert
    assertTrue(devices.isEmpty());
    verify(socketService).receivePacket(eq(socket), any(byte[].class));
  }

  @Test
  void testParseDeviceResponseReturnsNullForInvalidType() throws Exception {
    // Arrange
    String broadcastAddr = "192.168.1.255";
    InetAddress broadcastAddress = InetAddress.getByName(broadcastAddr);

    when(networkService.getByName(broadcastAddr)).thenReturn(broadcastAddress);
    when(socketService.createSocket()).thenReturn(socket);
    when(timeService.getCurrentTimeMillis()).thenReturn(0L, 1000L, 4000L);

    // Create packet with invalid type
    DatagramPacket responsePacket =
        createMockResponsePacket("{\"t\":\"invalid\",\"pack\":\"encrypted\"}");
    when(socketService.receivePacket(eq(socket), any(byte[].class))).thenReturn(responsePacket);

    // Act
    CompletableFuture<List<DeviceInfo>> result = hvacDiscovery.discoverDevices(broadcastAddr);
    List<DeviceInfo> devices = result.get();

    // Assert
    assertTrue(devices.isEmpty());
  }

  @Test
  void testParseDeviceResponseReturnsNullForDecryptionFailure() throws Exception {
    // Arrange
    String broadcastAddr = "192.168.1.255";
    InetAddress broadcastAddress = InetAddress.getByName(broadcastAddr);

    when(networkService.getByName(broadcastAddr)).thenReturn(broadcastAddress);
    when(socketService.createSocket()).thenReturn(socket);
    when(timeService.getCurrentTimeMillis()).thenReturn(0L, 1000L, 4000L);

    DatagramPacket responsePacket = createMockResponsePacket();
    when(socketService.receivePacket(eq(socket), any(byte[].class))).thenReturn(responsePacket);
    when(cryptoService.decryptPackData(anyString())).thenReturn(null);

    // Act
    CompletableFuture<List<DeviceInfo>> result = hvacDiscovery.discoverDevices(broadcastAddr);
    List<DeviceInfo> devices = result.get();

    // Assert
    assertTrue(devices.isEmpty());
    verify(cryptoService).decryptPackData(anyString());
  }

  @Test
  void testParseDeviceResponseReturnsNullForInvalidDeviceType() throws Exception {
    // Arrange
    String broadcastAddr = "192.168.1.255";
    InetAddress broadcastAddress = InetAddress.getByName(broadcastAddr);

    when(networkService.getByName(broadcastAddr)).thenReturn(broadcastAddress);
    when(socketService.createSocket()).thenReturn(socket);
    when(timeService.getCurrentTimeMillis()).thenReturn(0L, 1000L, 4000L);

    DatagramPacket responsePacket = createMockResponsePacket();
    when(socketService.receivePacket(eq(socket), any(byte[].class))).thenReturn(responsePacket);

    String decryptedData = "{\"t\":\"invalid\",\"name\":\"TestDevice\"}";
    when(cryptoService.decryptPackData(anyString())).thenReturn(decryptedData);

    // Act
    CompletableFuture<List<DeviceInfo>> result = hvacDiscovery.discoverDevices(broadcastAddr);
    List<DeviceInfo> devices = result.get();

    // Assert
    assertTrue(devices.isEmpty());
  }

  @Test
  void testDefaultConstructorCreatesDefaultServices() {
    // Act
    HvacDiscovery discovery = new HvacDiscovery();

    // Assert
    assertNotNull(discovery);
  }

  private DatagramPacket createMockResponsePacket() throws Exception {
    return createMockResponsePacket("{\"t\":\"pack\",\"pack\":\"encryptedData\"}");
  }

  private DatagramPacket createMockResponsePacket(String response) throws Exception {
    byte[] responseData = response.getBytes();
    InetAddress sourceAddress = InetAddress.getByName("192.168.1.100");

    DatagramPacket packet = mock(DatagramPacket.class);
    when(packet.getData()).thenReturn(responseData);
    when(packet.getLength()).thenReturn(responseData.length);
    when(packet.getAddress()).thenReturn(sourceAddress);

    return packet;
  }
}
