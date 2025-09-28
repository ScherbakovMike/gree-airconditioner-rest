package com.gree.hvac.client;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;

/** Unit tests for ModeFeatureValidator based on GREE mobile app feature matrix */
public class ModeFeatureValidatorTest {

  @Test
  public void testXFanAvailability() {
    // X-Fan should only be available in Cool and Dry modes
    assertTrue(ModeFeatureValidator.isFeatureAvailable("blow", "cool"));
    assertTrue(ModeFeatureValidator.isFeatureAvailable("blow", "dry"));

    assertFalse(ModeFeatureValidator.isFeatureAvailable("blow", "auto"));
    assertFalse(ModeFeatureValidator.isFeatureAvailable("blow", "heat"));
    assertFalse(ModeFeatureValidator.isFeatureAvailable("blow", "fan"));
  }

  @Test
  public void testHealthUVCAvailability() {
    // Health/UVC should be available in all modes
    assertTrue(ModeFeatureValidator.isFeatureAvailable("health", "auto"));
    assertTrue(ModeFeatureValidator.isFeatureAvailable("health", "cool"));
    assertTrue(ModeFeatureValidator.isFeatureAvailable("health", "heat"));
    assertTrue(ModeFeatureValidator.isFeatureAvailable("health", "fan"));
    assertTrue(ModeFeatureValidator.isFeatureAvailable("health", "dry"));

    // UVC alias should work the same
    assertTrue(ModeFeatureValidator.isFeatureAvailable("uvc", "cool"));
    assertTrue(ModeFeatureValidator.isFeatureAvailable("uvc", "heat"));
  }

  @Test
  public void testEnergySavingAvailability() {
    // Energy Saving (SE) should only be available in Cool mode
    assertTrue(ModeFeatureValidator.isFeatureAvailable("powerSave", "cool"));
    assertTrue(ModeFeatureValidator.isFeatureAvailable("energySaving", "cool"));

    assertFalse(ModeFeatureValidator.isFeatureAvailable("powerSave", "auto"));
    assertFalse(ModeFeatureValidator.isFeatureAvailable("powerSave", "heat"));
    assertFalse(ModeFeatureValidator.isFeatureAvailable("powerSave", "fan"));
    assertFalse(ModeFeatureValidator.isFeatureAvailable("powerSave", "dry"));

    assertFalse(ModeFeatureValidator.isFeatureAvailable("energySaving", "auto"));
    assertFalse(ModeFeatureValidator.isFeatureAvailable("energySaving", "heat"));
    assertFalse(ModeFeatureValidator.isFeatureAvailable("energySaving", "fan"));
    assertFalse(ModeFeatureValidator.isFeatureAvailable("energySaving", "dry"));
  }

  @Test
  public void testCaseInsensitivity() {
    assertTrue(ModeFeatureValidator.isFeatureAvailable("BLOW", "COOL"));
    assertTrue(ModeFeatureValidator.isFeatureAvailable("Health", "Heat"));
    assertTrue(ModeFeatureValidator.isFeatureAvailable("powerSave", "COOL"));
  }

  @Test
  public void testNullValues() {
    assertFalse(ModeFeatureValidator.isFeatureAvailable(null, "cool"));
    assertFalse(ModeFeatureValidator.isFeatureAvailable("blow", null));
    assertFalse(ModeFeatureValidator.isFeatureAvailable(null, null));
  }

  @Test
  public void testUnknownFeature() {
    // Unknown features should return true for backward compatibility
    assertTrue(ModeFeatureValidator.isFeatureAvailable("unknownFeature", "cool"));
  }

  @Test
  public void testGetAvailableModesForFeature() {
    Set<String> xFanModes = ModeFeatureValidator.getAvailableModesForFeature("blow");
    assertEquals(Set.of("cool", "dry"), xFanModes);

    Set<String> healthModes = ModeFeatureValidator.getAvailableModesForFeature("health");
    assertEquals(Set.of("auto", "cool", "heat", "fan", "dry"), healthModes);

    Set<String> energySavingModes = ModeFeatureValidator.getAvailableModesForFeature("powerSave");
    assertEquals(Set.of("cool"), energySavingModes);
  }

  @Test
  public void testGetAvailableFeaturesForMode() {
    Set<String> coolFeatures = ModeFeatureValidator.getAvailableFeaturesForMode("cool");
    assertTrue(coolFeatures.contains("blow"));
    assertTrue(coolFeatures.contains("health"));
    assertTrue(coolFeatures.contains("powersave"));
    assertTrue(coolFeatures.contains("uvc"));
    assertTrue(coolFeatures.contains("energysaving"));

    Set<String> heatFeatures = ModeFeatureValidator.getAvailableFeaturesForMode("heat");
    assertFalse(heatFeatures.contains("blow"));
    assertTrue(heatFeatures.contains("health"));
    assertFalse(heatFeatures.contains("powersave"));
  }

  @Test
  public void testValidateFeatureRequest() {
    Map<String, Object> features = new HashMap<>();

    // Valid: X-Fan in Cool mode
    features.put("blow", true);
    List<String> errors = ModeFeatureValidator.validateFeatureRequest("cool", features);
    assertTrue(errors.isEmpty());

    // Invalid: X-Fan in Heat mode
    errors = ModeFeatureValidator.validateFeatureRequest("heat", features);
    assertFalse(errors.isEmpty());
    assertTrue(errors.get(0).contains("blow"));
    assertTrue(errors.get(0).contains("heat"));

    // Valid: Health in any mode
    features.clear();
    features.put("health", true);
    errors = ModeFeatureValidator.validateFeatureRequest("auto", features);
    assertTrue(errors.isEmpty());

    // Invalid: Energy Saving in Heat mode
    features.clear();
    features.put("powerSave", true);
    errors = ModeFeatureValidator.validateFeatureRequest("heat", features);
    assertFalse(errors.isEmpty());

    // Valid: Multiple features in Cool mode
    features.clear();
    features.put("blow", true);
    features.put("health", true);
    features.put("powerSave", true);
    errors = ModeFeatureValidator.validateFeatureRequest("cool", features);
    assertTrue(errors.isEmpty());
  }

  @Test
  public void testValidateFeatureRequestIgnoresFalseValues() {
    Map<String, Object> features = new HashMap<>();

    // Should not validate features that are being turned OFF
    features.put("blow", false); // This should not trigger validation
    List<String> errors = ModeFeatureValidator.validateFeatureRequest("heat", features);
    assertTrue(errors.isEmpty());

    // Only features being turned ON should be validated
    features.put("blow", true);
    errors = ModeFeatureValidator.validateFeatureRequest("heat", features);
    assertFalse(errors.isEmpty());
  }

  @Test
  public void testFeatureMatrixGeneration() {
    String matrix = ModeFeatureValidator.getFeatureMatrix();

    assertNotNull(matrix);
    assertTrue(matrix.contains("Feature"));
    assertTrue(matrix.contains("Auto"));
    assertTrue(matrix.contains("Cool"));
    assertTrue(matrix.contains("Heat"));
    assertTrue(matrix.contains("blow"));
    assertTrue(matrix.contains("health"));
    assertTrue(matrix.contains("powersave"));
    assertTrue(matrix.contains("✓"));
    assertTrue(matrix.contains("✗"));
  }
}
