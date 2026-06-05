import 'dart:async';

import 'package:bitapp/common/presentation/bloc/state.dart';
import 'package:bitapp/common/presentation/viewmodel/viewmodel.dart';
import 'package:bitapp/features/loan/domain/loan.dart';
import 'package:bitapp/features/loan/domain/loan_type.dart';
import 'package:bitapp/features/loan/presentation/viewmodel/loan_viewmodel.dart';
import 'package:bitapp/features/loan/domain/loan_usecase.dart';
import 'package:equatable/equatable.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

part 'loan_events.dart';
part 'loan_states.dart';

class LoanBloc extends Bloc<LoanEvent, LoanState> {
  final LoanUseCase _loanUseCase;

  LoanBloc(this._loanUseCase) : super(LoanInitial()) {
    on<FetchLoans>(_fetchLoans);
    on<GetLoans>(_getLoans);
    on<GetLoan>(_getLoan);
    on<AddLoan>(_addLoan);
    on<UpdateLoan>(_updateLoan);
    on<DeleteLoan>(_deleteLoan);
  }

  void _fetchLoans(FetchLoans event, Emitter<LoanState> emit) async {
    emit(LoanProcessing());
    final loadingResult = await _loanUseCase.fetchLoans(event.userId);
    if (loadingResult.isFailure) {
      emit(LoansFetchingFailed());
      return;
    }
    emit(LoansRetrieved(loadingResult.data));
  }

  void _getLoans(GetLoans event, Emitter<LoanState> emit) async {
    emit(LoanProcessing());
    final loadingResult = await _loanUseCase.getLoans(event.userId);
    if (loadingResult.isFailure) {
      emit(LoansRetrievalFailed());
      return;
    }
    emit(LoansRetrieved(loadingResult.data));
  }

  Future<void> _getLoan(GetLoan event, Emitter<LoanState> emit) async {
    emit(LoanProcessing());
    final loadingResult = await _loanUseCase.getLoan(event.id);
    if (loadingResult.isFailure) {
      emit(LoanRetrievalFailed());
      return;
    }
    emit(LoanRetrieved(loadingResult.data));
  }

  Future<void> _addLoan(AddLoan event, Emitter<LoanState> emit) async {
    final loan = Loan(
      title: event.title,
      description: event.description,
      amount: event.amount,
      partyName: event.partyName,
      date: event.date,
      time: event.time,
      type: event.type,
      userId: event.userId,
    );
    final addResult = await _loanUseCase.addLoan(loan);
    if (addResult.isFailure) {
      emit(LoanAdditionFailed());
      return;
    }
    emit(LoanAdded());
  }

  Future<void> _deleteLoan(DeleteLoan event, Emitter<LoanState> emit) async {
    emit(LoanProcessing());
    final deleteResult = await _loanUseCase.deleteLoan(event.id);
    if (deleteResult.isFailure) {
      emit(LoanDeletionFailed());
      return;
    }
    emit(LoanDeleted());
  }

  Future<void> _updateLoan(UpdateLoan event, Emitter<LoanState> emit) async {
    emit(LoanProcessing());
    final loan = Loan(
      id: event.id,
      title: event.title,
      description: event.description,
      amount: event.amount,
      partyName: event.partyName,
      date: event.date,
      time: event.time,
      type: event.type,
      userId: event.userId,
    );
    final updateResult = await _loanUseCase.updateLoan(event.id, loan);
    if (updateResult.isFailure) {
      emit(LoanUpdateFailed());
      return;
    }
    emit(LoanUpdated());
  }
}
