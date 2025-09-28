package com.gree.hvac.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public interface SocketService {
  DatagramSocket createSocket() throws SocketException;

  void sendPacket(DatagramSocket socket, byte[] data, InetAddress address, int port)
      throws IOException;

  DatagramPacket receivePacket(DatagramSocket socket, byte[] buffer) throws IOException;
}
