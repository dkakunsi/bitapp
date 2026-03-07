import 'package:bitapp/common/presentation/viewmodel/viewmodel.dart';
import 'package:bitapp/common/util/formatter.dart';
import 'package:bitapp/features/transaction/data/transaction.dart';
import 'package:bitapp/features/transaction_analytic/data/transaction_analytics.dart';
import 'package:bitapp/features/transaction/presentation/viewmodel/transaction_viewmodel.dart';
import 'package:flutter/widgets.dart';

class TransactionAnalyticsViewModel implements ViewModel {
  final TransactionAnalytics transactionAnalytics;
  late final double income;
  late final double expense;
  late final List<ExpenseGroup> expenseGroups;

  TransactionAnalyticsViewModel(this.transactionAnalytics) {
    income = transactionAnalytics.incomeTransactions.fold(
      0,
      (previousValue, e) => previousValue + e.amount,
    );
    expense = transactionAnalytics.expenseTransactions.fold(
      0,
      (previousValue, e) => previousValue + e.amount,
    );
    expenseGroups = _groupExpenses(
      transactionAnalytics.expenseTransactions,
      expense,
    );
  }

  static List<ExpenseGroup> _groupExpenses(
    List<Transaction> transactions,
    double expense,
  ) {
    final Map<String, List<Transaction>> groupedTransactions = {};
    for (var t in transactions) {
      if (groupedTransactions.containsKey(t.category!.name)) {
        groupedTransactions[t.category!.name]!.add(t);
      } else {
        groupedTransactions[t.category!.name] = [t];
      }
    }
    return groupedTransactions.entries
        .map((e) => ExpenseGroup(e.value, expense))
        .toList();
  }

  String getPeriod(BuildContext context) {
    return transactionAnalytics.period.toPeriodFormat(context);
  }

  DateTime get anlaysisDate => transactionAnalytics.period;

  double get accumulation => income - expense;
}

class ExpenseGroup {
  final List<Transaction> _transactions;
  late final double total;
  late final double percentage;

  ExpenseGroup(this._transactions, double totalExpenses) {
    total = _transactions.fold(
      0,
      (previousValue, e) => previousValue + e.amount,
    );
    percentage = (total / totalExpenses) * 100;
  }

  TransactionCategory get category => _transactions[0].category!;

  Color get color => category.color;

  List<TransactionViewModel> get transactions =>
      _transactions.map((t) => TransactionViewModel(t)).toList();
}
