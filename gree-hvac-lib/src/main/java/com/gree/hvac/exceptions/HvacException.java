package com.gree.hvac.exceptions;

/** Base exception for GREE HVAC client operations */
public class HvacException extends Exception {
  private final Throwable origin;
  private final Object props;

  public HvacException(String message) {
    super(message);
    this.origin = null;
    this.props = null;
  }

  public HvacException(String message, Throwable origin) {
    super(message, origin);
    this.origin = origin;
    this.props = null;
  }

  public HvacException(String message, Throwable origin, Object props) {
    super(message, origin);
    this.origin = origin;
    this.props = props;
  }

  public Throwable getOrigin() {
    return origin;
  }

  public Object getProps() {
    return props;
  }
}

/** Exception thrown when socket communication fails */
class HvacSocketException extends HvacException {
  public HvacSocketException(Throwable cause) {
    super(cause != null ? cause.getMessage() : null, cause);
  }
}

/** Exception thrown when JSON message parsing fails */
class HvacMessageParseException extends HvacException {
  public HvacMessageParseException(Throwable cause, Object props) {
    super(
        "Cannot parse device JSON response (" + (cause != null ? cause.getMessage() : "null") + ")",
        cause,
        props);
  }
}

/** Exception thrown when message decryption fails */
class HvacDecryptionException extends HvacException {
  public HvacDecryptionException(Throwable cause, Object props) {
    super(
        "Cannot decrypt message (" + (cause != null ? cause.getMessage() : "null") + ")",
        cause,
        props);
  }
}

/** Exception thrown when unknown message type is received */
class HvacUnknownMessageException extends HvacException {
  public HvacUnknownMessageException(Object props) {
    super("Unknown message type received", null, props);
  }
}

/** Exception thrown when client is not connected */
class HvacNotConnectedException extends HvacException {
  public HvacNotConnectedException() {
    super("Client is not connected to the HVAC device");
  }
}

/** Exception thrown when connection times out */
class HvacConnectionTimeoutException extends HvacException {
  public HvacConnectionTimeoutException() {
    super("Connection to HVAC device timed out");
  }
}

/** Exception thrown when connection is cancelled */
class HvacConnectionCancelledException extends HvacException {
  public HvacConnectionCancelledException() {
    super("Connection to HVAC device was cancelled");
  }
}
