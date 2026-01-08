package io.dkakunsi.bitapp.common;

import java.util.Optional;

public interface Configuration {

  Optional<String> get(String key);

}
