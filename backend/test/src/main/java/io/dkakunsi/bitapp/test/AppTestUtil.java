package io.dkakunsi.bitapp.test;

import java.util.HashMap;
import java.util.Map;

import io.dkakunsi.bitapp.common.Launcher;

public abstract class AppTestUtil {

  protected static final String APP_PORT = "app.port";

  protected static final String USER_ID = "user@email.com";

  private Launcher launcher;

  private Map<String, String> env;

  protected void create(Map<String, String> appEnv) throws Exception {
    MongoServer.startDb();
    env = new HashMap<>();
    env.put("app.env", "test");
    env.putAll(appEnv);
  }

  protected void destroy() throws Exception {
    stopServer();
    MongoServer.stopDb();
  }

  protected void startServer(Launcher launcher) throws Exception {
    while (MongoServer.isNotRunning()) {
      System.out.println("Waiting for Mongo to start...");
      Thread.sleep(1000);
    }

    env.putAll(MongoServer.getDbConfig());
    this.launcher = launcher;
    this.launcher.launch(env::get);
  }

  protected void stopServer() {
    this.launcher.stop();
  }
}
