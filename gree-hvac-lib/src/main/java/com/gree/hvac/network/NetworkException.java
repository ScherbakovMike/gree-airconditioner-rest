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

/** Runtime exception thrown when socket creation fails */
class NetworkSocketException extends RuntimeException {
  public NetworkSocketException(String message, Throwable cause) {
    super(message, cause);
  }
}

/** Runtime exception thrown when address resolution fails */
class NetworkAddressException extends RuntimeException {
  public NetworkAddressException(String message, Throwable cause) {
    super(message, cause);
  }
}

/** Runtime exception thrown when data transmission fails */
class NetworkTransmissionException extends RuntimeException {
  public NetworkTransmissionException(String message, Throwable cause) {
    super(message, cause);
  }
}
