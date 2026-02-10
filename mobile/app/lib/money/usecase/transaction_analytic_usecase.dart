import 'package:app_common/app_common.dart';
import 'package:bitapp/money/data/model/transaction.dart';
import 'package:bitapp/money/data/model/transaction_analytics.dart';
import 'package:bitapp/money/data/store/transaction_store.dart';
import 'package:logging/logging.dart';

class TransactionAnalyticUseCase {
  final _logger = Logger("TransactionAnalyticUseCase");
  final TransactionStore _transactionStore;

  TransactionAnalyticUseCase(this._transactionStore);

  Future<ProcessingResult<TransactionAnalytics>> analyzeTransactions(
    String userId,
    DateTime date,
  ) async {
    try {
      final expenseAnalysis = TransactionAnalytics(
        expenseTransactions: await getExpenseTransactionsByPeriod(userId, date),
        incomeTransactions: await getIncomeTransactionsByPeriod(userId, date),
        period: date,
      );
      return ProcessingResult(data: expenseAnalysis);
    } on Exception catch (e) {
      _logger.warning('Error Analyzing Expenses: $e');
      return ProcessingResult(exception: e);
    }
  }

  Future<double> calculateTotalIncome(String userId, DateTime date) async {
    double totalIncome = 0;
    var transactions = await getIncomeTransactionsByPeriod(userId, date);
    for (var transaction in transactions) {
      totalIncome += transaction.amount;
    }
    return totalIncome;
  }

  Future<List<Transaction>> getIncomeTransactionsByPeriod(
    String userId,
    DateTime date,
  ) async => await _getTransactionsByUserAndTypeAndPeriod(
    userId,
    TransactionType.credit,
    date,
  );

  Future<List<Transaction>> getExpenseTransactionsByPeriod(
    String userId,
    DateTime date,
  ) async => await _getTransactionsByUserAndTypeAndPeriod(
    userId,
    TransactionType.debit,
    date,
  );

  Future<List<Transaction>> _getTransactionsByUserAndTypeAndPeriod(
    String userId,
    TransactionType type,
    DateTime date,
  ) async {
    final startOfMonth = DateTime(date.year, date.month, 1, 0, 0, 0, 0);
    final endOfMonth = DateTime(date.year, date.month + 1, 0, 23, 59, 59, 999);
    return await _transactionStore.getListByTypeAndDateRange(
      userId,
      type,
      startOfMonth.millisecondsSinceEpoch,
      endOfMonth.millisecondsSinceEpoch,
    );
  }
}
