import 'package:bitapp/money/data/model/transaction.dart';

class TransactionAnalytics {
  final DateTime period;
  final List<Transaction> incomeTransactions;
  final List<Transaction> expenseTransactions;

  TransactionAnalytics({
    required this.period,
    required this.incomeTransactions,
    required this.expenseTransactions,
  });

  static TransactionAnalytics empty() => TransactionAnalytics(
    period: DateTime.now(),
    incomeTransactions: [],
    expenseTransactions: [],
  );
}
