package io.dkakunsi.lab.money;

import java.util.HashMap;
import java.util.Map;

import io.dkakunsi.lab.test.Mongo;

public abstract class BaseTest {
  static final String SCHEMA = "schema";
  static final String APP_PORT = "app.port";

  private Launcher launcher;

  protected void create() throws Exception {
    Mongo.startDb();
  }

  protected void destroy() throws Exception {
    stopServer();
    Mongo.stopDb();
  }

  protected void startServer(int port) throws Exception {
    while (Mongo.isNotRunning()) {
      System.out.println("Waiting for Mongo to start...");
      Thread.sleep(1000);
    }

    var mongoEnv = Mongo.getDbConfig();
    var appEnv = Map.of(APP_PORT, Integer.toString(port));

    var env = new HashMap<String, String>();
    env.putAll(mongoEnv);
    env.putAll(appEnv);

    launcher = new Launcher();
    launcher.launch(env::get);
  }

  protected void stopServer() {
    launcher.stop();
  }
}
