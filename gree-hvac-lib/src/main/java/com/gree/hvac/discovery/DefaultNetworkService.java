package com.gree.hvac.discovery;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class DefaultNetworkService implements NetworkService {

  @Override
  public Enumeration<NetworkInterface> getNetworkInterfaces() throws SocketException {
    return NetworkInterface.getNetworkInterfaces();
  }

  @Override
  public InetAddress getByName(String hostname) throws UnknownHostException {
    return InetAddress.getByName(hostname);
  }
}
