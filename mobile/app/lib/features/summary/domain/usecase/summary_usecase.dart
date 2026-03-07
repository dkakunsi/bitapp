import 'package:bitapp/common/util/processing_result.dart';
import 'package:bitapp/features/loan/data/loan.dart';
import 'package:bitapp/features/summary/data/summary.dart';
import 'package:bitapp/features/transaction/data/transaction.dart';
import 'package:bitapp/features/account/data/account_store.dart';
import 'package:bitapp/features/loan/data/loan_store.dart';
import 'package:bitapp/features/transaction/data/transaction_store.dart';
import 'package:logging/logging.dart';

class SummaryUseCase {
  final _logger = Logger("SummaryUseCase");
  final AccountStore _accountStore;
  final LoanStore _loanStore;
  final TransactionStore _transactionStore;

  SummaryUseCase(this._accountStore, this._loanStore, this._transactionStore);

  Future<ProcessingResult<Summary>> calculateSummary(String userId) async {
    _logger.info('Summarizing transactions for user');
    try {
      final results = await Future.wait([
        _calculateTotalAsset(userId),
        _calculateTotalDebt(userId),
        _calculateTotalIncome(userId),
        _calculateTotalExpense(userId),
      ]);
      final summary = Summary(
        totalAsset: results[0],
        totalDebt: results[1],
        totalIncome: results[2],
        totalExpense: results[3],
      );
      return ProcessingResult(data: summary);
    } on Exception catch (e) {
      _logger.warning('Error calculating summary: $e');
      return ProcessingResult(exception: e);
    }
  }

  Future<double> _calculateTotalAsset(String userId) async {
    double totalAsset = 0;
    var accounts = await _accountStore.getList(userId);
    for (var account in accounts) {
      totalAsset += account.balance ?? 0;
    }
    return totalAsset;
  }

  Future<double> _calculateTotalDebt(String userId) async {
    double totalDebt = 0;
    var loans = await _loanStore.getListByType(userId, LoanType.debt);
    for (var loan in loans) {
      totalDebt += loan.remainingAmount ?? 0;
    }
    return totalDebt;
  }

  Future<double> _calculateTotalIncome(String userId) async {
    double totalIncome = 0;
    final startOfMonth = DateTime(
      DateTime.now().year,
      DateTime.now().month,
      1,
      0,
      0,
      0,
      0,
    );
    final endOfMonth = DateTime(
      DateTime.now().year,
      DateTime.now().month + 1,
      0,
      23,
      59,
      59,
      999,
    );
    var transactions = await _transactionStore.getListByTypeAndDateRange(
      userId,
      TransactionType.credit,
      startOfMonth.millisecondsSinceEpoch,
      endOfMonth.millisecondsSinceEpoch,
    );
    for (var transaction in transactions) {
      totalIncome += transaction.amount;
    }
    return totalIncome;
  }

  Future<double> _calculateTotalExpense(String userId) async {
    double totalExpense = 0;
    final startOfMonth = DateTime(
      DateTime.now().year,
      DateTime.now().month,
      1,
      0,
      0,
      0,
      0,
    );
    final endOfMonth = DateTime(
      DateTime.now().year,
      DateTime.now().month + 1,
      0,
      23,
      59,
      59,
      999,
    );
    var transactions = await _transactionStore.getListByTypeAndDateRange(
      userId,
      TransactionType.debit,
      startOfMonth.millisecondsSinceEpoch,
      endOfMonth.millisecondsSinceEpoch,
    );
    for (var transaction in transactions) {
      totalExpense += transaction.amount;
    }
    return totalExpense;
  }
}
