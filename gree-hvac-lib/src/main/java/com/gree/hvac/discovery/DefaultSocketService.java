package com.gree.hvac.discovery;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class DefaultSocketService implements SocketService {

  @Override
  public DatagramSocket createSocket() throws Exception {
    return new DatagramSocket();
  }

  @Override
  public void sendPacket(DatagramSocket socket, byte[] data, InetAddress address, int port)
      throws Exception {
    DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
    socket.send(packet);
  }

  @Override
  public DatagramPacket receivePacket(DatagramSocket socket, byte[] buffer) throws Exception {
    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
    socket.receive(packet);
    return packet;
  }
}
