package com.gree.airconditioner.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class CommandTest {

  @Test
  public void toJson() throws Exception {
    Command command = Command.builder().buildScanCommand();
    assertEquals("{\"t\":\"scan\"}", command.toJson());
  }
}
