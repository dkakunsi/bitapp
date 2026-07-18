package io.dkakunsi.bitapp.chat.domain.entity;

import io.dkakunsi.bitapp.Id;
import lombok.Getter;

@Getter
public abstract class ExternalData {
  protected final Id id;
  protected final String name;

  protected ExternalData(Id id, String name) {
    this.id = id;
    this.name = name;
  }
}
