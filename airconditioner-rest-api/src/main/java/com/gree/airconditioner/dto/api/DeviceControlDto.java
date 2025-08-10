package com.gree.airconditioner.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Device control parameters")
public class DeviceControlDto {

  @Schema(description = "Power state", example = "true")
  private Boolean power;

  @Schema(
      description = "Target temperature in Celsius (16-30)",
      example = "22",
      minimum = "16",
      maximum = "30")
  private Integer temperature;

  @Schema(
      description = "Operation mode",
      example = "COOL",
      allowableValues = {"AUTO", "COOL", "HEAT", "DRY", "FAN_ONLY"})
  private String mode;

  @Schema(
      description = "Fan speed",
      example = "AUTO",
      allowableValues = {"AUTO", "LOW", "MEDIUM", "HIGH"})
  private String fanSpeed;

  @Schema(
      description = "Horizontal swing",
      example = "DEFAULT",
      allowableValues = {"DEFAULT", "FULL", "FIXED_LEFT", "FIXED_RIGHT"})
  private String swingHorizontal;

  @Schema(
      description = "Vertical swing",
      example = "DEFAULT",
      allowableValues = {"DEFAULT", "FULL", "FIXED_TOP", "FIXED_BOTTOM"})
  private String swingVertical;

  @Schema(description = "Display lights", example = "true")
  private Boolean lights;

  @Schema(description = "Turbo mode", example = "false")
  private Boolean turbo;

  @Schema(description = "Quiet mode", example = "false")
  private Boolean quiet;

  @Schema(description = "Health mode", example = "false")
  private Boolean health;

  @Schema(description = "Power save mode", example = "false")
  private Boolean powerSave;

  @Schema(description = "Sleep mode", example = "false")
  private Boolean sleep;
}
