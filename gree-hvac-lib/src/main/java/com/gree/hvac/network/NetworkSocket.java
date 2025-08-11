package com.gree.hvac.network;

/** Interface representing a network socket for abstraction purposes */
public interface NetworkSocket {

  /**
   * Check if the socket is closed
   *
   * @return true if socket is closed
   */
  boolean isClosed();

  /** Close the socket */
  void close();

  /**
   * Get the underlying socket implementation (for implementation-specific operations)
   *
   * @return the underlying socket object
   */
  Object getUnderlyingSocket();
}
