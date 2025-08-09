package com.gree.airconditioner.util;

import java.net.DatagramSocket;
import java.net.SocketException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GreeAirconditionerHelper {

  @Value("${gree.airconditioner.socket-timeout}")
  private int socketTimeout;

  private static int staticSocketTimeout = 5000; // default fallback
  private static DatagramSocket clientSocket = null;

  @PostConstruct
  private void init() {
    staticSocketTimeout = socketTimeout;
  }

  public static DatagramSocket getDatagramSocket() {
    if (clientSocket == null) {
      try {
        clientSocket = new DatagramSocket();
        clientSocket.setSoTimeout(staticSocketTimeout);
        log.debug("Created DatagramSocket with timeout: {}ms", staticSocketTimeout);
      } catch (SocketException e) {
        log.error("Can't create a datagram socket", e);
        throw new RuntimeException("Can't create a datagram socket");
      }
    }
    return clientSocket;
  }
}
