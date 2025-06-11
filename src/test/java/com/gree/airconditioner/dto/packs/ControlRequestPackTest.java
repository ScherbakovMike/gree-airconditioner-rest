package com.gree.airconditioner.dto.packs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gree.airconditioner.dto.status.GreeDeviceStatus;
import com.gree.airconditioner.dto.status.Switch;
import org.junit.jupiter.api.Test;

public class ControlRequestPackTest {

  @Test
  public void testPowerCommand() throws Exception {
    GreeDeviceStatus status = new GreeDeviceStatus();
    status.setPower(Switch.ON);

    ControlRequestPack pack = new ControlRequestPack(status);
    assertEquals("{\"t\":\"cmd\",\"opt\":[\"Pow\"],\"p\":[1]}", pack.toJson());
  }
}
