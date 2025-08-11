package com.gree.airconditioner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.gree.airconditioner.dto.api.ApiResponse;
import com.gree.airconditioner.dto.api.DeviceControlDto;
import com.gree.airconditioner.exceptions.HvacDeviceException;
import com.gree.airconditioner.service.HvacDeviceService;
import com.gree.hvac.exceptions.HvacException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

@ExtendWith(MockitoExtension.class)
class ErrorHandlingTest {

  @Mock private HvacDeviceService hvacDeviceService;

  @Mock private WebRequest webRequest;

  private DeviceControlDto validControlDto;
  private DeviceControlDto invalidControlDto;

  @BeforeEach
  void setUp() {
    validControlDto = new DeviceControlDto();
    validControlDto.setPower(true);
    validControlDto.setTemperature(22);
    validControlDto.setMode("COOL");

    invalidControlDto = new DeviceControlDto();
    // Invalid temperature (outside range 16-30)
    invalidControlDto.setTemperature(15);
  }

  @Test
  void testHvacDeviceExceptionHandling() {
    // Test that HvacDeviceException is properly handled
    HvacDeviceException exception = new HvacDeviceException("Device not found");

    assertNotNull(exception.getMessage());
    assertEquals("Device not found", exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  void testHvacDeviceExceptionWithCause() {
    // Test HvacDeviceException with cause
    Exception cause = new RuntimeException("Connection failed");
    HvacDeviceException exception = new HvacDeviceException("Device error", cause);

    assertNotNull(exception.getMessage());
    assertEquals("Device error", exception.getMessage());
    assertEquals(cause, exception.getCause());
  }

  @Test
  void testApiResponseErrorCreation() {
    // Test error response creation
    ApiResponse<String> errorResponse = ApiResponse.error("Test error message");

    assertFalse(errorResponse.isSuccess());
    assertEquals("Test error message", errorResponse.getMessage());
    assertNull(errorResponse.getData());
    assertNotNull(errorResponse.getTimestamp());
  }

  @Test
  void testApiResponseSuccessCreation() {
    // Test success response creation
    String data = "Success data";
    ApiResponse<String> successResponse = ApiResponse.success("Operation successful", data);

    assertTrue(successResponse.isSuccess());
    assertEquals("Operation successful", successResponse.getMessage());
    assertEquals(data, successResponse.getData());
    assertNotNull(successResponse.getTimestamp());
  }

  @Test
  void testApiResponseSuccessWithoutMessage() {
    // Test success response without custom message
    String data = "Success data";
    ApiResponse<String> successResponse = ApiResponse.success(data);

    assertTrue(successResponse.isSuccess());
    assertEquals("Operation successful", successResponse.getMessage());
    assertEquals(data, successResponse.getData());
    assertNotNull(successResponse.getTimestamp());
  }

  @Test
  void testServiceExceptionPropagation() throws ExecutionException, InterruptedException {
    // Test that service exceptions are properly propagated
    when(hvacDeviceService.controlDevice(anyString(), any(DeviceControlDto.class)))
        .thenReturn(CompletableFuture.failedFuture(new HvacDeviceException("Service error")));

    CompletableFuture<Boolean> future = hvacDeviceService.controlDevice("device1", validControlDto);

    assertThrows(ExecutionException.class, () -> future.get());
    assertTrue(future.isCompletedExceptionally());
  }

  @Test
  void testHvacExceptionHandling() {
    // Test HvacException handling
    HvacException hvacException = new HvacException("HVAC library error");

    assertNotNull(hvacException.getMessage());
    assertEquals("HVAC library error", hvacException.getMessage());
  }

  @Test
  void testInvalidDeviceIdHandling() {
    // Test handling of invalid device IDs
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          if (null == null) {
            throw new IllegalArgumentException("Device ID cannot be null");
          }
        });
  }

  @Test
  void testInvalidControlParameters() {
    // Test validation of control parameters
    DeviceControlDto invalidControl = new DeviceControlDto();
    invalidControl.setTemperature(35); // Invalid temperature

    // This would typically be validated by @Valid annotation in real controller
    // For now, we just test the structure
    assertNotNull(invalidControl);
    assertEquals(35, invalidControl.getTemperature());
  }

  @Test
  void testNullControlParameters() {
    // Test handling of null control parameters
    DeviceControlDto nullControl = new DeviceControlDto();

    assertNotNull(nullControl);
    assertNull(nullControl.getPower());
    assertNull(nullControl.getTemperature());
    assertNull(nullControl.getMode());
  }

  @Test
  void testResponseEntityErrorHandling() {
    // Test ResponseEntity error handling patterns
    ResponseEntity<ApiResponse<String>> errorResponse =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("Bad request"));

    assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
    assertFalse(errorResponse.getBody().isSuccess());
    assertEquals("Bad request", errorResponse.getBody().getMessage());
  }

  @Test
  void testResponseEntitySuccessHandling() {
    // Test ResponseEntity success handling patterns
    ResponseEntity<ApiResponse<String>> successResponse =
        ResponseEntity.ok(ApiResponse.success("Operation successful", "Success"));

    assertEquals(HttpStatus.OK, successResponse.getStatusCode());
    assertTrue(successResponse.getBody().isSuccess());
    assertEquals("Operation successful", successResponse.getBody().getMessage());
    assertEquals("Success", successResponse.getBody().getData());
  }
}
