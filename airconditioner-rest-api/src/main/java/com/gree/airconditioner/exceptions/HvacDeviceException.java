package com.gree.airconditioner.exceptions;

/**
 * Exception thrown when HVAC device operations fail. This provides more specific error handling
 * than generic RuntimeException.
 */
public class HvacDeviceException extends RuntimeException {

  /**
   * Constructs a new HVAC device exception with the specified detail message.
   *
   * @param message the detail message
   */
  public HvacDeviceException(String message) {
    super(message);
  }

  /**
   * Constructs a new HVAC device exception with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the cause
   */
  public HvacDeviceException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new HVAC device exception with the specified cause.
   *
   * @param cause the cause
   */
  public HvacDeviceException(Throwable cause) {
    super(cause);
  }
}
