import 'dart:async';

import 'package:app_common/app_common.dart';
import 'package:bitapp/money/data/model/transaction_analytics.dart';
import 'package:bitapp/money/presentation/viewmodel/transaction_analytics_viewmodel.dart';
import 'package:bitapp/money/usecase/transaction_analytic_usecase.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

part 'transaction_analytics_events.dart';
part 'transaction_analytics_states.dart';

class TransactionAnalyticsBloc
    extends Bloc<TransactionAnalyticsEvent, TransactionAnalyticsState> {
  final TransactionAnalyticUseCase _transactionAnalyticUseCase;

  TransactionAnalyticsBloc(this._transactionAnalyticUseCase)
    : super(TransactionAnalyticsInitial()) {
    on<AnalyzeTransactions>(_analyzeEvent);
  }

  Future<void> _analyzeEvent(
    AnalyzeTransactions event,
    Emitter<TransactionAnalyticsState> emit,
  ) async {
    emit(AnalyzingExpense());
    final result = await _transactionAnalyticUseCase.analyzeTransactions(
      event.userId,
      event.date,
    );
    if (result.isFailure) {
      emit(TransactionAnalyticsFailed());
      return;
    }
    emit(TransactionAnalyticsSucceed(result.data));
  }
}
