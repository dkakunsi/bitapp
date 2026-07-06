package io.dkakunsi.bitapp;

import java.util.function.Function;

public interface Launcher {
  void launch(Function<String, String> envProvider);

  void stop();
}
