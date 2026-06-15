import 'package:bitapp/common/util/processing_result.dart';
import 'package:bitapp/features/transaction/domain/local_tansaction_service.dart';
import 'package:bitapp/features/transaction/domain/transaction.dart';
import 'package:bitapp/features/transaction/domain/transaction_type.dart';
import 'package:bitapp/features/transaction_analytic/data/transaction_analytics.dart';
import 'package:bitapp/features/transaction/data/transaction_store.dart';
import 'package:logging/logging.dart';

class TransactionAnalyticUseCase {
  final _logger = Logger("TransactionAnalyticUseCase");
  final TransactionStore _transactionStore;
  final LocalTransactionService _localTransactionService;

  TransactionAnalyticUseCase({
    required TransactionStore transactionStore,
    required LocalTransactionService localTransactionService,
  }) : _transactionStore = transactionStore,
       _localTransactionService = localTransactionService;

  Exception _toException(Object error) {
    return error is Exception ? error : Exception(error.toString());
  }

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
    } catch (e, stackTrace) {
      _logger.warning('Error Analyzing Expenses', e, stackTrace);
      return ProcessingResult(exception: _toException(e));
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
    final transactionModels = await _transactionStore.getListByTypeAndDateRange(
      userId,
      type,
      startOfMonth.millisecondsSinceEpoch,
      endOfMonth.millisecondsSinceEpoch,
    );
    return await Future.wait(
      transactionModels.map(
        (model) => _localTransactionService.buildTransaction(model),
      ),
    );
  }
}
