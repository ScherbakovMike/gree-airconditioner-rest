package com.gree.hvac.discovery;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public interface SocketService {
  DatagramSocket createSocket() throws Exception;

  void sendPacket(DatagramSocket socket, byte[] data, InetAddress address, int port)
      throws Exception;

  DatagramPacket receivePacket(DatagramSocket socket, byte[] buffer) throws Exception;
}
