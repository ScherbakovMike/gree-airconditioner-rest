package com.gree.hvac.client;

/** HVAC client configuration options */
public class HvacClientOptions {
  private String host;
  private int port = 7000;
  private int connectTimeout = 3000;
  private boolean autoConnect = true;
  private boolean poll = true;
  private int pollingInterval = 3000;
  private int pollingTimeout = 1000;
  private String logLevel = "error";
  private boolean debug = false;

  public HvacClientOptions() {}

  public HvacClientOptions(String host) {
    this.host = host;
  }

  // Getters and setters
  public String getHost() {
    return host;
  }

  public HvacClientOptions setHost(String host) {
    this.host = host;
    return this;
  }

  public int getPort() {
    return port;
  }

  public HvacClientOptions setPort(int port) {
    this.port = port;
    return this;
  }

  public int getConnectTimeout() {
    return connectTimeout;
  }

  public HvacClientOptions setConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
    return this;
  }

  public boolean isAutoConnect() {
    return autoConnect;
  }

  public HvacClientOptions setAutoConnect(boolean autoConnect) {
    this.autoConnect = autoConnect;
    return this;
  }

  public boolean isPoll() {
    return poll;
  }

  public HvacClientOptions setPoll(boolean poll) {
    this.poll = poll;
    return this;
  }

  public int getPollingInterval() {
    return pollingInterval;
  }

  public HvacClientOptions setPollingInterval(int pollingInterval) {
    this.pollingInterval = pollingInterval;
    return this;
  }

  public int getPollingTimeout() {
    return pollingTimeout;
  }

  public HvacClientOptions setPollingTimeout(int pollingTimeout) {
    this.pollingTimeout = pollingTimeout;
    return this;
  }

  public String getLogLevel() {
    return logLevel;
  }

  public HvacClientOptions setLogLevel(String logLevel) {
    this.logLevel = logLevel;
    return this;
  }

  public boolean isDebug() {
    return debug;
  }

  public HvacClientOptions setDebug(boolean debug) {
    this.debug = debug;
    return this;
  }
}
