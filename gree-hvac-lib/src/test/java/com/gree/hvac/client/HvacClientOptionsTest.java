package com.gree.hvac.client;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class HvacClientOptionsTest {

  @Test
  void testDefaultConstructor() {
    HvacClientOptions options = new HvacClientOptions();

    assertNull(options.getHost());
    assertEquals(7000, options.getPort());
    assertEquals(3000, options.getConnectTimeout());
    assertTrue(options.isAutoConnect());
    assertTrue(options.isPoll());
    assertEquals(3000, options.getPollingInterval());
    assertEquals(1000, options.getPollingTimeout());
    assertEquals("error", options.getLogLevel());
    assertFalse(options.isDebug());
  }

  @Test
  void testConstructorWithHost() {
    String host = "192.168.1.100";
    HvacClientOptions options = new HvacClientOptions(host);

    assertEquals(host, options.getHost());
    assertEquals(7000, options.getPort());
    assertEquals(3000, options.getConnectTimeout());
    assertTrue(options.isAutoConnect());
    assertTrue(options.isPoll());
    assertEquals(3000, options.getPollingInterval());
    assertEquals(1000, options.getPollingTimeout());
    assertEquals("error", options.getLogLevel());
    assertFalse(options.isDebug());
  }

  @Test
  void testSetHost() {
    HvacClientOptions options = new HvacClientOptions();
    String host = "192.168.1.100";

    HvacClientOptions result = options.setHost(host);

    assertEquals(host, options.getHost());
    assertSame(options, result); // Test builder pattern
  }

  @Test
  void testSetPort() {
    HvacClientOptions options = new HvacClientOptions();
    int port = 8080;

    HvacClientOptions result = options.setPort(port);

    assertEquals(port, options.getPort());
    assertSame(options, result); // Test builder pattern
  }

  @Test
  void testSetConnectTimeout() {
    HvacClientOptions options = new HvacClientOptions();
    int timeout = 5000;

    HvacClientOptions result = options.setConnectTimeout(timeout);

    assertEquals(timeout, options.getConnectTimeout());
    assertSame(options, result); // Test builder pattern
  }

  @Test
  void testSetAutoConnect() {
    HvacClientOptions options = new HvacClientOptions();
    boolean autoConnect = false;

    HvacClientOptions result = options.setAutoConnect(autoConnect);

    assertEquals(autoConnect, options.isAutoConnect());
    assertSame(options, result); // Test builder pattern
  }

  @Test
  void testSetPoll() {
    HvacClientOptions options = new HvacClientOptions();
    boolean poll = false;

    HvacClientOptions result = options.setPoll(poll);

    assertEquals(poll, options.isPoll());
    assertSame(options, result); // Test builder pattern
  }

  @Test
  void testSetPollingInterval() {
    HvacClientOptions options = new HvacClientOptions();
    int interval = 5000;

    HvacClientOptions result = options.setPollingInterval(interval);

    assertEquals(interval, options.getPollingInterval());
    assertSame(options, result); // Test builder pattern
  }

  @Test
  void testSetPollingTimeout() {
    HvacClientOptions options = new HvacClientOptions();
    int timeout = 2000;

    HvacClientOptions result = options.setPollingTimeout(timeout);

    assertEquals(timeout, options.getPollingTimeout());
    assertSame(options, result); // Test builder pattern
  }

  @Test
  void testSetLogLevel() {
    HvacClientOptions options = new HvacClientOptions();
    String logLevel = "debug";

    HvacClientOptions result = options.setLogLevel(logLevel);

    assertEquals(logLevel, options.getLogLevel());
    assertSame(options, result); // Test builder pattern
  }

  @Test
  void testSetDebug() {
    HvacClientOptions options = new HvacClientOptions();
    boolean debug = true;

    HvacClientOptions result = options.setDebug(debug);

    assertEquals(debug, options.isDebug());
    assertSame(options, result); // Test builder pattern
  }

  @Test
  void testBuilderPatternChaining() {
    HvacClientOptions options =
        new HvacClientOptions()
            .setHost("192.168.1.100")
            .setPort(8080)
            .setConnectTimeout(5000)
            .setAutoConnect(false)
            .setPoll(false)
            .setPollingInterval(10000)
            .setPollingTimeout(2000)
            .setLogLevel("info")
            .setDebug(true);

    assertEquals("192.168.1.100", options.getHost());
    assertEquals(8080, options.getPort());
    assertEquals(5000, options.getConnectTimeout());
    assertFalse(options.isAutoConnect());
    assertFalse(options.isPoll());
    assertEquals(10000, options.getPollingInterval());
    assertEquals(2000, options.getPollingTimeout());
    assertEquals("info", options.getLogLevel());
    assertTrue(options.isDebug());
  }

  @Test
  void testSetNullHost() {
    HvacClientOptions options = new HvacClientOptions("192.168.1.100");

    HvacClientOptions result = options.setHost(null);

    assertNull(options.getHost());
    assertSame(options, result);
  }

  @Test
  void testSetEmptyHost() {
    HvacClientOptions options = new HvacClientOptions();
    String emptyHost = "";

    HvacClientOptions result = options.setHost(emptyHost);

    assertEquals(emptyHost, options.getHost());
    assertSame(options, result);
  }

  @Test
  void testSetZeroPort() {
    HvacClientOptions options = new HvacClientOptions();

    HvacClientOptions result = options.setPort(0);

    assertEquals(0, options.getPort());
    assertSame(options, result);
  }

  @Test
  void testSetNegativePort() {
    HvacClientOptions options = new HvacClientOptions();

    HvacClientOptions result = options.setPort(-1);

    assertEquals(-1, options.getPort());
    assertSame(options, result);
  }

  @Test
  void testSetZeroTimeout() {
    HvacClientOptions options = new HvacClientOptions();

    HvacClientOptions result = options.setConnectTimeout(0);

    assertEquals(0, options.getConnectTimeout());
    assertSame(options, result);
  }

  @Test
  void testSetNegativeTimeout() {
    HvacClientOptions options = new HvacClientOptions();

    HvacClientOptions result = options.setConnectTimeout(-1000);

    assertEquals(-1000, options.getConnectTimeout());
    assertSame(options, result);
  }

  @Test
  void testSetZeroPollingInterval() {
    HvacClientOptions options = new HvacClientOptions();

    HvacClientOptions result = options.setPollingInterval(0);

    assertEquals(0, options.getPollingInterval());
    assertSame(options, result);
  }

  @Test
  void testSetNegativePollingInterval() {
    HvacClientOptions options = new HvacClientOptions();

    HvacClientOptions result = options.setPollingInterval(-5000);

    assertEquals(-5000, options.getPollingInterval());
    assertSame(options, result);
  }

  @Test
  void testSetZeroPollingTimeout() {
    HvacClientOptions options = new HvacClientOptions();

    HvacClientOptions result = options.setPollingTimeout(0);

    assertEquals(0, options.getPollingTimeout());
    assertSame(options, result);
  }

  @Test
  void testSetNegativePollingTimeout() {
    HvacClientOptions options = new HvacClientOptions();

    HvacClientOptions result = options.setPollingTimeout(-1000);

    assertEquals(-1000, options.getPollingTimeout());
    assertSame(options, result);
  }

  @Test
  void testSetNullLogLevel() {
    HvacClientOptions options = new HvacClientOptions();

    HvacClientOptions result = options.setLogLevel(null);

    assertNull(options.getLogLevel());
    assertSame(options, result);
  }

  @Test
  void testSetEmptyLogLevel() {
    HvacClientOptions options = new HvacClientOptions();
    String emptyLogLevel = "";

    HvacClientOptions result = options.setLogLevel(emptyLogLevel);

    assertEquals(emptyLogLevel, options.getLogLevel());
    assertSame(options, result);
  }
}
