package com.gree.hvac.discovery;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public interface NetworkService {
  Enumeration<NetworkInterface> getNetworkInterfaces() throws Exception;

  InetAddress getByName(String hostname) throws Exception;
}
