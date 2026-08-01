package io.dkakunsi.bitapp.test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.dkakunsi.bitapp.Configuration;
import io.dkakunsi.bitapp.Launcher;

public abstract class AppTestUtil {

  protected static final String APP_PORT = "app.port";

  protected static final String USER_ID = "user@email.com";

  private static final List<Integer> PORTS = new ArrayList<>();

  private static Map<String, Object> testDependencies = new HashMap<>();

  private Launcher launcher;

  private Map<String, String> env;

  protected void create(Map<String, String> appEnv) throws Exception {
    MongoServer.startDb();
    env = new HashMap<>();
    env.put(Configuration.APP_ENV, "test");
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

  protected static synchronized int getPort() {
    int port = PORTS.size() + 20000;
    PORTS.add(port);
    return port;
  }

  public static <T> T getTestDependency(Class<T> dependencyClass) {
    var key = getKey(dependencyClass);
    var dependency = testDependencies.get(key);
    if (dependency == null) {
      throw new IllegalStateException("Test dependency not found for key: " + key);
    }
    if (!dependencyClass.isInstance(dependency)) {
      throw new IllegalStateException("Test dependency for key: " + key + " is not of type: " + dependencyClass.getName());
    }
    return (T) dependency;
  }

  public static <T> void setTestDependency(Class<T> dependencyClass, T dependency) {
    var key = getKey(dependencyClass);
    testDependencies.put(key, dependency);
  }

  private static final String getKey(Class<?> dependencyClass) {
    return dependencyClass.getName();
  }
}
