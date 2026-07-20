package io.dkakunsi.bitapp.chat.infrastructure.mongo.model;

import dev.morphia.annotations.Entity;
import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.chat.application.port.LoanPort.ChatLoan;

@Entity(discriminator = "loan")
public class LoanModel extends CrossDomainReferenceModel {

  public LoanModel(String id, String name) {
    super(id, name);
  }

  @Override
  public ChatLoan toCrossDomainReference() {
    return new ChatLoan(Id.of(getId()), getName());
  }
}
