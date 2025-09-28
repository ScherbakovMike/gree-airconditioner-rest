package com.gree.hvac.client;

import static org.junit.jupiter.api.Assertions.*;

import com.gree.hvac.dto.DeviceControl;
import com.gree.hvac.exceptions.HvacFeatureValidationException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Test strict validation functionality to prevent incorrect feature usage */
public class StrictValidationTest {

  @Test
  public void testValidationModeEnum() {
    // Test default validation mode
    assertEquals(ValidationMode.WARN, ValidationMode.getDefault());

    // Test all validation modes exist
    assertNotNull(ValidationMode.NONE);
    assertNotNull(ValidationMode.WARN);
    assertNotNull(ValidationMode.STRICT);
  }

  @Test
  public void testHvacClientOptionsValidationSettings() {
    HvacClientOptions options = new HvacClientOptions();

    // Test default values
    assertEquals(ValidationMode.WARN, options.getValidationMode());
    assertTrue(options.isValidateWindSettings());

    // Test setters return this for chaining
    assertSame(options, options.setValidationMode(ValidationMode.STRICT));
    assertSame(options, options.setValidateWindSettings(false));

    // Test values are set correctly
    assertEquals(ValidationMode.STRICT, options.getValidationMode());
    assertFalse(options.isValidateWindSettings());

    // Test null validation mode defaults to WARN
    options.setValidationMode(null);
    assertEquals(ValidationMode.WARN, options.getValidationMode());
  }

  @Test
  public void testModeFeatureValidatorWindSettings() {
    // Test wind setting availability based on mobile app Table 2

    // Dry mode should have NO wind controls
    assertFalse(ModeFeatureValidator.isWindSettingAvailable("fanspeed", "dry"));
    assertFalse(ModeFeatureValidator.isWindSettingAvailable("quiet", "dry"));
    assertFalse(ModeFeatureValidator.isWindSettingAvailable("turbo", "dry"));
    assertFalse(ModeFeatureValidator.isWindSettingAvailable("auto", "dry"));

    // Turbo should only be available in Cool and Heat
    assertTrue(ModeFeatureValidator.isWindSettingAvailable("turbo", "cool"));
    assertTrue(ModeFeatureValidator.isWindSettingAvailable("turbo", "heat"));
    assertFalse(ModeFeatureValidator.isWindSettingAvailable("turbo", "auto"));
    assertFalse(ModeFeatureValidator.isWindSettingAvailable("turbo", "fan"));
    assertFalse(ModeFeatureValidator.isWindSettingAvailable("turbo", "dry"));

    // Other wind settings should be available except in Dry mode
    String[] modes = {"auto", "cool", "heat", "fan"};
    String[] windSettings = {"fanspeed", "quiet", "auto"};

    for (String mode : modes) {
      for (String setting : windSettings) {
        assertTrue(
            ModeFeatureValidator.isWindSettingAvailable(setting, mode),
            setting + " should be available in " + mode + " mode");
      }
    }
  }

  @Test
  public void testValidateFeatureRequestWithWindSettings() {
    // Test Dry mode restrictions
    DeviceControl dryModeControl = new DeviceControl();
    dryModeControl.setMode("dry");
    dryModeControl.setFanSpeed("low"); // Should be invalid in dry mode
    dryModeControl.setQuiet(true); // Should be invalid in dry mode
    dryModeControl.setTurbo(true); // Should be invalid in dry mode

    List<String> errors =
        ModeFeatureValidator.validateFeatureRequest(
            "dry",
            java.util.Map.of(
                "fanspeed", "low",
                "quiet", true,
                "turbo", true),
            true);

    assertFalse(errors.isEmpty());
    assertTrue(errors.size() >= 3); // Should have at least 3 errors
    assertTrue(errors.stream().anyMatch(e -> e.contains("fan speed")));
    assertTrue(errors.stream().anyMatch(e -> e.contains("Quiet")));
    assertTrue(errors.stream().anyMatch(e -> e.contains("Turbo")));

    // Test Auto mode turbo restriction
    List<String> autoErrors =
        ModeFeatureValidator.validateFeatureRequest("auto", java.util.Map.of("turbo", true), true);

    assertFalse(autoErrors.isEmpty());
    assertTrue(autoErrors.get(0).contains("Turbo"));
    assertTrue(autoErrors.get(0).contains("auto"));

    // Test valid combination in Cool mode
    List<String> coolErrors =
        ModeFeatureValidator.validateFeatureRequest(
            "cool",
            java.util.Map.of(
                "fanspeed", "high",
                "quiet", true,
                "turbo", true,
                "blow", true,
                "powersave", true),
            true);

    assertTrue(coolErrors.isEmpty(), "Cool mode should support all features");
  }

  @Test
  public void testHvacFeatureValidationException() {
    // Test single feature exception
    Set<String> availableModes = Set.of("cool", "dry");
    HvacFeatureValidationException singleException =
        new HvacFeatureValidationException("blow", "heat", availableModes);

    assertEquals("heat", singleException.getMode());
    assertEquals("blow", singleException.getFeature());
    assertEquals(availableModes, singleException.getAvailableModes());
    assertFalse(singleException.hasMultipleErrors());
    assertEquals(1, singleException.getAllErrors().size());
    assertTrue(singleException.getMessage().contains("blow"));
    assertTrue(singleException.getMessage().contains("heat"));

    // Test multiple errors exception
    List<String> errors =
        List.of(
            "Feature 'turbo' is not available in mode 'dry'",
            "Feature 'quiet' is not available in mode 'dry'");
    HvacFeatureValidationException multipleException =
        new HvacFeatureValidationException("dry", errors);

    assertEquals("dry", multipleException.getMode());
    assertNull(multipleException.getFeature());
    assertNull(multipleException.getAvailableModes());
    assertTrue(multipleException.hasMultipleErrors());
    assertEquals(2, multipleException.getAllErrors().size());
    assertTrue(multipleException.getMessage().contains("Multiple feature validation errors"));
  }

  @Test
  public void testFeatureMatrixGeneration() {
    String matrix = ModeFeatureValidator.getFeatureMatrix();

    assertNotNull(matrix);
    assertTrue(matrix.contains("TABLE 1: FEATURES"));
    assertTrue(matrix.contains("TABLE 2: WIND SETTINGS"));
    assertTrue(matrix.contains("Dry mode has the most restrictions"));
    assertTrue(matrix.contains("blow"));
    assertTrue(matrix.contains("turbo"));
    assertTrue(matrix.contains("fanspeed"));
    assertTrue(matrix.contains("✓"));
    assertTrue(matrix.contains("✗"));

    // Verify some specific content
    assertTrue(matrix.contains("Cool") && matrix.contains("Heat") && matrix.contains("Dry"));
  }

  @Test
  public void testWindSettingHelperMethods() {
    // Test getAvailableModesForWindSetting
    Set<String> turboModes = ModeFeatureValidator.getAvailableModesForWindSetting("turbo");
    assertEquals(Set.of("cool", "heat"), turboModes);

    Set<String> fanspeedModes = ModeFeatureValidator.getAvailableModesForWindSetting("fanspeed");
    assertEquals(Set.of("auto", "cool", "heat", "fan"), fanspeedModes);

    Set<String> unknownModes = ModeFeatureValidator.getAvailableModesForWindSetting("unknown");
    assertTrue(unknownModes.isEmpty());

    Set<String> nullModes = ModeFeatureValidator.getAvailableModesForWindSetting(null);
    assertTrue(nullModes.isEmpty());
  }

  @Test
  public void testBackwardCompatibility() {
    // Unknown features should return true (backward compatibility)
    assertTrue(ModeFeatureValidator.isFeatureAvailable("unknownFeature", "cool"));
    assertTrue(ModeFeatureValidator.isWindSettingAvailable("unknownWindSetting", "cool"));

    // Null inputs should return false
    assertFalse(ModeFeatureValidator.isFeatureAvailable(null, "cool"));
    assertFalse(ModeFeatureValidator.isFeatureAvailable("blow", null));
    assertFalse(ModeFeatureValidator.isWindSettingAvailable(null, "cool"));
    assertFalse(ModeFeatureValidator.isWindSettingAvailable("turbo", null));
  }

  @Test
  public void testCaseInsensitivity() {
    // Test case insensitive mode and feature names
    assertTrue(ModeFeatureValidator.isFeatureAvailable("BLOW", "COOL"));
    assertTrue(ModeFeatureValidator.isFeatureAvailable("Health", "Heat"));
    assertTrue(ModeFeatureValidator.isWindSettingAvailable("TURBO", "COOL"));
    assertFalse(ModeFeatureValidator.isWindSettingAvailable("Turbo", "Auto"));
  }

  @Test
  public void testValidationWithWindSettingsDisabled() {
    // Test validation when wind settings validation is disabled
    List<String> errors =
        ModeFeatureValidator.validateFeatureRequest(
            "dry",
            java.util.Map.of(
                "fanspeed", "low",
                "quiet", true,
                "turbo", true,
                "blow", true // This should still be validated as it's a feature, not wind setting
                ),
            false); // Wind settings validation disabled

    // Should have no errors because blow IS available in dry mode and wind settings validation is
    // disabled
    assertEquals(
        0,
        errors.size(),
        "No errors expected when wind settings validation is disabled and blow is valid in dry mode");
  }
}
