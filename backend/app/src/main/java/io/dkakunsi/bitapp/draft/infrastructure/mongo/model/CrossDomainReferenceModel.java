package io.dkakunsi.bitapp.draft.infrastructure.mongo.model;

import dev.morphia.annotations.Entity;
import io.dkakunsi.bitapp.CrossDomainReference;
import io.dkakunsi.bitapp.draft.application.port.AccountPort.ChatAccount;
import io.dkakunsi.bitapp.draft.application.port.LoanPort.ChatLoan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(useDiscriminator = true)
public abstract class CrossDomainReferenceModel {

  private String id;
  private String name;

  public abstract CrossDomainReference toCrossDomainReference();

  public static CrossDomainReferenceModel from(CrossDomainReference crossDomainReference) {
    if (crossDomainReference instanceof ChatAccount chatAccount) {
      return new AccountModel(chatAccount.getId().value(), chatAccount.getName());
    } else if (crossDomainReference instanceof ChatLoan chatLoan) {
      return new LoanModel(chatLoan.getId().value(), chatLoan.getName());
    } else {
      throw new IllegalArgumentException(
          "Unknown CrossDomainReference type: " + crossDomainReference.getClass().getName());
    }
  }
}
