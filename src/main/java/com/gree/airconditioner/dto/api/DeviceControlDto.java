package com.gree.airconditioner.dto.api;

import lombok.Data;

@Data
public class DeviceControlDto {
    private Boolean power;
    private Integer temperature;
    private String mode; // AUTO, COOL, HEAT, DRY, FAN_ONLY
    private String fanSpeed; // AUTO, LOW, MEDIUM, HIGH
    private String swingHorizontal; // DEFAULT, FULL, FIXED_LEFT, FIXED_RIGHT
    private String swingVertical; // DEFAULT, FULL, FIXED_TOP, FIXED_BOTTOM
    private Boolean lights;
    private Boolean turbo;
    private Boolean quiet;
    private Boolean health;
    private Boolean powerSave;
    private Boolean sleep;
}