import 'package:app_common/app_common.dart';
import 'package:bitapp/money/data/model/summary.dart';
import 'package:bitapp/money/presentation/viewmodel/summary_viewmodel.dart';
import 'package:bitapp/money/usecase/summary_usecase.dart';
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
