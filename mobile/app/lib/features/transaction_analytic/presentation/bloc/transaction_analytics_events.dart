part of 'transaction_analytics_bloc.dart';

abstract class TransactionAnalyticsEvent {}

class AnalyzeTransactions extends TransactionAnalyticsEvent {
  final String userId;
  final DateTime date;

  AnalyzeTransactions({required this.userId, required this.date});
}
