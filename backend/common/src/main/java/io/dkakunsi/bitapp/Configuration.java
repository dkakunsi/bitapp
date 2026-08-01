package io.dkakunsi.bitapp;

import java.util.Optional;
import java.util.function.Function;

public interface Configuration {

  String APP_ENV = "app.env";

  Optional<String> get(String key);

  boolean isTest();

  public static final class EnvironmentConfiguration implements Configuration {

    private Function<String, String> environmentSupplier;

    private EnvironmentConfiguration(Function<String, String> environmentSupplier) {
      this.environmentSupplier = environmentSupplier;
    }

    @Override
    public Optional<String> get(String key) {
      var value = environmentSupplier.apply(key);
      return Optional.ofNullable(value);
    }

    public static EnvironmentConfiguration of() {
      return of(System::getenv);
    }

    public static EnvironmentConfiguration of(Function<String, String> environmentSupplier) {
      return new EnvironmentConfiguration(environmentSupplier);
    }

    @Override
    public boolean isTest() {
      var value = environmentSupplier.apply(APP_ENV);
      return "test".equalsIgnoreCase(value);
    }
  }
}
