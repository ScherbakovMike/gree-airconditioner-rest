package com.gree.hvac.discovery;

public class DefaultTimeService implements TimeService {

  @Override
  public long getCurrentTimeMillis() {
    return System.currentTimeMillis();
  }
}
