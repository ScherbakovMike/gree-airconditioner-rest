package com.gree.hvac.discovery;

import static org.junit.jupiter.api.Assertions.*;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultNetworkServiceTest {

  private DefaultNetworkService networkService;

  @BeforeEach
  void setUp() {
    networkService = new DefaultNetworkService();
  }

  @Test
  void testGetNetworkInterfaces() throws Exception {
    // Act
    Enumeration<NetworkInterface> interfaces = networkService.getNetworkInterfaces();

    // Assert
    assertNotNull(interfaces);
  }

  @Test
  void testGetByName() throws Exception {
    // Act
    InetAddress address = networkService.getByName("localhost");

    // Assert
    assertNotNull(address);
    assertTrue(address.isLoopbackAddress());
  }

  @Test
  void testGetByNameThrowsExceptionForInvalidHost() {
    // Act & Assert
    assertThrows(
        Exception.class, () -> networkService.getByName("invalid.host.that.does.not.exist"));
  }
}
