package com.gree.hvac.network;

import java.net.DatagramSocket;

/** Wrapper for DatagramSocket to implement NetworkSocket interface */
public class DatagramSocketWrapper implements NetworkSocket {

  private final DatagramSocket socket;

  public DatagramSocketWrapper(DatagramSocket socket) {
    this.socket = socket;
  }

  @Override
  public boolean isClosed() {
    return socket.isClosed();
  }

  @Override
  public void close() {
    socket.close();
  }

  @Override
  public Object getUnderlyingSocket() {
    return socket;
  }

  public DatagramSocket getDatagramSocket() {
    return socket;
  }
}
