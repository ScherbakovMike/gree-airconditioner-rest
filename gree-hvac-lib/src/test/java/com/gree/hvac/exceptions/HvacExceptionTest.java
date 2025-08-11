package com.gree.hvac.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class HvacExceptionTest {

  @Test
  void testHvacExceptionWithMessageOnly() {
    String message = "Test exception message";
    HvacException exception = new HvacException(message);

    assertEquals(message, exception.getMessage());
    assertNull(exception.getOrigin());
    assertNull(exception.getProps());
    assertNull(exception.getCause());
  }

  @Test
  void testHvacExceptionWithMessageAndOrigin() {
    String message = "Test exception message";
    Throwable origin = new RuntimeException("Original error");
    HvacException exception = new HvacException(message, origin);

    assertEquals(message, exception.getMessage());
    assertEquals(origin, exception.getOrigin());
    assertEquals(origin, exception.getCause());
    assertNull(exception.getProps());
  }

  @Test
  void testHvacExceptionWithMessageOriginAndProps() {
    String message = "Test exception message";
    Throwable origin = new RuntimeException("Original error");
    Object props = new Object();
    HvacException exception = new HvacException(message, origin, props);

    assertEquals(message, exception.getMessage());
    assertEquals(origin, exception.getOrigin());
    assertEquals(origin, exception.getCause());
    assertEquals(props, exception.getProps());
  }

  @Test
  void testHvacExceptionWithNullMessage() {
    HvacException exception = new HvacException(null);

    assertNull(exception.getMessage());
    assertNull(exception.getOrigin());
    assertNull(exception.getProps());
    assertNull(exception.getCause());
  }

  @Test
  void testHvacExceptionWithNullOrigin() {
    String message = "Test exception message";
    HvacException exception = new HvacException(message, null);

    assertEquals(message, exception.getMessage());
    assertNull(exception.getOrigin());
    assertNull(exception.getCause());
    assertNull(exception.getProps());
  }

  @Test
  void testHvacExceptionWithNullProps() {
    String message = "Test exception message";
    Throwable origin = new RuntimeException("Original error");
    HvacException exception = new HvacException(message, origin, null);

    assertEquals(message, exception.getMessage());
    assertEquals(origin, exception.getOrigin());
    assertEquals(origin, exception.getCause());
    assertNull(exception.getProps());
  }

  @Test
  void testHvacExceptionInheritance() {
    String message = "Test exception message";
    HvacException exception = new HvacException(message);

    assertTrue(exception instanceof Exception);
    assertTrue(exception instanceof HvacException);
  }

  @Test
  void testHvacExceptionStackTrace() {
    HvacException exception = new HvacException("Test message");
    StackTraceElement[] stackTrace = exception.getStackTrace();

    assertNotNull(stackTrace);
    assertTrue(stackTrace.length > 0);
  }
}

class HvacSocketExceptionTest {

  @Test
  void testHvacSocketExceptionWithCause() {
    Throwable cause = new RuntimeException("Socket error");
    HvacSocketException exception = new HvacSocketException(cause);

    assertEquals(cause.getMessage(), exception.getMessage());
    assertEquals(cause, exception.getOrigin());
    assertEquals(cause, exception.getCause());
    assertNull(exception.getProps());
  }

  @Test
  void testHvacSocketExceptionWithNullCause() {
    HvacSocketException exception = new HvacSocketException(null);

    assertNull(exception.getMessage());
    assertNull(exception.getOrigin());
    assertNull(exception.getCause());
    assertNull(exception.getProps());
  }

  @Test
  void testHvacSocketExceptionInheritance() {
    Throwable cause = new RuntimeException("Socket error");
    HvacSocketException exception = new HvacSocketException(cause);

    assertTrue(exception instanceof HvacException);
    assertTrue(exception instanceof Exception);
  }
}

class HvacMessageParseExceptionTest {

  @Test
  void testHvacMessageParseExceptionWithCauseAndProps() {
    Throwable cause = new RuntimeException("JSON parse error");
    Object props = "{\"invalid\": json}";
    HvacMessageParseException exception = new HvacMessageParseException(cause, props);

    assertTrue(exception.getMessage().contains("Cannot parse device JSON response"));
    assertTrue(exception.getMessage().contains("JSON parse error"));
    assertEquals(cause, exception.getOrigin());
    assertEquals(cause, exception.getCause());
    assertEquals(props, exception.getProps());
  }

  @Test
  void testHvacMessageParseExceptionWithNullCause() {
    Object props = "{\"invalid\": json}";
    HvacMessageParseException exception = new HvacMessageParseException(null, props);

    assertTrue(exception.getMessage().contains("Cannot parse device JSON response"));
    assertTrue(exception.getMessage().contains("null"));
    assertNull(exception.getOrigin());
    assertNull(exception.getCause());
    assertEquals(props, exception.getProps());
  }

  @Test
  void testHvacMessageParseExceptionWithNullProps() {
    Throwable cause = new RuntimeException("JSON parse error");
    HvacMessageParseException exception = new HvacMessageParseException(cause, null);

    assertTrue(exception.getMessage().contains("Cannot parse device JSON response"));
    assertTrue(exception.getMessage().contains("JSON parse error"));
    assertEquals(cause, exception.getOrigin());
    assertEquals(cause, exception.getCause());
    assertNull(exception.getProps());
  }

  @Test
  void testHvacMessageParseExceptionInheritance() {
    Throwable cause = new RuntimeException("JSON parse error");
    Object props = "{\"invalid\": json}";
    HvacMessageParseException exception = new HvacMessageParseException(cause, props);

    assertTrue(exception instanceof HvacException);
    assertTrue(exception instanceof Exception);
  }
}

class HvacDecryptionExceptionTest {

  @Test
  void testHvacDecryptionExceptionWithCauseAndProps() {
    Throwable cause = new RuntimeException("Decryption error");
    Object props = "encrypted_data";
    HvacDecryptionException exception = new HvacDecryptionException(cause, props);

    assertTrue(exception.getMessage().contains("Cannot decrypt message"));
    assertTrue(exception.getMessage().contains("Decryption error"));
    assertEquals(cause, exception.getOrigin());
    assertEquals(cause, exception.getCause());
    assertEquals(props, exception.getProps());
  }

  @Test
  void testHvacDecryptionExceptionWithNullCause() {
    Object props = "encrypted_data";
    HvacDecryptionException exception = new HvacDecryptionException(null, props);

    assertTrue(exception.getMessage().contains("Cannot decrypt message"));
    assertTrue(exception.getMessage().contains("null"));
    assertNull(exception.getOrigin());
    assertNull(exception.getCause());
    assertEquals(props, exception.getProps());
  }

  @Test
  void testHvacDecryptionExceptionWithNullProps() {
    Throwable cause = new RuntimeException("Decryption error");
    HvacDecryptionException exception = new HvacDecryptionException(cause, null);

    assertTrue(exception.getMessage().contains("Cannot decrypt message"));
    assertTrue(exception.getMessage().contains("Decryption error"));
    assertEquals(cause, exception.getOrigin());
    assertEquals(cause, exception.getCause());
    assertNull(exception.getProps());
  }

  @Test
  void testHvacDecryptionExceptionInheritance() {
    Throwable cause = new RuntimeException("Decryption error");
    Object props = "encrypted_data";
    HvacDecryptionException exception = new HvacDecryptionException(cause, props);

    assertTrue(exception instanceof HvacException);
    assertTrue(exception instanceof Exception);
  }
}

class HvacUnknownMessageExceptionTest {

  @Test
  void testHvacUnknownMessageExceptionWithProps() {
    Object props = "unknown_message_type";
    HvacUnknownMessageException exception = new HvacUnknownMessageException(props);

    assertEquals("Unknown message type received", exception.getMessage());
    assertNull(exception.getOrigin());
    assertNull(exception.getCause());
    assertEquals(props, exception.getProps());
  }

  @Test
  void testHvacUnknownMessageExceptionWithNullProps() {
    HvacUnknownMessageException exception = new HvacUnknownMessageException(null);

    assertEquals("Unknown message type received", exception.getMessage());
    assertNull(exception.getOrigin());
    assertNull(exception.getCause());
    assertNull(exception.getProps());
  }

  @Test
  void testHvacUnknownMessageExceptionInheritance() {
    Object props = "unknown_message_type";
    HvacUnknownMessageException exception = new HvacUnknownMessageException(props);

    assertTrue(exception instanceof HvacException);
    assertTrue(exception instanceof Exception);
  }
}

class HvacNotConnectedExceptionTest {

  @Test
  void testHvacNotConnectedException() {
    HvacNotConnectedException exception = new HvacNotConnectedException();

    assertEquals("Client is not connected to the HVAC device", exception.getMessage());
    assertNull(exception.getOrigin());
    assertNull(exception.getCause());
    assertNull(exception.getProps());
  }

  @Test
  void testHvacNotConnectedExceptionInheritance() {
    HvacNotConnectedException exception = new HvacNotConnectedException();

    assertTrue(exception instanceof HvacException);
    assertTrue(exception instanceof Exception);
  }
}

class HvacConnectionTimeoutExceptionTest {

  @Test
  void testHvacConnectionTimeoutException() {
    HvacConnectionTimeoutException exception = new HvacConnectionTimeoutException();

    assertEquals("Connection to HVAC device timed out", exception.getMessage());
    assertNull(exception.getOrigin());
    assertNull(exception.getCause());
    assertNull(exception.getProps());
  }

  @Test
  void testHvacConnectionTimeoutExceptionInheritance() {
    HvacConnectionTimeoutException exception = new HvacConnectionTimeoutException();

    assertTrue(exception instanceof HvacException);
    assertTrue(exception instanceof Exception);
  }
}

class HvacConnectionCancelledExceptionTest {

  @Test
  void testHvacConnectionCancelledException() {
    HvacConnectionCancelledException exception = new HvacConnectionCancelledException();

    assertEquals("Connection to HVAC device was cancelled", exception.getMessage());
    assertNull(exception.getOrigin());
    assertNull(exception.getCause());
    assertNull(exception.getProps());
  }

  @Test
  void testHvacConnectionCancelledExceptionInheritance() {
    HvacConnectionCancelledException exception = new HvacConnectionCancelledException();

    assertTrue(exception instanceof HvacException);
    assertTrue(exception instanceof Exception);
  }
}
