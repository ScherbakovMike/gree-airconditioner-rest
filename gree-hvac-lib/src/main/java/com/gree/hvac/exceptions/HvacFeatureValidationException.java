package com.gree.hvac.exceptions;

import java.util.List;
import java.util.Set;

/**
 * Runtime exception thrown when attempting to use features that are not available in the current AC
 * mode. This helps prevent incorrect usage based on the official GREE mobile app feature matrix.
 * Being a runtime exception, it doesn't require explicit handling but can be caught if desired.
 */
public class HvacFeatureValidationException extends RuntimeException {

  private final String mode;
  private final String feature;
  private final Set<String> availableModes;
  private final List<String> allErrors;

  /**
   * Constructor for single feature validation error
   *
   * @param feature The feature that failed validation
   * @param mode The current AC mode
   * @param availableModes Set of modes where the feature is available
   */
  public HvacFeatureValidationException(String feature, String mode, Set<String> availableModes) {
    super(
        String.format(
            "Feature '%s' is not available in mode '%s'. Available in: %s",
            feature, mode, availableModes));
    this.feature = feature;
    this.mode = mode;
    this.availableModes = availableModes;
    this.allErrors = List.of(getMessage());
  }

  /**
   * Constructor for multiple validation errors
   *
   * @param mode The current AC mode
   * @param errors List of all validation error messages
   */
  public HvacFeatureValidationException(String mode, List<String> errors) {
    super(
        "Multiple feature validation errors in mode '" + mode + "': " + String.join("; ", errors));
    this.mode = mode;
    this.feature = null;
    this.availableModes = null;
    this.allErrors = errors;
  }

  /**
   * Gets the current AC mode that caused the validation failure
   *
   * @return The AC mode
   */
  public String getMode() {
    return mode;
  }

  /**
   * Gets the specific feature that failed validation (if single error)
   *
   * @return The feature name, or null if multiple errors
   */
  public String getFeature() {
    return feature;
  }

  /**
   * Gets the modes where the feature is available (if single error)
   *
   * @return Set of available modes, or null if multiple errors
   */
  public Set<String> getAvailableModes() {
    return availableModes;
  }

  /**
   * Gets all validation error messages
   *
   * @return List of error messages
   */
  public List<String> getAllErrors() {
    return allErrors;
  }

  /**
   * Checks if this exception represents multiple validation errors
   *
   * @return true if multiple errors, false if single error
   */
  public boolean hasMultipleErrors() {
    return allErrors.size() > 1;
  }
}
