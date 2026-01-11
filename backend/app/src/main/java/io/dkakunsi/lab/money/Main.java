package io.dkakunsi.lab.money;

import java.util.UUID;

import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.Logger;
import io.dkakunsi.bitapp.common.SystemLogger;

public class Main {

  private static final Logger LOGGER = SystemLogger.getLogger(Main.class);

  public static void main(String[] args) {
    Context.set(Context.builder().requestId(UUID.randomUUID().toString()).requester("SYSTEM").build());
    LOGGER.info("Starting services!");
    try {
      new AppLauncher().launch(System::getenv);
      LOGGER.info("Service is started!");
    } catch (Exception ex) {
      LOGGER.error("Cannot start the application", ex);
      System.exit(1);
    }
  }
}
