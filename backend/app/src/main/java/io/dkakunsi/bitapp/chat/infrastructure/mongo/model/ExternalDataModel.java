package io.dkakunsi.bitapp.chat.infrastructure.mongo.model;

import io.dkakunsi.bitapp.chat.domain.entity.ExternalData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class ExternalDataModel {

  private String id;
  private String name;

  public abstract ExternalData toExternalData();

  public static ExternalDataModel from(ExternalData externalData) {
    if (externalData instanceof io.dkakunsi.bitapp.chat.application.port.AccountPort.ChatAccount chatAccount) {
      return new AccountModel(chatAccount.getId().value(), chatAccount.getName());
    } else if (externalData instanceof io.dkakunsi.bitapp.chat.application.port.LoanPort.ChatLoan chatLoan) {
      return new LoanModel(chatLoan.getId().value(), chatLoan.getName());
    } else {
      throw new IllegalArgumentException("Unknown ExternalData type: " + externalData.getClass().getName());
    }
  }
}
