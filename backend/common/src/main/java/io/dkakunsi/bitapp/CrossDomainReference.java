package io.dkakunsi.bitapp;

import lombok.Getter;

@Getter
public abstract class CrossDomainReference {
  protected final Id id;
  protected final String name;

  protected CrossDomainReference(Id id, String name) {
    this.id = id;
    this.name = name;
  }
}
