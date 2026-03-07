import 'package:bitapp/common/util/processing_result.dart';
import 'package:bitapp/features/configuration/data/configuration_store.dart';
import 'package:bitapp/features/loan/data/loan_api.dart';
import 'package:bitapp/features/loan/data/loan.dart';
import 'package:bitapp/features/loan/data/loan_store.dart';
import 'package:logging/logging.dart';
import 'package:uuid/uuid.dart';

class LoanUseCase {
  final _logger = Logger("LoanUseCase");
  final LoanApi _loanApi;
  final LoanStore _loanStore;
  final ConfigurationStore _configurationStore;
  final LocalLoanService _localLoanService;

  LoanUseCase(
    this._loanApi,
    this._loanStore,
    this._configurationStore,
    this._localLoanService,
  );

  Future<ProcessingResult<void>> addLoan(Loan loan) async {
    try {
      Loan? result;
      if (await _configurationStore.isRemoteEnabled) {
        result = await _loanApi.add(loan);
      }
      await _loanStore.save(result ?? loan.copyWith(id: Uuid().v4()));
      return ProcessingResult();
    } on Exception catch (e) {
      _logger.warning('Error creating loan: $e');
      return ProcessingResult(exception: e);
    }
  }

  Future<ProcessingResult<void>> updateLoan(String id, Loan loan) async {
    try {
      Loan? result;
      if (await _configurationStore.isRemoteEnabled) {
        result = await _loanApi.update(id, loan);
        await _loanStore.save(result);
      } else {
        await _localLoanService.update(id, loan);
      }
      return ProcessingResult();
    } on Exception catch (e) {
      _logger.warning('Error updating loan: $e');
      return ProcessingResult(exception: e);
    }
  }

  Future<ProcessingResult<void>> deleteLoan(String id) async {
    try {
      if (await _configurationStore.isRemoteEnabled) {
        await _loanApi.delete(id);
      }
      await _loanStore.delete(id);
      return ProcessingResult();
    } on Exception catch (e) {
      _logger.warning('Error deleting loan: $e');
      return ProcessingResult(exception: e);
    }
  }

  Future<ProcessingResult<List<Loan>>> fetchLoans(String userId) async {
    try {
      List<Loan> loans;
      if (await _configurationStore.isRemoteEnabled) {
        loans = await _loanApi.fetchByUser(userId);
        if (loans.isNotEmpty) {
          await _loanStore.clear();
          await _loanStore.addAll(loans);
        }
      } else {
        loans = await _loanStore.getList(userId);
      }
      return ProcessingResult(data: loans);
    } on Exception catch (e) {
      _logger.warning('Error fetching loans: $e');
      return ProcessingResult(exception: e);
    }
  }

  Future<ProcessingResult<List<Loan>>> getLoans(String userId) async {
    try {
      final loans = await _loanStore.getList(userId);
      return ProcessingResult(data: loans);
    } on Exception catch (e) {
      _logger.warning('Error getting loans from store: $e');
      return ProcessingResult(exception: e);
    }
  }

  Future<ProcessingResult<Loan>> getLoan(String id) async {
    try {
      final loan = await _loanStore.get(id);
      return ProcessingResult(data: loan);
    } on Exception catch (e) {
      _logger.warning('Error getting loan from store: $e');
      return ProcessingResult(exception: e);
    }
  }
}

class LocalLoanService {
  final LoanStore _loanStore;

  LocalLoanService(this._loanStore);

  Future<void> update(String id, Loan updatingLoan) async {
    final currentLoan = await _loanStore.get(id);
    if (currentLoan != null && currentLoan.amount != updatingLoan.amount) {
      // the loan amount is changed, remaining amount should be re-calculated
      _loanStore.save(
        updatingLoan.copyWith(
          remainingAmount:
              (currentLoan.remainingAmount ?? 0) +
              (updatingLoan.amount - currentLoan.amount),
        ),
      );
    } else {
      _loanStore.save(updatingLoan);
    }
  }
}
