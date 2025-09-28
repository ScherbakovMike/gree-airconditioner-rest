package com.gree.hvac.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class DefaultSocketService implements SocketService {

  @Override
  public DatagramSocket createSocket() throws SocketException {
    return new DatagramSocket();
  }

  @Override
  public void sendPacket(DatagramSocket socket, byte[] data, InetAddress address, int port)
      throws IOException {
    DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
    socket.send(packet);
  }

  @Override
  public DatagramPacket receivePacket(DatagramSocket socket, byte[] buffer) throws IOException {
    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
    socket.receive(packet);
    return packet;
  }
}
