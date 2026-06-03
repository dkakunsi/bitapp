import 'dart:async';

import 'package:bitapp/common/presentation/bloc/state.dart';
import 'package:bitapp/common/presentation/viewmodel/viewmodel.dart';
import 'package:bitapp/features/account/data/account.dart';
import 'package:bitapp/features/account/presentation/bloc/account_bloc.dart';
import 'package:bitapp/features/configuration/domain/usecase/configuration_usecase.dart';
import 'package:bitapp/features/loan/data/loan.dart';
import 'package:bitapp/features/loan/presentation/bloc/loan_bloc.dart';
import 'package:bitapp/features/transaction/domain/transaction_category.dart';
import 'package:bitapp/features/transaction/domain/transaction_type.dart';
import 'package:bitapp/features/transaction/presentation/viewmodel/transaction_viewmodel.dart';
import 'package:bitapp/features/transaction/domain/transaction_usecase.dart';
import 'package:bitapp/features/transaction/domain/transaction.dart';
import 'package:equatable/equatable.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

part 'transaction_events.dart';
part 'transaction_states.dart';

class TransactionBloc extends Bloc<TransactionEvent, TransactionState> {
  final TransactionUseCase _transactionUseCase;
  final AccountBloc _accountBloc;
  final LoanBloc _loanBloc;
  final ConfigurationUseCase _configurationRepository;

  TransactionBloc(
    this._transactionUseCase,
    this._configurationRepository,
    this._accountBloc,
    this._loanBloc,
  ) : super(TransactionInitial()) {
    on<AddTransaction>(_addTransaction);
    on<DeleteTransaction>(_deleteTransaction);
    on<FetchTransactions>(_fetchTransactions);
    on<GetTransactions>(_getTransactions);
    on<GetAccountTransactions>(_getAccountTransactions);
    on<GetLoanTransactions>(_getLoanTransactions);
  }

  Future<void> _addTransaction(
    AddTransaction event,
    Emitter<TransactionState> emit,
  ) async {
    emit(TransactionProcessing());
    final transaction = Transaction(
      userId: event.userId,
      title: event.title,
      description: event.description,
      amount: event.amount,
      date: event.date,
      time: event.time,
      type: event.transactionType,
      transactionCategory: event.category,
      sourceAccount: event.sourceAccount,
      destinationAccount: event.destinationAccount,
      loan: event.loan,
    );
    final result = await _transactionUseCase.addTransaction(transaction);
    if (result.isFailure) {
      emit(TransactionAdditionFailed());
      return;
    }
    _accountBloc.add(FetchAccounts(userId: event.userId));
    _loanBloc.add(FetchLoans(userId: event.userId));
    emit(TransactionAdded());
  }

  Future<void> _deleteTransaction(
    DeleteTransaction event,
    Emitter<TransactionState> emit,
  ) async {
    emit(TransactionProcessing());
    final result = await _transactionUseCase.deleteTransaction(event.id);
    if (result.isFailure) {
      emit(TransactionDeletionFailed());
      return;
    }
    emit(TransactionDeleted());

    final configResult = await _configurationRepository.getConfiguration();
    if (configResult.isEmpty) {
      emit(TransactionSynchronizationFailed());
      return;
    }
    _accountBloc.add(FetchAccounts(userId: configResult.data.user!.id!));
    _loanBloc.add(FetchLoans(userId: configResult.data.user!.id!));
  }

  Future<void> _fetchTransactions(
    FetchTransactions event,
    Emitter<TransactionState> emit,
  ) async {
    emit(TransactionProcessing());
    final result = await _transactionUseCase.fetchTransactions(
      userId: event.userId,
    );
    if (result.isFailure) {
      emit(TransactionsFetchingFailed());
      return;
    }
    emit(UserTransactionsRetrieved(result.data));
  }

  Future<void> _getAccountTransactions(
    GetAccountTransactions event,
    Emitter<TransactionState> emit,
  ) async {
    emit(TransactionProcessing());
    final result = await _transactionUseCase.getTransactionsByAccount(
      accountId: event.accountId,
    );
    if (result.isFailure) {
      emit(TransactionsRetrievalFailed());
      return;
    }
    emit(AccountTransactionsRetrieved(result.data));
  }

  Future<void> _getLoanTransactions(
    GetLoanTransactions event,
    Emitter<TransactionState> emit,
  ) async {
    emit(TransactionProcessing());
    final result = await _transactionUseCase.getTransactionsByLoan(
      loanId: event.loanId,
    );
    if (result.isFailure) {
      emit(TransactionsRetrievalFailed());
      return;
    }
    emit(LoanTransactionsRetrieved(result.data));
  }

  Future<void> _getTransactions(
    GetTransactions event,
    Emitter<TransactionState> emit,
  ) async {
    emit(TransactionProcessing());
    final result = await _transactionUseCase.getTransactions(event.userId);
    if (result.isFailure) {
      emit(TransactionsRetrievalFailed());
      return;
    }
    emit(UserTransactionsRetrieved(result.data));
  }
}
