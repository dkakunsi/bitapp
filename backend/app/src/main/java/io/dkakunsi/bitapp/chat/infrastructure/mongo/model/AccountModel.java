package io.dkakunsi.bitapp.chat.infrastructure.mongo.model;

import dev.morphia.annotations.Entity;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.chat.application.port.AccountPort.ChatAccount;
import io.dkakunsi.bitapp.chat.domain.entity.ExternalData;

@Entity(discriminator = "account")
public class AccountModel extends ExternalDataModel {

  public AccountModel(String id, String name) {
    super(id, name);
  }

  @Override
  public ExternalData toExternalData() {
    return new ChatAccount(Id.of(getId()), getName());
  }
}
