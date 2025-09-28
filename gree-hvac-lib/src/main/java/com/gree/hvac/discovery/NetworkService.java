package com.gree.hvac.discovery;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public interface NetworkService {
  Enumeration<NetworkInterface> getNetworkInterfaces() throws SocketException;

  InetAddress getByName(String hostname) throws UnknownHostException;
}
