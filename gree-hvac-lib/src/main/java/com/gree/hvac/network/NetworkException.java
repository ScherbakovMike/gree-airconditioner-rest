package com.gree.hvac.network;

/** Base exception for network operations */
public class NetworkException extends Exception {
  public NetworkException(String message) {
    super(message);
  }

  public NetworkException(String message, Throwable cause) {
    super(message, cause);
  }
}

/** Exception thrown when socket creation fails */
class NetworkSocketException extends NetworkException {
  public NetworkSocketException(String message, Throwable cause) {
    super(message, cause);
  }
}

/** Exception thrown when address resolution fails */
class NetworkAddressException extends NetworkException {
  public NetworkAddressException(String message, Throwable cause) {
    super(message, cause);
  }
}

/** Exception thrown when data transmission fails */
class NetworkTransmissionException extends NetworkException {
  public NetworkTransmissionException(String message, Throwable cause) {
    super(message, cause);
  }
}
