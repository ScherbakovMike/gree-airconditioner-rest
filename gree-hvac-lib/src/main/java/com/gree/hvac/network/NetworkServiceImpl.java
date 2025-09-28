package com.gree.hvac.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

/** Real implementation of NetworkService using DatagramSocket */
@Slf4j
public class NetworkServiceImpl implements NetworkService {

  @Override
  public NetworkSocket createSocket(int port) throws Exception {
    try {
      DatagramSocket socket = new DatagramSocket(port);
      socket.setBroadcast(true);
      return new DatagramSocketWrapper(socket);
    } catch (Exception e) {
      throw new NetworkSocketException("Failed to create socket on port " + port, e);
    }
  }

  @Override
  public InetAddress resolveAddress(String hostname) throws Exception {
    try {
      return InetAddress.getByName(hostname);
    } catch (Exception e) {
      throw new NetworkAddressException("Failed to resolve hostname: " + hostname, e);
    }
  }

  @Override
  public CompletableFuture<Void> startListening(
      NetworkSocket socket, Consumer<byte[]> messageHandler) {
    return CompletableFuture.runAsync(
        () -> {
          DatagramSocket datagramSocket =
              (DatagramSocket) ((DatagramSocketWrapper) socket).getUnderlyingSocket();
          byte[] buffer = new byte[1024];
          DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

          while (!socket.isClosed()) {
            try {
              datagramSocket.receive(packet);
              byte[] data = Arrays.copyOf(packet.getData(), packet.getLength());
              messageHandler.accept(data);
            } catch (Exception e) {
              if (!socket.isClosed()) {
                log.error("Error receiving data", e);
                throw new NetworkSocketException(e.getMessage(), e);
              }
            }
          }
        });
  }

  @Override
  public void sendData(NetworkSocket socket, byte[] data, InetAddress address, int port)
      throws Exception {
    try {
      DatagramSocket datagramSocket =
          (DatagramSocket) ((DatagramSocketWrapper) socket).getUnderlyingSocket();
      DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
      datagramSocket.send(packet);
    } catch (Exception e) {
      throw new NetworkTransmissionException("Failed to send data to " + address + ":" + port, e);
    }
  }

  @Override
  public boolean isClosed(NetworkSocket socket) {
    return socket.isClosed();
  }

  @Override
  public void closeSocket(NetworkSocket socket) {
    socket.close();
  }
}
