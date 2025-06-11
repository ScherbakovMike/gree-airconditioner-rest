package com.gree.airconditioner.services;

import com.gree.airconditioner.GreeAirconditionerDevice;
import com.gree.airconditioner.binding.GreeDeviceBinderService;
import com.gree.airconditioner.binding.GreeDeviceBinding;
import com.gree.airconditioner.communication.GreeCommunicationService;
import com.gree.airconditioner.dto.Command;
import com.gree.airconditioner.dto.CommandBuilder;
import com.gree.airconditioner.dto.packs.StatusResponsePack;
import com.gree.airconditioner.dto.status.GreeDeviceStatus;
import com.gree.airconditioner.dto.status.Switch;
import com.gree.airconditioner.dto.status.Temperature;
import com.gree.airconditioner.dto.status.TemperatureUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GreeAirconditionerService {

  private final GreeDeviceBinderService binderService;
  private final GreeCommunicationService communicationService;
  private List<GreeAirconditionerDevice> devices;

  public GreeAirconditionerService(
      GreeDeviceBinderService binderService, GreeCommunicationService communicationService) {
    this.binderService = binderService;
    this.communicationService = communicationService;
  }

  public List<GreeAirconditionerDevice> getDevices() {
    return devices;
  }

  public void discoverDevices() {
    List<GreeAirconditionerDevice> devices = new ArrayList<>();
    while (devices.size() == 0) {
      devices = GreeAirconditionerDeviceFinder.findDevices();
    }
    this.devices = devices;
  }

  public boolean turnOn(GreeAirconditionerDevice device) {
    log.info("Turning on the air conditioner");
    GreeDeviceBinding binding = binderService.getBiding(device);

    GreeDeviceStatus status = new GreeDeviceStatus();
    status.setPower(Switch.ON);

    Command command = CommandBuilder.builder().buildControlCommand(status, binding);
    String result = communicationService.sendCommand(devices.get(0), command, Function.identity());
    return true;
  }

  public boolean turnOff(GreeAirconditionerDevice device) {
    log.info("Turning off the air conditioner");
    GreeDeviceBinding binding = binderService.getBiding(device);

    GreeDeviceStatus status = new GreeDeviceStatus();
    status.setPower(Switch.OFF);

    Command command = CommandBuilder.builder().buildControlCommand(status, binding);
    String result = communicationService.sendCommand(devices.get(0), command, Function.identity());
    return true;
  }

  public boolean setTemperature(GreeAirconditionerDevice device, Integer temperature) {
    log.info("Setting the temperature to {}", temperature);
    GreeDeviceBinding binding = binderService.getBiding(device);

    GreeDeviceStatus status = new GreeDeviceStatus();
    status.setTemperature(new Temperature(temperature, TemperatureUnit.CELSIUS));

    Command command = CommandBuilder.builder().buildControlCommand(status, binding);
    String result = communicationService.sendCommand(devices.get(0), command, Function.identity());
    return true;
  }

  public GreeDeviceStatus getStatus(GreeAirconditionerDevice device) {
    log.info("Getting status of device");
    GreeDeviceBinding binding = binderService.getBiding(device);

    Command command = CommandBuilder.builder().buildStatusCommand(binding);
    GreeDeviceStatus result =
        communicationService.sendCommand(
            devices.get(0), command, (json) -> StatusResponsePack.build(json, binding).toObject());
    return result;
  }
}
