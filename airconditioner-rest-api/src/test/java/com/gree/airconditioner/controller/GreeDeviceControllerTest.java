package com.gree.airconditioner.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.gree.airconditioner.dto.api.ApiResponse;
import com.gree.airconditioner.dto.api.DeviceControlDto;
import com.gree.airconditioner.dto.api.DeviceInfoDto;
import com.gree.airconditioner.dto.api.DeviceStatusDto;
import com.gree.airconditioner.service.HvacDeviceService;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
@DisplayName("GreeDeviceController Tests")
class GreeDeviceControllerTest {

  @Mock private HvacDeviceService hvacDeviceService;

  @InjectMocks private GreeDeviceController controller;

  private DeviceInfoDto mockDeviceInfoDto;
  private DeviceStatusDto mockDeviceStatusDto;
  private DeviceControlDto mockDeviceControlDto;

  @BeforeEach
  void setUp() {
    // Setup mock DeviceInfoDto
    mockDeviceInfoDto = new DeviceInfoDto();
    mockDeviceInfoDto.setId("device-001");
    mockDeviceInfoDto.setName("Test AC Unit");
    mockDeviceInfoDto.setBrand("GREE");
    mockDeviceInfoDto.setModel("KFR-35GW");
    mockDeviceInfoDto.setVersion("1.0.0");
    mockDeviceInfoDto.setMacAddress("AA:BB:CC:DD:EE:FF");
    mockDeviceInfoDto.setIpAddress("192.168.1.100");
    mockDeviceInfoDto.setConnected(false);
    mockDeviceInfoDto.setStatus("Discovered");

    // Setup mock DeviceStatusDto
    mockDeviceStatusDto = new DeviceStatusDto();
    mockDeviceStatusDto.setDeviceId("device-001");
    mockDeviceStatusDto.setPower(true);
    mockDeviceStatusDto.setTemperature(22);
    mockDeviceStatusDto.setCurrentTemperature(24);
    mockDeviceStatusDto.setMode("COOL");
    mockDeviceStatusDto.setFanSpeed("AUTO");
    mockDeviceStatusDto.setSwingHorizontal("DEFAULT");
    mockDeviceStatusDto.setSwingVertical("DEFAULT");
    mockDeviceStatusDto.setLights(true);
    mockDeviceStatusDto.setTurbo(false);
    mockDeviceStatusDto.setQuiet(false);
    mockDeviceStatusDto.setHealth(false);
    mockDeviceStatusDto.setPowerSave(false);
    mockDeviceStatusDto.setSleep(false);

    // Setup mock DeviceControlDto
    mockDeviceControlDto = new DeviceControlDto();
    mockDeviceControlDto.setPower(true);
    mockDeviceControlDto.setTemperature(22);
    mockDeviceControlDto.setMode("COOL");
    mockDeviceControlDto.setFanSpeed("AUTO");
  }

  @Test
  @DisplayName("Should discover devices successfully")
  void shouldDiscoverDevicesSuccessfully() throws ExecutionException, InterruptedException {
    // Given
    List<DeviceInfoDto> mockDevices = Arrays.asList(mockDeviceInfoDto);
    CompletableFuture<List<DeviceInfoDto>> mockFuture =
        CompletableFuture.completedFuture(mockDevices);
    when(hvacDeviceService.discoverDevices()).thenReturn(mockFuture);

    // When
    CompletableFuture<ResponseEntity<ApiResponse<List<DeviceInfoDto>>>> result =
        controller.discoverDevices();
    ResponseEntity<ApiResponse<List<DeviceInfoDto>>> response = result.get();

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isSuccess());
    assertEquals("Devices discovered successfully", response.getBody().getMessage());
    assertEquals(mockDevices, response.getBody().getData());
    assertEquals(1, response.getBody().getData().size());

    verify(hvacDeviceService).discoverDevices();
  }

  @Test
  @DisplayName("Should handle device discovery failure")
  void shouldHandleDeviceDiscoveryFailure() throws ExecutionException, InterruptedException {
    // Given
    RuntimeException exception = new RuntimeException("Discovery failed");
    CompletableFuture<List<DeviceInfoDto>> mockFuture = CompletableFuture.failedFuture(exception);
    when(hvacDeviceService.discoverDevices()).thenReturn(mockFuture);

    // When
    CompletableFuture<ResponseEntity<ApiResponse<List<DeviceInfoDto>>>> result =
        controller.discoverDevices();
    ResponseEntity<ApiResponse<List<DeviceInfoDto>>> response = result.get();

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().isSuccess());
    assertTrue(response.getBody().getMessage().contains("Failed to discover devices"));
    assertTrue(response.getBody().getMessage().contains("Discovery failed"));

    verify(hvacDeviceService).discoverDevices();
  }

  @Test
  @DisplayName("Should get all devices successfully")
  void shouldGetAllDevicesSuccessfully() {
    // Given
    List<DeviceInfoDto> mockDevices = Arrays.asList(mockDeviceInfoDto);
    when(hvacDeviceService.getDevices()).thenReturn(mockDevices);

    // When
    ResponseEntity<ApiResponse<List<DeviceInfoDto>>> response = controller.getAllDevices();

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isSuccess());
    assertEquals(mockDevices, response.getBody().getData());
    assertEquals(1, response.getBody().getData().size());

    verify(hvacDeviceService).getDevices();
  }

  @Test
  @DisplayName("Should handle get all devices failure")
  void shouldHandleGetAllDevicesFailure() {
    // Given
    RuntimeException exception = new RuntimeException("Service unavailable");
    when(hvacDeviceService.getDevices()).thenThrow(exception);

    // When
    ResponseEntity<ApiResponse<List<DeviceInfoDto>>> response = controller.getAllDevices();

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().isSuccess());
    assertTrue(response.getBody().getMessage().contains("Failed to retrieve devices"));
    assertTrue(response.getBody().getMessage().contains("Service unavailable"));

    verify(hvacDeviceService).getDevices();
  }

  @Test
  @DisplayName("Should connect to device successfully")
  void shouldConnectToDeviceSuccessfully() throws ExecutionException, InterruptedException {
    // Given
    String deviceId = "device-001";
    CompletableFuture<Boolean> mockFuture = CompletableFuture.completedFuture(true);
    when(hvacDeviceService.connectToDevice(deviceId)).thenReturn(mockFuture);

    // When
    CompletableFuture<ResponseEntity<ApiResponse<String>>> result =
        controller.connectToDevice(deviceId);
    ResponseEntity<ApiResponse<String>> response = result.get();

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isSuccess());
    assertEquals("Connected to device successfully", response.getBody().getMessage());
    assertEquals("Connected", response.getBody().getData());

    verify(hvacDeviceService).connectToDevice(deviceId);
  }

  @Test
  @DisplayName("Should handle connection failure")
  void shouldHandleConnectionFailure() throws ExecutionException, InterruptedException {
    // Given
    String deviceId = "device-001";
    CompletableFuture<Boolean> mockFuture = CompletableFuture.completedFuture(false);
    when(hvacDeviceService.connectToDevice(deviceId)).thenReturn(mockFuture);

    // When
    CompletableFuture<ResponseEntity<ApiResponse<String>>> result =
        controller.connectToDevice(deviceId);
    ResponseEntity<ApiResponse<String>> response = result.get();

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().isSuccess());
    assertEquals("Failed to connect to device", response.getBody().getMessage());

    verify(hvacDeviceService).connectToDevice(deviceId);
  }

  @Test
  @DisplayName("Should handle connection exception")
  void shouldHandleConnectionException() throws ExecutionException, InterruptedException {
    // Given
    String deviceId = "device-001";
    RuntimeException exception = new RuntimeException("Connection timeout");
    CompletableFuture<Boolean> mockFuture = CompletableFuture.failedFuture(exception);
    when(hvacDeviceService.connectToDevice(deviceId)).thenReturn(mockFuture);

    // When
    CompletableFuture<ResponseEntity<ApiResponse<String>>> result =
        controller.connectToDevice(deviceId);
    ResponseEntity<ApiResponse<String>> response = result.get();

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().isSuccess());
    assertTrue(response.getBody().getMessage().contains("Connection failed"));
    assertTrue(response.getBody().getMessage().contains("Connection timeout"));

    verify(hvacDeviceService).connectToDevice(deviceId);
  }

  @Test
  @DisplayName("Should disconnect from device successfully")
  void shouldDisconnectFromDeviceSuccessfully() throws ExecutionException, InterruptedException {
    // Given
    String deviceId = "device-001";
    CompletableFuture<Boolean> mockFuture = CompletableFuture.completedFuture(true);
    when(hvacDeviceService.disconnectFromDevice(deviceId)).thenReturn(mockFuture);

    // When
    CompletableFuture<ResponseEntity<ApiResponse<String>>> result =
        controller.disconnectFromDevice(deviceId);
    ResponseEntity<ApiResponse<String>> response = result.get();

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isSuccess());
    assertEquals("Disconnected from device successfully", response.getBody().getMessage());
    assertEquals("Disconnected", response.getBody().getData());

    verify(hvacDeviceService).disconnectFromDevice(deviceId);
  }

  @Test
  @DisplayName("Should handle disconnection failure")
  void shouldHandleDisconnectionFailure() throws ExecutionException, InterruptedException {
    // Given
    String deviceId = "device-001";
    CompletableFuture<Boolean> mockFuture = CompletableFuture.completedFuture(false);
    when(hvacDeviceService.disconnectFromDevice(deviceId)).thenReturn(mockFuture);

    // When
    CompletableFuture<ResponseEntity<ApiResponse<String>>> result =
        controller.disconnectFromDevice(deviceId);
    ResponseEntity<ApiResponse<String>> response = result.get();

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().isSuccess());
    assertEquals("Failed to disconnect from device", response.getBody().getMessage());

    verify(hvacDeviceService).disconnectFromDevice(deviceId);
  }

  @Test
  @DisplayName("Should get device status successfully")
  void shouldGetDeviceStatusSuccessfully() throws ExecutionException, InterruptedException {
    // Given
    String deviceId = "device-001";
    CompletableFuture<DeviceStatusDto> mockFuture =
        CompletableFuture.completedFuture(mockDeviceStatusDto);
    when(hvacDeviceService.getDeviceStatus(deviceId)).thenReturn(mockFuture);

    // When
    CompletableFuture<ResponseEntity<ApiResponse<DeviceStatusDto>>> result =
        controller.getDeviceStatus(deviceId);
    ResponseEntity<ApiResponse<DeviceStatusDto>> response = result.get();

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isSuccess());
    assertEquals("Device status retrieved successfully", response.getBody().getMessage());
    assertEquals(mockDeviceStatusDto, response.getBody().getData());

    verify(hvacDeviceService).getDeviceStatus(deviceId);
  }

  @Test
  @DisplayName("Should handle device status failure")
  void shouldHandleDeviceStatusFailure() throws ExecutionException, InterruptedException {
    // Given
    String deviceId = "device-001";
    RuntimeException exception = new RuntimeException("Device not responding");
    CompletableFuture<DeviceStatusDto> mockFuture = CompletableFuture.failedFuture(exception);
    when(hvacDeviceService.getDeviceStatus(deviceId)).thenReturn(mockFuture);

    // When
    CompletableFuture<ResponseEntity<ApiResponse<DeviceStatusDto>>> result =
        controller.getDeviceStatus(deviceId);
    ResponseEntity<ApiResponse<DeviceStatusDto>> response = result.get();

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().isSuccess());
    assertTrue(response.getBody().getMessage().contains("Failed to get device status"));
    assertTrue(response.getBody().getMessage().contains("Device not responding"));

    verify(hvacDeviceService).getDeviceStatus(deviceId);
  }

  @Test
  @DisplayName("Should control device successfully")
  void shouldControlDeviceSuccessfully() throws ExecutionException, InterruptedException {
    // Given
    String deviceId = "device-001";
    CompletableFuture<Boolean> mockFuture = CompletableFuture.completedFuture(true);
    when(hvacDeviceService.controlDevice(deviceId, mockDeviceControlDto)).thenReturn(mockFuture);

    // When
    CompletableFuture<ResponseEntity<ApiResponse<String>>> result =
        controller.controlDevice(deviceId, mockDeviceControlDto);
    ResponseEntity<ApiResponse<String>> response = result.get();

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isSuccess());
    assertEquals("Device controlled successfully", response.getBody().getMessage());
    assertEquals("Success", response.getBody().getData());

    verify(hvacDeviceService).controlDevice(deviceId, mockDeviceControlDto);
  }

  @Test
  @DisplayName("Should handle device control failure")
  void shouldHandleDeviceControlFailure() throws ExecutionException, InterruptedException {
    // Given
    String deviceId = "device-001";
    CompletableFuture<Boolean> mockFuture = CompletableFuture.completedFuture(false);
    when(hvacDeviceService.controlDevice(deviceId, mockDeviceControlDto)).thenReturn(mockFuture);

    // When
    CompletableFuture<ResponseEntity<ApiResponse<String>>> result =
        controller.controlDevice(deviceId, mockDeviceControlDto);
    ResponseEntity<ApiResponse<String>> response = result.get();

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().isSuccess());
    assertEquals("Failed to control device", response.getBody().getMessage());

    verify(hvacDeviceService).controlDevice(deviceId, mockDeviceControlDto);
  }

  @Test
  @DisplayName("Should toggle power successfully")
  void shouldTogglePowerSuccessfully() throws ExecutionException, InterruptedException {
    // Given
    String deviceId = "device-001";
    boolean powerOn = true;
    CompletableFuture<Boolean> mockFuture = CompletableFuture.completedFuture(true);
    when(hvacDeviceService.controlDevice(eq(deviceId), any(DeviceControlDto.class)))
        .thenReturn(mockFuture);

    // When
    CompletableFuture<ResponseEntity<ApiResponse<String>>> result =
        controller.togglePower(deviceId, powerOn);
    ResponseEntity<ApiResponse<String>> response = result.get();

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isSuccess());
    assertEquals("Device controlled successfully", response.getBody().getMessage());
    assertEquals("Success", response.getBody().getData());

    verify(hvacDeviceService).controlDevice(eq(deviceId), any(DeviceControlDto.class));
  }

  @Test
  @DisplayName("Should set temperature successfully")
  void shouldSetTemperatureSuccessfully() throws ExecutionException, InterruptedException {
    // Given
    String deviceId = "device-001";
    int temperature = 24;
    CompletableFuture<Boolean> mockFuture = CompletableFuture.completedFuture(true);
    when(hvacDeviceService.controlDevice(eq(deviceId), any(DeviceControlDto.class)))
        .thenReturn(mockFuture);

    // When
    CompletableFuture<ResponseEntity<ApiResponse<String>>> result =
        controller.setTemperature(deviceId, temperature);
    ResponseEntity<ApiResponse<String>> response = result.get();

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isSuccess());
    assertEquals("Device controlled successfully", response.getBody().getMessage());
    assertEquals("Success", response.getBody().getData());

    verify(hvacDeviceService).controlDevice(eq(deviceId), any(DeviceControlDto.class));
  }

  @Test
  @DisplayName("Should set mode successfully")
  void shouldSetModeSuccessfully() throws ExecutionException, InterruptedException {
    // Given
    String deviceId = "device-001";
    String mode = "HEAT";
    CompletableFuture<Boolean> mockFuture = CompletableFuture.completedFuture(true);
    when(hvacDeviceService.controlDevice(eq(deviceId), any(DeviceControlDto.class)))
        .thenReturn(mockFuture);

    // When
    CompletableFuture<ResponseEntity<ApiResponse<String>>> result =
        controller.setMode(deviceId, mode);
    ResponseEntity<ApiResponse<String>> response = result.get();

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isSuccess());
    assertEquals("Device controlled successfully", response.getBody().getMessage());
    assertEquals("Success", response.getBody().getData());

    verify(hvacDeviceService).controlDevice(eq(deviceId), any(DeviceControlDto.class));
  }

  @Test
  @DisplayName("Should set fan speed successfully")
  void shouldSetFanSpeedSuccessfully() throws ExecutionException, InterruptedException {
    // Given
    String deviceId = "device-001";
    String fanSpeed = "HIGH";
    CompletableFuture<Boolean> mockFuture = CompletableFuture.completedFuture(true);
    when(hvacDeviceService.controlDevice(eq(deviceId), any(DeviceControlDto.class)))
        .thenReturn(mockFuture);

    // When
    CompletableFuture<ResponseEntity<ApiResponse<String>>> result =
        controller.setFanSpeed(deviceId, fanSpeed);
    ResponseEntity<ApiResponse<String>> response = result.get();

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isSuccess());
    assertEquals("Device controlled successfully", response.getBody().getMessage());
    assertEquals("Success", response.getBody().getData());

    verify(hvacDeviceService).controlDevice(eq(deviceId), any(DeviceControlDto.class));
  }

  @Test
  @DisplayName("Should handle power toggle failure")
  void shouldHandlePowerToggleFailure() throws ExecutionException, InterruptedException {
    // Given
    String deviceId = "device-001";
    boolean powerOn = true;
    CompletableFuture<Boolean> mockFuture = CompletableFuture.completedFuture(false);
    when(hvacDeviceService.controlDevice(eq(deviceId), any(DeviceControlDto.class)))
        .thenReturn(mockFuture);

    // When
    CompletableFuture<ResponseEntity<ApiResponse<String>>> result =
        controller.togglePower(deviceId, powerOn);
    ResponseEntity<ApiResponse<String>> response = result.get();

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().isSuccess());
    assertEquals("Failed to control device", response.getBody().getMessage());

    verify(hvacDeviceService).controlDevice(eq(deviceId), any(DeviceControlDto.class));
  }

  @Test
  @DisplayName("Should handle temperature set failure")
  void shouldHandleTemperatureSetFailure() throws ExecutionException, InterruptedException {
    // Given
    String deviceId = "device-001";
    int temperature = 24;
    CompletableFuture<Boolean> mockFuture = CompletableFuture.completedFuture(false);
    when(hvacDeviceService.controlDevice(eq(deviceId), any(DeviceControlDto.class)))
        .thenReturn(mockFuture);

    // When
    CompletableFuture<ResponseEntity<ApiResponse<String>>> result =
        controller.setTemperature(deviceId, temperature);
    ResponseEntity<ApiResponse<String>> response = result.get();

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().isSuccess());
    assertEquals("Failed to control device", response.getBody().getMessage());

    verify(hvacDeviceService).controlDevice(eq(deviceId), any(DeviceControlDto.class));
  }

  @Test
  @DisplayName("Should handle mode set failure")
  void shouldHandleModeSetFailure() throws ExecutionException, InterruptedException {
    // Given
    String deviceId = "device-001";
    String mode = "HEAT";
    CompletableFuture<Boolean> mockFuture = CompletableFuture.completedFuture(false);
    when(hvacDeviceService.controlDevice(eq(deviceId), any(DeviceControlDto.class)))
        .thenReturn(mockFuture);

    // When
    CompletableFuture<ResponseEntity<ApiResponse<String>>> result =
        controller.setMode(deviceId, mode);
    ResponseEntity<ApiResponse<String>> response = result.get();

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().isSuccess());
    assertEquals("Failed to control device", response.getBody().getMessage());

    verify(hvacDeviceService).controlDevice(eq(deviceId), any(DeviceControlDto.class));
  }

  @Test
  @DisplayName("Should handle fan speed set failure")
  void shouldHandleFanSpeedSetFailure() throws ExecutionException, InterruptedException {
    // Given
    String deviceId = "device-001";
    String fanSpeed = "HIGH";
    CompletableFuture<Boolean> mockFuture = CompletableFuture.completedFuture(false);
    when(hvacDeviceService.controlDevice(eq(deviceId), any(DeviceControlDto.class)))
        .thenReturn(mockFuture);

    // When
    CompletableFuture<ResponseEntity<ApiResponse<String>>> result =
        controller.setFanSpeed(deviceId, fanSpeed);
    ResponseEntity<ApiResponse<String>> response = result.get();

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().isSuccess());
    assertEquals("Failed to control device", response.getBody().getMessage());

    verify(hvacDeviceService).controlDevice(eq(deviceId), any(DeviceControlDto.class));
  }
}
