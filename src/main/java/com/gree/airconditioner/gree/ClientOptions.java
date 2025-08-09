package com.gree.airconditioner.gree;

/**
 * Client configuration options
 */
public class ClientOptions {
    private String host = "192.168.1.255";
    private int port = 7000;
    private int connectTimeout = 3000;
    private boolean autoConnect = true;
    private boolean poll = true;
    private int pollingInterval = 3000;
    private int pollingTimeout = 1000;
    private String logLevel = "error";
    private boolean debug = false;

    public ClientOptions() {}

    public ClientOptions(String host) {
        this.host = host;
    }

    // Getters and setters
    public String getHost() {
        return host;
    }

    public ClientOptions setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public ClientOptions setPort(int port) {
        this.port = port;
        return this;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public ClientOptions setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public boolean isAutoConnect() {
        return autoConnect;
    }

    public ClientOptions setAutoConnect(boolean autoConnect) {
        this.autoConnect = autoConnect;
        return this;
    }

    public boolean isPoll() {
        return poll;
    }

    public ClientOptions setPoll(boolean poll) {
        this.poll = poll;
        return this;
    }

    public int getPollingInterval() {
        return pollingInterval;
    }

    public ClientOptions setPollingInterval(int pollingInterval) {
        this.pollingInterval = pollingInterval;
        return this;
    }

    public int getPollingTimeout() {
        return pollingTimeout;
    }

    public ClientOptions setPollingTimeout(int pollingTimeout) {
        this.pollingTimeout = pollingTimeout;
        return this;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public ClientOptions setLogLevel(String logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    public boolean isDebug() {
        return debug;
    }

    public ClientOptions setDebug(boolean debug) {
        this.debug = debug;
        return this;
    }
}