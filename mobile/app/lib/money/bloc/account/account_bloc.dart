import 'dart:async';

import 'package:bitapp/common/common.dart';
import 'package:bitapp/money/data/model/account.dart';
import 'package:bitapp/money/presentation/viewmodel/account_viewmodel.dart';
import 'package:bitapp/money/usecase/account_usecase.dart';
import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

part 'account_events.dart';
part 'account_states.dart';

class AccountBloc extends Bloc<AccountEvent, AccountState> {
  final AccountUseCase _accountUseCase;

  AccountBloc(this._accountUseCase) : super(AccountInitial()) {
    on<GetAccounts>(_getAccounts);
    on<GetAccount>(_getAccount);
    on<AddAccount>(_addAccount);
    on<UpdateAccount>(_updateAccount);
    on<DeleteAccount>(_deleteAccount);
    on<FetchAccounts>(_fetchAccounts);
  }

  Future<void> _fetchAccounts(
    FetchAccounts event,
    Emitter<AccountState> emit,
  ) async {
    emit(AccountProcessing());
    final result = await _accountUseCase.fetchAccounts(event.userId);
    if (result.isFailure) {
      emit(AccountsFetchingFailed());
      return;
    }
    emit(AccountsRetrieved(result.data));
  }

  Future<void> _getAccounts(
    GetAccounts event,
    Emitter<AccountState> emit,
  ) async {
    emit(AccountProcessing());
    final result = await _accountUseCase.getAccounts(event.userId);
    if (result.isFailure) {
      emit(AccountsRetrievalFailed());
      return;
    }
    emit(AccountsRetrieved(result.data));
  }

  Future<void> _getAccount(GetAccount event, Emitter<AccountState> emit) async {
    emit(AccountProcessing());
    final result = await _accountUseCase.getAccount(event.id);
    if (result.isFailure) {
      emit(AccountRetrievalFailed());
      return;
    }
    emit(AccountRetrieved(result.data));
  }

  Future<void> _addAccount(AddAccount event, Emitter<AccountState> emit) async {
    emit(AccountProcessing());
    final account = Account(
      userId: event.userId,
      name: event.name,
      type: AccountType.valueOf(event.type),
      themeColor: event.themeColor,
    );
    final result = await _accountUseCase.saveAccount(account);
    if (result.isFailure) {
      emit(AccountAdditionFailed());
      return;
    }
    emit(AccountAdded());
  }

  Future<void> _updateAccount(
    UpdateAccount event,
    Emitter<AccountState> emit,
  ) async {
    emit(AccountProcessing());
    final account = Account(
      id: event.id,
      userId: event.userId,
      name: event.name,
      type: AccountType.valueOf(event.type),
      themeColor: event.themeColor,
    );
    final result = await _accountUseCase.saveAccount(account);
    if (result.isFailure) {
      emit(AccountUpdateFailed());
      return;
    }
    emit(AccountUpdated());
  }

  Future<void> _deleteAccount(
    DeleteAccount event,
    Emitter<AccountState> emit,
  ) async {
    emit(AccountProcessing());
    final result = await _accountUseCase.deleteAccount(event.id);
    if (result.isFailure) {
      emit(AccountDeletionFailed());
      return;
    }
    emit(AccountDeleted());
  }
}
