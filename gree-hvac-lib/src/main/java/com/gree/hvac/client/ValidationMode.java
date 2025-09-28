package com.gree.hvac.client;

/**
 * Validation mode for GREE AC feature compatibility checking. Based on official GREE mobile app
 * behavior analysis.
 */
public enum ValidationMode {
  /**
   * No validation - allows all feature combinations (backward compatibility). Commands are sent to
   * device without any checks.
   */
  NONE,

  /**
   * Warning mode - validates features but only logs warnings. Invalid combinations are logged but
   * commands are still sent. This is the default mode for backward compatibility.
   */
  WARN,

  /**
   * Strict mode - validates features and throws exceptions for invalid combinations. Prevents
   * sending commands that are incompatible with current AC mode. Recommended for new applications
   * to ensure correct usage.
   */
  STRICT;

  /**
   * Gets the default validation mode (WARN for backward compatibility)
   *
   * @return Default validation mode
   */
  public static ValidationMode getDefault() {
    return WARN;
  }
}
