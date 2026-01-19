package io.dkakunsi.bitapp.loan.usecase;

import java.util.List;

import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.bitapp.loan.dto.GetUserLoansInput;
import io.dkakunsi.bitapp.loan.dto.GetUserLoansResult;
import io.dkakunsi.bitapp.loan.repository.LoanRepository;

public final class GetUserLoans implements UseCase<GetUserLoansInput, List<GetUserLoansResult>> {

  private final LoanRepository loanRepository;

  public GetUserLoans(LoanRepository loanRepository) {
    this.loanRepository = loanRepository;
  }

  @Override
  public Result<List<GetUserLoansResult>> execute(Context context, GetUserLoansInput input) {
    var loans = loanRepository.findByUserId(input.userId());
    var results = loans.stream()
        .map(GetUserLoansResult::from)
        .toList();
    return Result.success(results);
  }
}
