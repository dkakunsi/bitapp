import 'package:bitapp/features/configuration/data/configuration_store.dart';
import 'package:bitapp/features/loan/data/loan_api.dart';
import 'package:bitapp/features/loan/data/loan_model.dart';
import 'package:bitapp/features/loan/data/loan_store.dart';
import 'package:bitapp/features/loan/domain/loan.dart';
import 'package:uuid/uuid.dart';

class LoanRepository {
  final LoanApi _loanApi;
  final LoanStore _loanStore;
  final ConfigurationStore _configurationStore;

  LoanRepository({
    required LoanApi loanApi,
    required LoanStore loanStore,
    required ConfigurationStore configurationStore,
  }) : _loanApi = loanApi,
       _loanStore = loanStore,
       _configurationStore = configurationStore;

  Future<Loan> addLoan(Loan loan) async {
    final model = LoanModel.fromEntity(loan);
    LoanModel? result;
    if (await _configurationStore.isRemoteEnabled) {
      result = await _loanApi.add(model);
    }
    await _loanStore.save(result ?? model.copyWith(id: Uuid().v4()));
    return result!;
  }

  Future<Loan> updateLoan(String id, Loan loan) async {
    final model = LoanModel.fromEntity(loan);
    final result = await _loanApi.update(id, model);
    await _loanStore.save(result);
    return result;
  }

  Future<void> deleteLoan(String id) async {
    if (await _configurationStore.isRemoteEnabled) {
      await _loanApi.delete(id);
    }
    await _loanStore.delete(id);
  }

  Future<List<Loan>> fetchByUser(String userId) async {
      List<LoanModel> loans;
      if (await _configurationStore.isRemoteEnabled) {
        loans = await _loanApi.fetchByUser(userId);
        if (loans.isNotEmpty) {
          await _loanStore.clear();
          await _loanStore.addAll(loans);
        }
      } else {
        loans = await _loanStore.getList(userId);
      }
      return loans;
  }

  Future<List<Loan>> getByUser(String userId) async {
    return await _loanStore.getList(userId);
  }

  Future<Loan?> getById(String id) async {
    return await _loanStore.get(id);
  }
}
