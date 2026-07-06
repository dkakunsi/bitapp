package io.dkakunsi.bitapp;

import java.util.Optional;

public interface Configuration {

  Optional<String> get(String key);

}
