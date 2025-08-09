package com.gree.airconditioner.binding;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gree.airconditioner.DeviceInfo;
import com.gree.airconditioner.GreeAirconditionerDevice;
import com.gree.airconditioner.binding.exception.BindingUnsuccessfulException;
import com.gree.airconditioner.communication.GreeCommunicationService;
import com.gree.airconditioner.dto.Command;
import com.gree.airconditioner.dto.CommandBuilder;
import com.gree.airconditioner.dto.CommandResponse;
import com.gree.airconditioner.dto.CommandType;
import com.gree.airconditioner.dto.packs.BindResponsePack;
import com.gree.airconditioner.util.CryptoUtil;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GreeDeviceBinderService {
  private final GreeCommunicationService communicationService;

  private Map<GreeAirconditionerDevice, GreeDeviceBinding> bindings = new HashMap<>();

  public GreeDeviceBinderService(GreeCommunicationService communicationService) {
    this.communicationService = communicationService;
  }

  public GreeDeviceBinding getBiding(GreeAirconditionerDevice device) {
    log.info("Attempting to bind with device: {}, MAC: {}", 
        device.getConnectionInfo().getAddress(), 
        device.getDeviceInfo().getMacAddress());
    
    GreeDeviceBinding greeDeviceBinding = bindings.get(device);
    if (greeDeviceBinding != null) {
      long bindingCreationTime = greeDeviceBinding.getCreationDate().getTime();
      long nowTime = GregorianCalendar.getInstance().getTime().getTime();
      if (nowTime - bindingCreationTime < TimeUnit.MINUTES.toMillis(2)) {
        log.info("Using existing binding for device: {}", device.getConnectionInfo().getAddress());
        return greeDeviceBinding;
      } else {
        log.info("Existing binding expired for device: {}, creating new binding", device.getConnectionInfo().getAddress());
      }
    }
    
    // Implement proper handshake: scan then bind in same session
    GreeDeviceBinding binding = performHandshakeAndBind(device);
    bindings.put(device, binding);

    return binding;
  }
  
  private GreeDeviceBinding performHandshakeAndBind(GreeAirconditionerDevice device) {
    log.info("Starting scan-bind handshake sequence for device: {}", device.getConnectionInfo().getAddress());
    
    Command scanCommand = Command.builder().buildScanCommand();
    DeviceInfo deviceInfo = device.getDeviceInfo();
    Command bindCommand = CommandBuilder.builder().buildBindCommand(deviceInfo);
    
    return communicationService.sendScanThenBind(device, scanCommand, bindCommand, scanResponse -> bindResponse -> {
      BindResponsePack responsePack = getBindingResponse(bindResponse);
      if (responsePack != null && responsePack.getT().equalsIgnoreCase(CommandType.BINDOK.getCode())) {
        log.info("Bind successful for device: {}", device.getConnectionInfo().getAddress());
        return new GreeDeviceBinding(device, responsePack.getKey());
      } else {
        throw new BindingUnsuccessfulException(device);
      }
    });
  }

  private GreeDeviceBinding sendBindCommand(GreeAirconditionerDevice device, Command bindCommand) {
    return communicationService.sendCommand(
            device,
            bindCommand,
            (responseString) -> {
              BindResponsePack responsePack = getBindingResponse(responseString);
              if (responsePack.getT().equalsIgnoreCase(CommandType.BINDOK.getCode())) {
                if (log.isDebugEnabled()) {
                  String ipAddress = device.getConnectionInfo().getAddress().getHostAddress();
                  log.debug("Bind with device {} successful", ipAddress);
                }
                return new GreeDeviceBinding(device, responsePack.getKey());
              } else {
                throw new BindingUnsuccessfulException(device);
              }
            });
  }

  private BindResponsePack getBindingResponse(String bindingResponseString) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      CommandResponse response = mapper.readValue(bindingResponseString, CommandResponse.class);
      String encryptedPack = response.getPack();
      String decryptedPack = CryptoUtil.decryptPack(encryptedPack);
      BindResponsePack responsePack = mapper.readValue(decryptedPack, BindResponsePack.class);
      return responsePack;
    } catch (IOException e) {
      log.error("Can't map binding response to command response", e);
    }
    return null;
  }
}
