package com.gree.airconditioner.communication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gree.airconditioner.ConnectionInfo;
import com.gree.airconditioner.GreeAirconditionerDevice;
import com.gree.airconditioner.dto.Command;
import com.gree.airconditioner.dto.CommandResponse;
import com.gree.airconditioner.util.GreeAirconditionerHelper;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GreeCommunicationService {

  public <T> T sendCommand(
      GreeAirconditionerDevice device, Command command, Function<String, T> function) {
    return sendCommand(device, command, function, 1);
  }

  private <T> T sendCommand(
      GreeAirconditionerDevice device, Command command, Function<String, T> function, int attempt) {
    String json = command.toJson();
    if (log.isDebugEnabled()) {
      log.debug("Sending command (attempt {}): {}", attempt, json);
    }

    ConnectionInfo connectionInfo = device.getConnectionInfo();
    InetAddress address = connectionInfo.getAddress();
    Integer port = connectionInfo.getPort();
    DatagramPacket datagram =
        new DatagramPacket(json.getBytes(), json.getBytes().length, address, port);

    // Use a fresh socket for each command like Node.js implementation
    DatagramSocket datagramSocket = null;
    try {
      datagramSocket = new DatagramSocket();
      datagramSocket.setSoTimeout(5000); // 5 second timeout
      
      log.info("Sending UDP packet to {}:{}", address.getHostAddress(), port);
      datagramSocket.send(datagram);
      log.info("Waiting for response from {}:{}", address.getHostAddress(), port);
      byte[] receiveData = new byte[500];
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
      datagramSocket.receive(receivePacket);
      String responseString =
          new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();
      log.info("Received response from {}:{}: {}", address.getHostAddress(), port, responseString);
      return function.apply(responseString);
    } catch (IOException e) {
      if (attempt < 2) {
        log.warn("Command failed (attempt {}), retrying: {}", attempt, command.toJson());
        return sendCommand(device, command, function, attempt + 1);
      } else {
        log.error("Command failed after {} attempts: {}", attempt, command.toJson(), e);
        throw new RuntimeException("Failed to send command after " + attempt + " attempts", e);
      }
    } finally {
      if (datagramSocket != null && !datagramSocket.isClosed()) {
        datagramSocket.close();
      }
    }
  }
  
  // Special method for scan-bind handshake using same socket
  public <T> T sendScanThenBind(GreeAirconditionerDevice device, Command scanCommand, Command bindCommand, Function<String, Function<String, T>> processor) {
    ConnectionInfo connectionInfo = device.getConnectionInfo();
    InetAddress address = connectionInfo.getAddress();
    Integer port = connectionInfo.getPort();
    
    DatagramSocket datagramSocket = null;
    try {
      datagramSocket = new DatagramSocket();
      datagramSocket.setSoTimeout(5000);
      
      // Step 1: Send scan command
      String scanJson = scanCommand.toJson();
      DatagramPacket scanPacket = new DatagramPacket(scanJson.getBytes(), scanJson.getBytes().length, address, port);
      log.info("Sending scan command to {}:{}", address.getHostAddress(), port);
      datagramSocket.send(scanPacket);
      
      // Receive scan response
      byte[] receiveData = new byte[500];
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
      datagramSocket.receive(receivePacket);
      String scanResponse = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();
      log.info("Received scan response: {}", scanResponse);
      
      // Parse scan response to extract device info (this might reveal why bind fails)
      try {
        ObjectMapper mapper = new ObjectMapper();
        CommandResponse scanCommandResponse = mapper.readValue(scanResponse, CommandResponse.class);
        log.info("Scan response pack: {}", scanCommandResponse.getPack());
        log.info("Scan response cid: {}", scanCommandResponse.getCid());
        log.info("Scan response tcid: {}", scanCommandResponse.getTcid());
      } catch (Exception e) {
        log.warn("Could not parse scan response", e);
      }
      
      // Step 2: Send bind command using same socket (add small delay like Node.js)
      Thread.sleep(500); // 500ms delay like Node.js implementation
      String bindJson = bindCommand.toJson();
      DatagramPacket bindPacket = new DatagramPacket(bindJson.getBytes(), bindJson.getBytes().length, address, port);
      log.info("Sending bind command to {}:{} using same socket: {}", address.getHostAddress(), port, bindJson);
      datagramSocket.send(bindPacket);
      
      // Receive bind response
      receiveData = new byte[500];
      receivePacket = new DatagramPacket(receiveData, receiveData.length);
      datagramSocket.receive(receivePacket);
      String bindResponse = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();
      log.info("Received bind response: {}", bindResponse);
      
      return processor.apply(scanResponse).apply(bindResponse);
      
    } catch (IOException | InterruptedException e) {
      log.error("Scan-bind handshake failed", e);
      throw new RuntimeException("Scan-bind handshake failed", e);
    } finally {
      if (datagramSocket != null && !datagramSocket.isClosed()) {
        datagramSocket.close();
      }
    }
  }
}
