package io.dkakunsi.bitapp.draft.infrastructure.mongo.model;

import dev.morphia.annotations.Entity;
import io.dkakunsi.bitapp.CrossDomainReference;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.draft.application.port.AccountPort.ChatAccount;

@Entity(discriminator = "account")
public class AccountModel extends CrossDomainReferenceModel {

  public AccountModel(String id, String name) {
    super(id, name);
  }

  @Override
  public CrossDomainReference toCrossDomainReference() {
    return new ChatAccount(Id.of(getId()), getName());
  }
}
