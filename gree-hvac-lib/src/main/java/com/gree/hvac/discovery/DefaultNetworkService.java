package com.gree.hvac.discovery;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class DefaultNetworkService implements NetworkService {

  @Override
  public Enumeration<NetworkInterface> getNetworkInterfaces() throws Exception {
    return NetworkInterface.getNetworkInterfaces();
  }

  @Override
  public InetAddress getByName(String hostname) throws Exception {
    return InetAddress.getByName(hostname);
  }
}
