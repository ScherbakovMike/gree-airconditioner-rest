package com.gree.hvac.discovery;

import static org.junit.jupiter.api.Assertions.*;

import java.net.DatagramSocket;
import java.net.InetAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultSocketServiceTest {

  private DefaultSocketService socketService;

  @BeforeEach
  void setUp() {
    socketService = new DefaultSocketService();
  }

  @Test
  void testCreateSocket() throws Exception {
    // Act
    DatagramSocket socket = socketService.createSocket();

    // Assert
    assertNotNull(socket);
    socket.close();
  }

  @Test
  void testSendPacket() throws Exception {
    // Arrange
    DatagramSocket socket = new DatagramSocket();
    byte[] data = "test".getBytes();
    InetAddress address = InetAddress.getByName("localhost");
    int port = 12345;

    // Act & Assert - Should not throw exception
    assertDoesNotThrow(() -> socketService.sendPacket(socket, data, address, port));

    socket.close();
  }

  @Test
  void testReceivePacket() throws Exception {
    // Arrange
    DatagramSocket socket = new DatagramSocket();
    socket.setSoTimeout(100); // Short timeout for test
    byte[] buffer = new byte[1024];

    // Act & Assert - Should throw SocketTimeoutException due to no data
    assertThrows(Exception.class, () -> socketService.receivePacket(socket, buffer));

    socket.close();
  }
}
