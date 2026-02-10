part of 'transaction_analytics_bloc.dart';

abstract class TransactionAnalyticsState implements ObjectState {
  @override
  TransactionAnalyticsViewModel get object =>
      TransactionAnalyticsViewModel(TransactionAnalytics.empty());
}

class TransactionAnalyticsInitial extends TransactionAnalyticsState {}

class AnalyzingExpense extends TransactionAnalyticsState {}

class TransactionAnalyticsFailed extends TransactionAnalyticsState {}

class TransactionAnalyticsSucceed extends TransactionAnalyticsState {
  final TransactionAnalytics expenseAnalysis;

  TransactionAnalyticsSucceed(this.expenseAnalysis);
  @override
  TransactionAnalyticsViewModel get object =>
      TransactionAnalyticsViewModel(expenseAnalysis);
}
