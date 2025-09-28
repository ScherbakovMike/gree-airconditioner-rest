package com.gree.hvac.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

/** Real implementation of NetworkService using DatagramSocket */
@Slf4j
public class NetworkServiceImpl implements NetworkService {

  @Override
  public NetworkSocket createSocket(int port) throws SocketException {
    try {
      DatagramSocket socket = new DatagramSocket(port);
      socket.setBroadcast(true);
      return new DatagramSocketWrapper(socket);
    } catch (Exception e) {
      throw new NetworkSocketException("Failed to create socket on port " + port, e);
    }
  }

  @Override
  public InetAddress resolveAddress(String hostname) throws UnknownHostException {
    return InetAddress.getByName(hostname);
  }

  @Override
  public void startListening(NetworkSocket socket, Consumer<byte[]> messageHandler) {
    CompletableFuture.runAsync(
        () -> {
          DatagramSocket datagramSocket = (DatagramSocket) socket.getUnderlyingSocket();
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
      throws IOException {
    DatagramSocket datagramSocket = (DatagramSocket) socket.getUnderlyingSocket();
    DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
    datagramSocket.send(packet);
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
