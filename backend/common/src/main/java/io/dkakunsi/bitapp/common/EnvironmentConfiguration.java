package io.dkakunsi.bitapp.common;

import java.util.Optional;
import java.util.function.Function;

public final class EnvironmentConfiguration implements Configuration {

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
}
