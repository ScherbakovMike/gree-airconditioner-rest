package com.gree.airconditioner.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("HvacDeviceException Tests")
class HvacDeviceExceptionTest {

  @Test
  @DisplayName("Should create exception with message only")
  void shouldCreateExceptionWithMessageOnly() {
    String message = "Device connection failed";
    HvacDeviceException exception = new HvacDeviceException(message);

    assertEquals(message, exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  @DisplayName("Should create exception with message and cause")
  void shouldCreateExceptionWithMessageAndCause() {
    String message = "Device connection failed";
    Throwable cause = new RuntimeException("Network timeout");
    HvacDeviceException exception = new HvacDeviceException(message, cause);

    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }

  @Test
  @DisplayName("Should create exception with cause only")
  void shouldCreateExceptionWithCauseOnly() {
    Throwable cause = new RuntimeException("Network timeout");
    HvacDeviceException exception = new HvacDeviceException(cause);

    assertEquals(cause.toString(), exception.getMessage());
    assertEquals(cause, exception.getCause());
  }

  @Test
  @DisplayName("Should create exception with null message")
  void shouldCreateExceptionWithNullMessage() {
    HvacDeviceException exception = new HvacDeviceException((String) null);

    assertNull(exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  @DisplayName("Should create exception with null cause")
  void shouldCreateExceptionWithNullCause() {
    String message = "Device connection failed";
    HvacDeviceException exception = new HvacDeviceException(message, null);

    assertEquals(message, exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  @DisplayName("Should create exception with empty message")
  void shouldCreateExceptionWithEmptyMessage() {
    String message = "";
    HvacDeviceException exception = new HvacDeviceException(message);

    assertEquals(message, exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  @DisplayName("Should maintain exception hierarchy")
  void shouldMaintainExceptionHierarchy() {
    HvacDeviceException exception = new HvacDeviceException("Test message");

    assertTrue(exception instanceof RuntimeException);
    assertTrue(exception instanceof Exception);
    assertTrue(exception instanceof Throwable);
  }

  @Test
  @DisplayName("Should preserve stack trace information")
  void shouldPreserveStackTraceInformation() {
    HvacDeviceException exception = new HvacDeviceException("Test message");

    StackTraceElement[] stackTrace = exception.getStackTrace();
    assertNotNull(stackTrace);
    assertTrue(stackTrace.length > 0);

    // The first element should be this test method
    assertTrue(stackTrace[0].getMethodName().contains("shouldPreserveStackTraceInformation"));
  }
}
