package com.gree.airconditioner.dto;

import com.gree.airconditioner.DeviceInfo;
import com.gree.airconditioner.binding.GreeDeviceBinding;
import com.gree.airconditioner.dto.packs.BindRequestPack;
import com.gree.airconditioner.dto.packs.ControlRequestPack;
import com.gree.airconditioner.dto.packs.StatusRequestPack;
import com.gree.airconditioner.dto.status.GreeDeviceStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommandBuilder {

  public static CommandBuilder builder() {
    return new CommandBuilder();
  }

  public Command buildScanCommand() {
    Command command = new Command();
    command.setCommandType(CommandType.SCAN);
    return command;
  }

  public Command buildBindCommand(DeviceInfo info) {
    Command command = new Command();
    command.setCommandType(CommandType.PACK);
    command.setCid("app");
    command.setI(1);
    command.setTcid(info.getMacAddress());
    command.setUid(1l);

    String packEncrypted = BindRequestPack.build(info);
    command.setPack(packEncrypted);

    return command;
  }

  public Command buildControlCommand(GreeDeviceStatus status, GreeDeviceBinding binding) {
    DeviceInfo info = binding.getDevice().getDeviceInfo();

    Command command = new Command();
    command.setCommandType(CommandType.PACK);
    command.setCid("app");
    command.setI(0);
    command.setTcid(info.getMacAddress());
    command.setUid(0l);

    String packEncrypted = ControlRequestPack.build(status, binding);
    command.setPack(packEncrypted);

    return command;
  }

  public Command buildStatusCommand(GreeDeviceBinding binding) {
    DeviceInfo info = binding.getDevice().getDeviceInfo();

    Command command = new Command();
    command.setCommandType(CommandType.PACK);
    command.setCid("app");
    command.setI(0);
    command.setTcid(info.getMacAddress());
    command.setUid(0l);

    String packEncrypted = StatusRequestPack.build(info, binding);
    command.setPack(packEncrypted);

    return command;
  }
}
