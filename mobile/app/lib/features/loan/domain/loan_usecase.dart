import 'package:bitapp/common/util/processing_result.dart';
import 'package:bitapp/features/configuration/data/configuration_store.dart';
import 'package:bitapp/features/loan/data/loan_model.dart';
import 'package:bitapp/features/loan/data/loan_repository.dart';
import 'package:bitapp/features/loan/domain/loan.dart';
import 'package:bitapp/features/loan/domain/local_loan_service.dart';
import 'package:logging/logging.dart';

class LoanUseCase {
  final _logger = Logger("LoanUseCase");
  final LoanRepository _loanRepository;
  final ConfigurationStore _configurationStore;
  final LocalLoanService _localLoanService;

  LoanUseCase({
    required ConfigurationStore configurationStore,
    required LocalLoanService localLoanService,
    required LoanRepository loanRepository,
  }) : _configurationStore = configurationStore,
       _localLoanService = localLoanService,
       _loanRepository = loanRepository;

  Exception _toException(Object error) {
    return error is Exception ? error : Exception(error.toString());
  }

  Future<ProcessingResult<void>> addLoan(Loan loan) async {
    try {
      await _loanRepository.addLoan(LoanModel.fromEntity(loan));
      return ProcessingResult();
    } catch (e, stackTrace) {
      _logger.warning('Error creating loan', e, stackTrace);
      return ProcessingResult(exception: _toException(e));
    }
  }

  Future<ProcessingResult<void>> updateLoan(String id, Loan loan) async {
    try {
      if (await _configurationStore.isRemoteEnabled) {
        await _loanRepository.updateLoan(id, LoanModel.fromEntity(loan));
      } else {
        await _localLoanService.update(id, loan);
      }
      return ProcessingResult();
    } catch (e, stackTrace) {
      _logger.warning('Error updating loan', e, stackTrace);
      return ProcessingResult(exception: _toException(e));
    }
  }

  Future<ProcessingResult<void>> deleteLoan(String id) async {
    try {
      await _loanRepository.deleteLoan(id);
      return ProcessingResult();
    } catch (e, stackTrace) {
      _logger.warning('Error deleting loan', e, stackTrace);
      return ProcessingResult(exception: _toException(e));
    }
  }

  Future<ProcessingResult<List<Loan>>> fetchLoans(String userId) async {
    try {
      List<Loan> loans = await _loanRepository.fetchByUser(userId);
      return ProcessingResult(data: loans);
    } catch (e, stackTrace) {
      _logger.warning('Error fetching loans', e, stackTrace);
      return ProcessingResult(exception: _toException(e));
    }
  }

  Future<ProcessingResult<List<Loan>>> getLoans(String userId) async {
    try {
      final loans = await _loanRepository.getByUser(userId);
      return ProcessingResult(data: loans);
    } catch (e, stackTrace) {
      _logger.warning('Error getting loans from store', e, stackTrace);
      return ProcessingResult(exception: _toException(e));
    }
  }

  Future<ProcessingResult<Loan>> getLoan(String id) async {
    try {
      final loan = await _loanRepository.getById(id);
      if (loan == null) {
        return ProcessingResult(exception: Exception('Loan not found'));
      }
      return ProcessingResult(data: loan);
    } catch (e, stackTrace) {
      _logger.warning('Error getting loan from store', e, stackTrace);
      return ProcessingResult(exception: _toException(e));
    }
  }
}
