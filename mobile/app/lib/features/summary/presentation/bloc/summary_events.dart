part of 'summary_bloc.dart';

abstract class SummaryEvent {
  const SummaryEvent();
}

class CalculateSummary extends SummaryEvent {
  final String userId;

  const CalculateSummary({required this.userId});
}
