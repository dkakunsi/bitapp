part of 'summary_bloc.dart';

abstract class SummaryState implements ObjectState {
  @override
  SummaryViewModel get object => SummaryViewModel(Summary.empty());
}

class InitialSummary extends SummaryState {}

class CalculatingSummary extends SummaryState {}

class SummaryCalculated extends SummaryState {
  final Summary _summary;

  SummaryCalculated(this._summary);

  @override
  SummaryViewModel get object => SummaryViewModel(_summary);
}

class SummaryCalculationFailed extends SummaryState {}
