package com.gree.hvac.network;

import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/** Mock implementation of NetworkService for testing */
public class MockNetworkService implements NetworkService {

  private final ConcurrentHashMap<MockNetworkSocket, Consumer<byte[]>> messageHandlers =
      new ConcurrentHashMap<>();
  private volatile boolean simulateConnectionFailure = false;
  private volatile boolean simulateBindFailure = false;
  private volatile InetAddress mockAddress;

  public MockNetworkService() {
    try {
      this.mockAddress = InetAddress.getByName("127.0.0.1");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public NetworkSocket createSocket(int port) throws Exception {
    if (simulateConnectionFailure) {
      throw new Exception("Simulated connection failure");
    }
    return new MockNetworkSocket();
  }

  @Override
  public InetAddress resolveAddress(String hostname) throws Exception {
    return mockAddress;
  }

  @Override
  public CompletableFuture<Void> startListening(
      NetworkSocket socket, Consumer<byte[]> messageHandler) {
    MockNetworkSocket mockSocket = (MockNetworkSocket) socket;
    messageHandlers.put(mockSocket, messageHandler);
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public void sendData(NetworkSocket socket, byte[] data, InetAddress address, int port)
      throws Exception {
    MockNetworkSocket mockSocket = (MockNetworkSocket) socket;
    mockSocket.addSentMessage(data);

    // Don't simulate responses if connection failure is enabled
    if (simulateConnectionFailure) {
      return;
    }

    // Simulate immediate response for certain message types
    String message = new String(data);
    if (message.contains("\"t\":\"scan\"")) {
      simulateDeviceResponse(mockSocket);
    } else if (message.contains("\"t\":\"bind\"")) {
      if (!simulateBindFailure) {
        simulateBindOkResponse(mockSocket);
      }
    } else if (message.contains("\"t\":\"status\"")) {
      simulateStatusResponse(mockSocket);
    } else if (message.contains("\"t\":\"cmd\"")) {
      simulateCommandResponse(mockSocket);
    }
  }

  @Override
  public boolean isClosed(NetworkSocket socket) {
    return socket.isClosed();
  }

  @Override
  public void closeSocket(NetworkSocket socket) {
    socket.close();
    messageHandlers.remove(socket);
  }

  // Mock control methods
  public void simulateConnectionFailure(boolean simulate) {
    this.simulateConnectionFailure = simulate;
  }

  public void simulateBindFailure(boolean simulate) {
    this.simulateBindFailure = simulate;
  }

  public void simulateMessage(NetworkSocket socket, String message) {
    Consumer<byte[]> handler = messageHandlers.get(socket);
    if (handler != null) {
      handler.accept(message.getBytes());
    }
  }

  private void simulateDeviceResponse(MockNetworkSocket socket) {
    CompletableFuture.runAsync(
        () -> {
          try {
            Thread.sleep(10); // Small delay to simulate network
            // Simulate a proper encrypted device response with valid base64
            String deviceResponse =
                "{\"t\":\"pack\",\"i\":0,\"uid\":0,\"cid\":\"app\",\"pack\":\"YWJjZGVmZ2hpams=\"}";
            Consumer<byte[]> handler = messageHandlers.get(socket);
            if (handler != null) {
              handler.accept(deviceResponse.getBytes());
            }
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        });
  }

  private void simulateBindOkResponse(MockNetworkSocket socket) {
    CompletableFuture.runAsync(
        () -> {
          try {
            Thread.sleep(50); // Small delay to simulate network
            // Simulate a proper encrypted bind response with valid base64
            String bindOkResponse =
                "{\"t\":\"pack\",\"i\":1,\"uid\":0,\"cid\":\"app\",\"pack\":\"YmluZG9rZGF0YQ==\"}";
            Consumer<byte[]> handler = messageHandlers.get(socket);
            if (handler != null) {
              handler.accept(bindOkResponse.getBytes());
            }
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        });
  }

  private void simulateStatusResponse(MockNetworkSocket socket) {
    CompletableFuture.runAsync(
        () -> {
          try {
            Thread.sleep(10); // Small delay to simulate network
            // Simulate a proper encrypted status response with valid base64
            String statusResponse =
                "{\"t\":\"pack\",\"i\":0,\"uid\":0,\"cid\":\"app\",\"pack\":\"c3RhdHVzZGF0YQ==\"}";
            Consumer<byte[]> handler = messageHandlers.get(socket);
            if (handler != null) {
              handler.accept(statusResponse.getBytes());
            }
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        });
  }

  private void simulateCommandResponse(MockNetworkSocket socket) {
    CompletableFuture.runAsync(
        () -> {
          try {
            Thread.sleep(10); // Small delay to simulate network
            // Simulate a proper encrypted command response with valid base64
            String commandResponse =
                "{\"t\":\"pack\",\"i\":0,\"uid\":0,\"cid\":\"app\",\"pack\":\"Y21kcmVzcG9uc2U=\"}";
            Consumer<byte[]> handler = messageHandlers.get(socket);
            if (handler != null) {
              handler.accept(commandResponse.getBytes());
            }
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        });
  }

  /** Mock implementation of NetworkSocket */
  public static class MockNetworkSocket implements NetworkSocket {
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final java.util.List<byte[]> sentMessages = new java.util.ArrayList<>();

    @Override
    public boolean isClosed() {
      return closed.get();
    }

    @Override
    public void close() {
      closed.set(true);
    }

    @Override
    public Object getUnderlyingSocket() {
      return this;
    }

    public void addSentMessage(byte[] message) {
      synchronized (sentMessages) {
        sentMessages.add(message.clone());
      }
    }

    public java.util.List<byte[]> getSentMessages() {
      synchronized (sentMessages) {
        return new java.util.ArrayList<>(sentMessages);
      }
    }

    public void clearSentMessages() {
      synchronized (sentMessages) {
        sentMessages.clear();
      }
    }
  }
}
