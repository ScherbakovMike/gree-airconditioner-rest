package com.gree.airconditioner.dto.api;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ApiResponse Tests")
class ApiResponseTest {

  @Test
  @DisplayName("Should create successful response with data only")
  void shouldCreateSuccessfulResponseWithDataOnly() {
    String testData = "test data";
    ApiResponse<String> response = ApiResponse.success(testData);

    assertTrue(response.isSuccess());
    assertEquals("Operation successful", response.getMessage());
    assertEquals(testData, response.getData());
    assertNotNull(response.getTimestamp());
    assertTrue(response.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
  }

  @Test
  @DisplayName("Should create successful response with custom message and data")
  void shouldCreateSuccessfulResponseWithCustomMessageAndData() {
    String testData = "test data";
    String customMessage = "Custom success message";
    ApiResponse<String> response = ApiResponse.success(customMessage, testData);

    assertTrue(response.isSuccess());
    assertEquals(customMessage, response.getMessage());
    assertEquals(testData, response.getData());
    assertNotNull(response.getTimestamp());
  }

  @Test
  @DisplayName("Should create error response")
  void shouldCreateErrorResponse() {
    String errorMessage = "Something went wrong";
    ApiResponse<String> response = ApiResponse.error(errorMessage);

    assertFalse(response.isSuccess());
    assertEquals(errorMessage, response.getMessage());
    assertNull(response.getData());
    assertNotNull(response.getTimestamp());
  }

  @Test
  @DisplayName("Should create response with all fields using constructor")
  void shouldCreateResponseWithAllFieldsUsingConstructor() {
    String testData = "test data";
    String message = "Test message";
    LocalDateTime timestamp = LocalDateTime.now();

    ApiResponse<String> response = new ApiResponse<>(true, message, testData, timestamp);

    assertTrue(response.isSuccess());
    assertEquals(message, response.getMessage());
    assertEquals(testData, response.getData());
    assertEquals(timestamp, response.getTimestamp());
  }

  @Test
  @DisplayName("Should create empty response using no-args constructor")
  void shouldCreateEmptyResponseUsingNoArgsConstructor() {
    ApiResponse<String> response = new ApiResponse<>();

    assertFalse(response.isSuccess());
    assertNull(response.getMessage());
    assertNull(response.getData());
    assertNull(response.getTimestamp());
  }

  @Test
  @DisplayName("Should handle null data in success response")
  void shouldHandleNullDataInSuccessResponse() {
    ApiResponse<String> response = ApiResponse.success(null);

    assertTrue(response.isSuccess());
    assertEquals("Operation successful", response.getMessage());
    assertNull(response.getData());
    assertNotNull(response.getTimestamp());
  }

  @Test
  @DisplayName("Should handle empty string message in error response")
  void shouldHandleEmptyStringMessageInErrorResponse() {
    ApiResponse<String> response = ApiResponse.error("");

    assertFalse(response.isSuccess());
    assertEquals("", response.getMessage());
    assertNull(response.getData());
    assertNotNull(response.getTimestamp());
  }

  @Test
  @DisplayName("Should handle null message in error response")
  void shouldHandleNullMessageInErrorResponse() {
    ApiResponse<String> response = ApiResponse.error(null);

    assertFalse(response.isSuccess());
    assertNull(response.getMessage());
    assertNull(response.getData());
    assertNotNull(response.getTimestamp());
  }
}
