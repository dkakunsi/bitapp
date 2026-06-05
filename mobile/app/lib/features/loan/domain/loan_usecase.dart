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

  Future<ProcessingResult<void>> addLoan(Loan loan) async {
    try {
      await _loanRepository.addLoan(loan.toModel());
      return ProcessingResult();
    } on Exception catch (e) {
      _logger.warning('Error creating loan: $e');
      return ProcessingResult(exception: e);
    }
  }

  Future<ProcessingResult<void>> updateLoan(String id, Loan loan) async {
    try {
      if (await _configurationStore.isRemoteEnabled) {
        await _loanRepository.updateLoan(id, loan.toModel());
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
      await _loanRepository.deleteLoan(id);
      return ProcessingResult();
    } on Exception catch (e) {
      _logger.warning('Error deleting loan: $e');
      return ProcessingResult(exception: e);
    }
  }

  Future<ProcessingResult<List<Loan>>> fetchLoans(String userId) async {
    try {
      List<LoanModel> loanModels = await _loanRepository.fetchByUser(userId);
      final loans = loanModels.map((model) => Loan.fromModel(model)).toList();
      return ProcessingResult(data: loans);
    } on Exception catch (e) {
      _logger.warning('Error fetching loans: $e');
      return ProcessingResult(exception: e);
    }
  }

  Future<ProcessingResult<List<Loan>>> getLoans(String userId) async {
    try {
      final loanModels = await _loanRepository.getByUser(userId);
      final loans = loanModels.map((model) => Loan.fromModel(model)).toList();
      return ProcessingResult(data: loans);
    } on Exception catch (e) {
      _logger.warning('Error getting loans from store: $e');
      return ProcessingResult(exception: e);
    }
  }

  Future<ProcessingResult<Loan>> getLoan(String id) async {
    try {
      final loanModel = await _loanRepository.getById(id);
      if (loanModel == null) {
        return ProcessingResult(exception: Exception('Loan not found'));
      }
      final loan = Loan.fromModel(loanModel);
      return ProcessingResult(data: loan);
    } on Exception catch (e) {
      _logger.warning('Error getting loan from store: $e');
      return ProcessingResult(exception: e);
    }
  }
}
