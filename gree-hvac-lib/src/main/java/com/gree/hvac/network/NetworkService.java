package com.gree.hvac.network;

import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/** Interface for abstracting network operations to enable testing and dependency injection */
public interface NetworkService {

  /**
   * Create and configure a UDP socket for communication
   *
   * @param port The port to bind to (0 for any available port)
   * @return A socket wrapper for network operations
   * @throws Exception if socket creation fails
   */
  NetworkSocket createSocket(int port) throws Exception;

  /**
   * Resolve hostname to InetAddress
   *
   * @param hostname The hostname to resolve
   * @return The resolved InetAddress
   * @throws Exception if resolution fails
   */
  InetAddress resolveAddress(String hostname) throws Exception;

  /**
   * Start listening for incoming packets on the socket
   *
   * @param socket The socket to listen on
   * @param messageHandler Handler for received messages
   * @return A CompletableFuture that completes when listening starts
   */
  CompletableFuture<Void> startListening(NetworkSocket socket, Consumer<byte[]> messageHandler);

  /**
   * Send data to a specific address and port
   *
   * @param socket The socket to send from
   * @param data The data to send
   * @param address The target address
   * @param port The target port
   * @throws Exception if sending fails
   */
  void sendData(NetworkSocket socket, byte[] data, InetAddress address, int port) throws Exception;

  /**
   * Check if the socket is closed
   *
   * @param socket The socket to check
   * @return true if socket is closed
   */
  boolean isClosed(NetworkSocket socket);

  /**
   * Close the socket and cleanup resources
   *
   * @param socket The socket to close
   */
  void closeSocket(NetworkSocket socket);
}
