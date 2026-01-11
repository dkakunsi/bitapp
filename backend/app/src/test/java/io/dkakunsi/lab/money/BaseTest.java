package io.dkakunsi.lab.money;

import java.util.HashMap;
import java.util.Map;

import io.dkakunsi.lab.test.MongoServer;

public abstract class BaseTest {
  static final String APP_PORT = "app.port";

  private Launcher launcher;

  private Map<String, String> env;

  protected void create(Map<String, String> appEnv) throws Exception {
    MongoServer.startDb();
    env = new HashMap<>();
    env.putAll(appEnv);
  }

  protected void destroy() throws Exception {
    stopServer();
    MongoServer.stopDb();
  }

  protected void startServer() throws Exception {
    while (MongoServer.isNotRunning()) {
      System.out.println("Waiting for Mongo to start...");
      Thread.sleep(1000);
    }

    env.putAll(MongoServer.getDbConfig());

    launcher = new Launcher();
    launcher.launch(env::get);
  }

  protected void stopServer() {
    launcher.stop();
  }
}
