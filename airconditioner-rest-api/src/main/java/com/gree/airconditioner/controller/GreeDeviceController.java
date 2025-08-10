package com.gree.airconditioner.controller;

import com.gree.airconditioner.dto.api.ApiResponse;
import com.gree.airconditioner.dto.api.DeviceControlDto;
import com.gree.airconditioner.dto.api.DeviceInfoDto;
import com.gree.airconditioner.dto.api.DeviceStatusDto;
import com.gree.airconditioner.service.HvacDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "GREE Air Conditioner API",
    description = "REST API for managing GREE air conditioning devices")
public class GreeDeviceController {

  private final HvacDeviceService hvacDeviceService;

  @GetMapping("/discover")
  @Operation(
      summary = "Discover GREE devices",
      description = "Scan the network to discover available GREE air conditioning devices")
  public CompletableFuture<ResponseEntity<ApiResponse<List<DeviceInfoDto>>>> discoverDevices() {
    log.info("Starting device discovery...");
    return hvacDeviceService
        .discoverDevices()
        .thenApply(
            devices -> {
              log.info("Device discovery completed. Found {} devices", devices.size());
              return ResponseEntity.ok(
                  ApiResponse.success("Devices discovered successfully", devices));
            })
        .exceptionally(
            ex -> {
              log.error("Error during device discovery", ex);
              return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                  .body(
                      ApiResponse.<List<DeviceInfoDto>>error(
                          "Failed to discover devices: " + ex.getMessage()));
            });
  }

  @GetMapping
  @Operation(summary = "Get all devices", description = "Get a list of all discovered GREE devices")
  public ResponseEntity<ApiResponse<List<DeviceInfoDto>>> getAllDevices() {
    try {
      List<DeviceInfoDto> devices = hvacDeviceService.getDevices();
      log.info("Retrieved {} devices", devices.size());
      return ResponseEntity.ok(ApiResponse.success(devices));
    } catch (Exception e) {
      log.error("Error retrieving devices", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<List<DeviceInfoDto>>error(
                  "Failed to retrieve devices: " + e.getMessage()));
    }
  }

  @PostMapping("/{deviceId}/connect")
  @Operation(
      summary = "Connect to device",
      description = "Establish connection to a specific GREE device")
  @Parameter(
      name = "deviceId",
      description = "Unique identifier of the GREE device",
      required = true,
      in = ParameterIn.PATH,
      schema = @Schema(type = "string"))
  public CompletableFuture<ResponseEntity<ApiResponse<String>>> connectToDevice(
      @PathVariable String deviceId) {
    log.info("Connecting to device: {}", deviceId);
    return hvacDeviceService
        .connectToDevice(deviceId)
        .thenApply(
            success -> {
              if (Boolean.TRUE.equals(success)) {
                log.info("Successfully connected to device: {}", deviceId);
                return ResponseEntity.ok(
                    ApiResponse.success("Connected to device successfully", "Connected"));
              } else {
                log.error("Failed to connect to device: {}", deviceId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<String>error("Failed to connect to device"));
              }
            })
        .exceptionally(
            ex -> {
              log.error("Error connecting to device {}", deviceId, ex);
              return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                  .body(ApiResponse.<String>error("Connection failed: " + ex.getMessage()));
            });
  }

  @PostMapping("/{deviceId}/disconnect")
  @Operation(
      summary = "Disconnect from device",
      description = "Disconnect from a specific GREE device")
  @Parameter(
      name = "deviceId",
      description = "Unique identifier of the GREE device",
      required = true,
      in = ParameterIn.PATH,
      schema = @Schema(type = "string"))
  public CompletableFuture<ResponseEntity<ApiResponse<String>>> disconnectFromDevice(
      @PathVariable String deviceId) {
    log.info("Disconnecting from device: {}", deviceId);
    return hvacDeviceService
        .disconnectFromDevice(deviceId)
        .thenApply(
            success -> {
              if (Boolean.TRUE.equals(success)) {
                log.info("Successfully disconnected from device: {}", deviceId);
                return ResponseEntity.ok(
                    ApiResponse.success("Disconnected from device successfully", "Disconnected"));
              } else {
                log.error("Failed to disconnect from device: {}", deviceId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<String>error("Failed to disconnect from device"));
              }
            })
        .exceptionally(
            ex -> {
              log.error("Error disconnecting from device {}", deviceId, ex);
              return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                  .body(ApiResponse.<String>error("Disconnection failed: " + ex.getMessage()));
            });
  }

  @GetMapping("/{deviceId}/status")
  @Operation(
      summary = "Get device status",
      description = "Get current status and properties of a GREE device")
  @Parameter(
      name = "deviceId",
      description = "Unique identifier of the GREE device",
      required = true,
      in = ParameterIn.PATH,
      schema = @Schema(type = "string"))
  public CompletableFuture<ResponseEntity<ApiResponse<DeviceStatusDto>>> getDeviceStatus(
      @PathVariable String deviceId) {
    log.info("Getting status for device: {}", deviceId);
    return hvacDeviceService
        .getDeviceStatus(deviceId)
        .thenApply(
            status -> {
              log.info("Retrieved status for device: {}", deviceId);
              return ResponseEntity.ok(
                  ApiResponse.success("Device status retrieved successfully", status));
            })
        .exceptionally(
            ex -> {
              log.error("Error getting status for device {}", deviceId, ex);
              return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                  .body(
                      ApiResponse.<DeviceStatusDto>error(
                          "Failed to get device status: " + ex.getMessage()));
            });
  }

  @PostMapping("/{deviceId}/control")
  @Operation(summary = "Control device", description = "Send control commands to a GREE device")
  @Parameter(
      name = "deviceId",
      description = "Unique identifier of the GREE device",
      required = true,
      in = ParameterIn.PATH,
      schema = @Schema(type = "string"))
  public CompletableFuture<ResponseEntity<ApiResponse<String>>> controlDevice(
      @PathVariable String deviceId,
      @Parameter(
              description =
                  "Device control parameters including power, temperature, mode, fan speed, etc.")
          @RequestBody
          DeviceControlDto controlDto) {
    log.info("Controlling device {}: {}", deviceId, controlDto);
    return hvacDeviceService
        .controlDevice(deviceId, controlDto)
        .thenApply(
            success -> {
              if (Boolean.TRUE.equals(success)) {
                log.info("Successfully controlled device: {}", deviceId);
                return ResponseEntity.ok(
                    ApiResponse.success("Device controlled successfully", "Success"));
              } else {
                log.error("Failed to control device: {}", deviceId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<String>error("Failed to control device"));
              }
            })
        .exceptionally(
            ex -> {
              log.error("Error controlling device {}", deviceId, ex);
              return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                  .body(ApiResponse.<String>error("Device control failed: " + ex.getMessage()));
            });
  }

  // Convenience endpoints for common operations

  @PostMapping("/{deviceId}/power")
  @Operation(summary = "Toggle power", description = "Turn device on or off")
  @Parameter(
      name = "deviceId",
      description = "Unique identifier of the GREE device",
      required = true,
      in = ParameterIn.PATH,
      schema = @Schema(type = "string"))
  @Parameter(
      name = "on",
      description = "Power state - true to turn on, false to turn off",
      required = true,
      in = ParameterIn.QUERY,
      schema = @Schema(type = "boolean", example = "true"))
  public CompletableFuture<ResponseEntity<ApiResponse<String>>> togglePower(
      @PathVariable String deviceId, @RequestParam boolean on) {
    DeviceControlDto control = new DeviceControlDto();
    control.setPower(on);
    return controlDevice(deviceId, control);
  }

  @PostMapping("/{deviceId}/temperature")
  @Operation(summary = "Set temperature", description = "Set target temperature for the device")
  @Parameter(
      name = "deviceId",
      description = "Unique identifier of the GREE device",
      required = true,
      in = ParameterIn.PATH,
      schema = @Schema(type = "string"))
  @Parameter(
      name = "temperature",
      description = "Target temperature in Celsius (16-30)",
      required = true,
      in = ParameterIn.QUERY,
      schema = @Schema(type = "integer", example = "22", minimum = "16", maximum = "30"))
  public CompletableFuture<ResponseEntity<ApiResponse<String>>> setTemperature(
      @PathVariable String deviceId, @RequestParam int temperature) {
    if (temperature < 16 || temperature > 30) {
      return CompletableFuture.completedFuture(
          ResponseEntity.badRequest()
              .body(
                  ApiResponse.<String>error(
                      "Temperature must be between 16 and 30 degrees Celsius")));
    }

    DeviceControlDto control = new DeviceControlDto();
    control.setTemperature(temperature);
    return controlDevice(deviceId, control);
  }

  @PostMapping("/{deviceId}/mode")
  @Operation(
      summary = "Set mode",
      description = "Set operating mode (AUTO, COOL, HEAT, DRY, FAN_ONLY)")
  @Parameter(
      name = "deviceId",
      description = "Unique identifier of the GREE device",
      required = true,
      in = ParameterIn.PATH,
      schema = @Schema(type = "string"))
  @Parameter(
      name = "mode",
      description = "Operating mode",
      required = true,
      in = ParameterIn.QUERY,
      schema =
          @Schema(
              type = "string",
              example = "COOL",
              allowableValues = {"AUTO", "COOL", "HEAT", "DRY", "FAN_ONLY"}))
  public CompletableFuture<ResponseEntity<ApiResponse<String>>> setMode(
      @PathVariable String deviceId, @RequestParam String mode) {
    DeviceControlDto control = new DeviceControlDto();
    control.setMode(mode.toUpperCase());
    return controlDevice(deviceId, control);
  }

  @PostMapping("/{deviceId}/fanspeed")
  @Operation(summary = "Set fan speed", description = "Set fan speed (AUTO, LOW, MEDIUM, HIGH)")
  @Parameter(
      name = "deviceId",
      description = "Unique identifier of the GREE device",
      required = true,
      in = ParameterIn.PATH,
      schema = @Schema(type = "string"))
  @Parameter(
      name = "fanSpeed",
      description = "Fan speed level",
      required = true,
      in = ParameterIn.QUERY,
      schema =
          @Schema(
              type = "string",
              example = "AUTO",
              allowableValues = {"AUTO", "LOW", "MEDIUM", "HIGH"}))
  public CompletableFuture<ResponseEntity<ApiResponse<String>>> setFanSpeed(
      @PathVariable String deviceId, @RequestParam String fanSpeed) {
    DeviceControlDto control = new DeviceControlDto();
    control.setFanSpeed(fanSpeed.toUpperCase());
    return controlDevice(deviceId, control);
  }
}
