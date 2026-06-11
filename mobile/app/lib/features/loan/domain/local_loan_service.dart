import 'package:bitapp/features/loan/data/loan_model.dart';
import 'package:bitapp/features/loan/data/loan_store.dart';
import 'package:bitapp/features/loan/domain/loan.dart';

class LocalLoanService {
  final LoanStore _loanStore;

  LocalLoanService(this._loanStore);

  Future<void> update(String id, Loan updatingLoan) async {
    final loanModel = await _loanStore.get(id);
    final updatingLoanModel = LoanModel.fromEntity(updatingLoan);
    if (loanModel != null && loanModel.amount != updatingLoan.amount) {
      // the loan amount is changed, remaining amount should be re-calculated
      _loanStore.save(
        updatingLoanModel.copyWith(
          remainingAmount:
              (loanModel.remainingAmount ?? 0) +
              (updatingLoan.amount - loanModel.amount),
        ),
      );
    } else {
      _loanStore.save(updatingLoanModel);
    }
  }
}
