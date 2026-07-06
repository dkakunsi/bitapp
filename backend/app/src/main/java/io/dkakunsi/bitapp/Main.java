package io.dkakunsi.bitapp;

public class Main {

  private static final Logger LOGGER = SystemLogger.getLogger(Main.class);

  public static void main(String[] args) {
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
