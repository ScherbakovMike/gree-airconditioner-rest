package com.gree.airconditioner.util;

import java.net.DatagramSocket;
import java.net.SocketException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GreeAirconditionerHelper {

  private static DatagramSocket clientSocket = null;

  private GreeAirconditionerHelper() {}

  public static DatagramSocket getDatagramSocket() {
    if (clientSocket == null) {
      try {
        clientSocket = new DatagramSocket();
        clientSocket.setSoTimeout(60000);
      } catch (SocketException e) {
        log.error("Can't create a datagram socket", e);
        throw new RuntimeException("Can't create a datagram socket");
      }
    }
    return clientSocket;
  }
}
