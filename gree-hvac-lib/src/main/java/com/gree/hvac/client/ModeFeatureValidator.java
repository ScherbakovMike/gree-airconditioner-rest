package com.gree.hvac.client;

import java.util.*;

/**
 * Validates feature availability based on AC operation mode. Based on official GREE mobile app
 * feature matrix.
 */
public class ModeFeatureValidator {

  private ModeFeatureValidator() {
    // Utility class - prevent instantiation
  }

  // Mode constants
  public static final String MODE_AUTO = "auto";
  public static final String MODE_COOL = "cool";
  public static final String MODE_HEAT = "heat";
  public static final String MODE_FAN = "fan";
  public static final String MODE_DRY = "dry";

  // Feature availability matrix based on GREE mobile app Table 1
  private static final Map<String, Set<String>> FEATURE_MODE_AVAILABILITY = new HashMap<>();

  // Wind setting availability matrix based on GREE mobile app Table 2
  private static final Map<String, Set<String>> WIND_SETTING_AVAILABILITY = new HashMap<>();

  static {
    // === FEATURE AVAILABILITY (Table 1) ===

    // X-Fan (Blow) - only available in Cool and Dry modes
    FEATURE_MODE_AVAILABILITY.put("blow", Set.of(MODE_COOL, MODE_DRY));

    // Health (UVC/Anion) - available in all modes
    FEATURE_MODE_AVAILABILITY.put(
        "health", Set.of(MODE_AUTO, MODE_COOL, MODE_HEAT, MODE_FAN, MODE_DRY));

    // UVC (alias for Health) - available in all modes
    FEATURE_MODE_AVAILABILITY.put(
        "uvc", Set.of(MODE_AUTO, MODE_COOL, MODE_HEAT, MODE_FAN, MODE_DRY));

    // SE (Energy Saving/PowerSave) - only available in Cool mode
    FEATURE_MODE_AVAILABILITY.put("powersave", Set.of(MODE_COOL));
    FEATURE_MODE_AVAILABILITY.put("energysaving", Set.of(MODE_COOL));

    // Safety Heating - typically available in all modes (device dependent)
    FEATURE_MODE_AVAILABILITY.put(
        "safetyheating", Set.of(MODE_AUTO, MODE_COOL, MODE_HEAT, MODE_FAN, MODE_DRY));

    // Fresh Air - typically available in all modes (device dependent)
    FEATURE_MODE_AVAILABILITY.put(
        "air", Set.of(MODE_AUTO, MODE_COOL, MODE_HEAT, MODE_FAN, MODE_DRY));

    // === WIND SETTING AVAILABILITY (Table 2) ===

    // Manual Fan Speed - available in all modes except Dry
    WIND_SETTING_AVAILABILITY.put("fanspeed", Set.of(MODE_AUTO, MODE_COOL, MODE_HEAT, MODE_FAN));

    // Wind Auto - available in all modes except Dry
    WIND_SETTING_AVAILABILITY.put("auto", Set.of(MODE_AUTO, MODE_COOL, MODE_HEAT, MODE_FAN));

    // Wind Quiet - available in all modes except Dry
    WIND_SETTING_AVAILABILITY.put("quiet", Set.of(MODE_AUTO, MODE_COOL, MODE_HEAT, MODE_FAN));

    // Wind Turbo - only available in Cool and Heat modes
    WIND_SETTING_AVAILABILITY.put("turbo", Set.of(MODE_COOL, MODE_HEAT));
  }

  /**
   * Validates if a feature is available in the specified mode
   *
   * @param feature The feature name (e.g., "blow", "health", "powerSave")
   * @param mode The current AC mode (e.g., "cool", "heat", "auto")
   * @return true if feature is available in the mode, false otherwise
   */
  public static boolean isFeatureAvailable(String feature, String mode) {
    if (feature == null || mode == null) {
      return false;
    }

    Set<String> availableModes = FEATURE_MODE_AVAILABILITY.get(feature.toLowerCase());
    if (availableModes == null) {
      // If feature is not in our matrix, assume it's available (backward compatibility)
      return true;
    }

    return availableModes.contains(mode.toLowerCase());
  }

  /**
   * Gets all modes where a feature is available
   *
   * @param feature The feature name
   * @return Set of mode names where the feature is available
   */
  public static Set<String> getAvailableModesForFeature(String feature) {
    if (feature == null) {
      return Collections.emptySet();
    }

    Set<String> modes = FEATURE_MODE_AVAILABILITY.get(feature.toLowerCase());
    return modes != null ? new HashSet<>(modes) : Collections.emptySet();
  }

  /**
   * Gets all features available in a specific mode
   *
   * @param mode The AC mode
   * @return Set of feature names available in the mode
   */
  public static Set<String> getAvailableFeaturesForMode(String mode) {
    if (mode == null) {
      return Collections.emptySet();
    }

    Set<String> features = new HashSet<>();
    String lowerMode = mode.toLowerCase();

    for (Map.Entry<String, Set<String>> entry : FEATURE_MODE_AVAILABILITY.entrySet()) {
      if (entry.getValue().contains(lowerMode)) {
        features.add(entry.getKey());
      }
    }

    return features;
  }

  /**
   * Validates if a wind setting is available in the specified mode
   *
   * @param windSetting The wind setting name (e.g., "fanspeed", "quiet", "turbo")
   * @param mode The current AC mode (e.g., "cool", "heat", "auto")
   * @return true if wind setting is available in the mode, false otherwise
   */
  public static boolean isWindSettingAvailable(String windSetting, String mode) {
    if (windSetting == null || mode == null) {
      return false;
    }

    Set<String> availableModes = WIND_SETTING_AVAILABILITY.get(windSetting.toLowerCase());
    if (availableModes == null) {
      // If wind setting is not in our matrix, assume it's available (backward compatibility)
      return true;
    }

    return availableModes.contains(mode.toLowerCase());
  }

  /**
   * Gets all modes where a wind setting is available
   *
   * @param windSetting The wind setting name
   * @return Set of mode names where the wind setting is available
   */
  public static Set<String> getAvailableModesForWindSetting(String windSetting) {
    if (windSetting == null) {
      return Collections.emptySet();
    }

    Set<String> modes = WIND_SETTING_AVAILABILITY.get(windSetting.toLowerCase());
    return modes != null ? new HashSet<>(modes) : Collections.emptySet();
  }

  /**
   * Validates a device control request against the current mode
   *
   * @param mode The current AC mode
   * @param requestedFeatures Map of feature names to their requested values
   * @return List of validation errors (empty if all valid)
   */
  public static List<String> validateFeatureRequest(
      String mode, Map<String, Object> requestedFeatures) {
    return validateFeatureRequest(mode, requestedFeatures, true);
  }

  /**
   * Validates a device control request against the current mode
   *
   * @param mode The current AC mode
   * @param requestedFeatures Map of feature names to their requested values
   * @param validateWindSettings Whether to validate wind settings
   * @return List of validation errors (empty if all valid)
   */
  public static List<String> validateFeatureRequest(
      String mode, Map<String, Object> requestedFeatures, boolean validateWindSettings) {
    List<String> errors = new ArrayList<>();

    if (mode == null) {
      errors.add("Mode cannot be null for feature validation");
      return errors;
    }

    for (Map.Entry<String, Object> entry : requestedFeatures.entrySet()) {
      String feature = entry.getKey();
      Object value = entry.getValue();

      // Only validate boolean features that are being turned ON
      if (value instanceof Boolean boolValue && boolValue && !isFeatureAvailable(feature, mode)) {
        Set<String> availableModes = getAvailableModesForFeature(feature);
        errors.add(
            String.format(
                "Feature '%s' is not available in mode '%s'. Available in: %s",
                feature, mode, availableModes));
      }

      // Validate wind settings if enabled
      if (validateWindSettings && value != null) {
        validateWindSettingForFeature(feature, value, mode, errors);
      }
    }

    return errors;
  }

  /** Validates wind setting based on feature and mode */
  private static void validateWindSettingForFeature(
      String feature, Object value, String mode, List<String> errors) {
    // Check specific wind settings
    if ("fanspeed".equalsIgnoreCase(feature) && value instanceof String) {
      String fanSpeed = (String) value;
      if (!"auto".equalsIgnoreCase(fanSpeed) && !isWindSettingAvailable("fanspeed", mode)) {
        errors.add(
            String.format(
                "Manual fan speed setting is not available in mode '%s'. Available in: %s",
                mode, getAvailableModesForWindSetting("fanspeed")));
      }
    } else if ("quiet".equalsIgnoreCase(feature)
        && value instanceof Boolean boolValue
        && boolValue) {
      if (!isWindSettingAvailable("quiet", mode)) {
        errors.add(
            String.format(
                "Quiet mode is not available in mode '%s'. Available in: %s",
                mode, getAvailableModesForWindSetting("quiet")));
      }
    } else if ("turbo".equalsIgnoreCase(feature)
        && value instanceof Boolean boolValue
        && boolValue
        && !isWindSettingAvailable("turbo", mode)) {
      errors.add(
          String.format(
              "Turbo mode is not available in mode '%s'. Available in: %s",
              mode, getAvailableModesForWindSetting("turbo")));
    }
  }

  /**
   * Creates a summary of feature availability for documentation
   *
   * @return Formatted string showing the feature-mode matrix
   */
  public static String getFeatureMatrix() {
    StringBuilder sb = new StringBuilder();
    sb.append("GREE AC Feature Availability by Mode (Based on Mobile App):\n\n");

    // Table 1: Features
    sb.append("=== TABLE 1: FEATURES ===\n");
    sb.append("Feature          | Auto | Cool | Heat | Fan  | Dry  |\n");
    sb.append("-----------------|------|------|------|------|------|\n");

    String[] features = {"blow", "health", "powersave", "safetyheating", "air"};
    String[] modes = {MODE_AUTO, MODE_COOL, MODE_HEAT, MODE_FAN, MODE_DRY};

    for (String feature : features) {
      sb.append(String.format("%-16s |", feature));
      for (String mode : modes) {
        String available = isFeatureAvailable(feature, mode) ? " ✓  " : " ✗  ";
        sb.append(available).append(" |");
      }
      sb.append("\n");
    }

    // Table 2: Wind Settings
    sb.append("\n=== TABLE 2: WIND SETTINGS ===\n");
    sb.append("Wind Setting     | Auto | Cool | Heat | Fan  | Dry  |\n");
    sb.append("-----------------|------|------|------|------|------|\n");

    String[] windSettings = {"fanspeed", "auto", "quiet", "turbo"};

    for (String windSetting : windSettings) {
      sb.append(String.format("%-16s |", windSetting));
      for (String mode : modes) {
        String available = isWindSettingAvailable(windSetting, mode) ? " ✓  " : " ✗  ";
        sb.append(available).append(" |");
      }
      sb.append("\n");
    }

    sb.append("\nNotes:\n");
    sb.append("- Dry mode has the most restrictions (no wind controls)\n");
    sb.append("- Turbo is only available in Cool and Heat modes\n");
    sb.append("- X-Fan (blow) only works in Cool and Dry modes\n");
    sb.append("- Energy Saving (powersave) only works in Cool mode\n");

    return sb.toString();
  }
}
