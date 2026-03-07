import 'package:bitapp/common/presentation/bloc/state.dart';
import 'package:bitapp/features/summary/data/summary.dart';
import 'package:bitapp/features/summary/presentation/viewmodel/summary_viewmodel.dart';
import 'package:bitapp/features/summary/domain/usecase/summary_usecase.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

part 'summary_events.dart';
part 'summary_states.dart';

class SummaryBloc extends Bloc<SummaryEvent, SummaryState> {
  final SummaryUseCase _summaryUseCase;

  SummaryBloc(this._summaryUseCase) : super(InitialSummary()) {
    on<CalculateSummary>(_calculateSummary);
  }

  Future<void> _calculateSummary(
    CalculateSummary event,
    Emitter<SummaryState> emit,
  ) async {
    emit(CalculatingSummary());
    final result = await _summaryUseCase.calculateSummary(event.userId);
    if (result.isFailure) {
      emit(SummaryCalculationFailed());
      return;
    }
    emit(SummaryCalculated(result.data));
  }
}
