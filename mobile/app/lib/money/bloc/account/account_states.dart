part of 'account_bloc.dart';

abstract class AccountState extends Equatable {
  const AccountState();

  @override
  List<Object> get props => [];
}

class AccountInitial extends AccountState {}

class AccountProcessing extends AccountState {}

class AccountsFetchingFailed extends AccountState {}

class AccountsRetrievalFailed extends AccountState {}

class AccountRetrievalFailed extends AccountState {}

class AccountAdditionFailed extends AccountState {}

class AccountUpdateFailed extends AccountState {}

class AccountDeletionFailed extends AccountState {}

abstract class AccountProcessed extends AccountState {}

class AccountAdded extends AccountProcessed {}

class AccountUpdated extends AccountProcessed {}

class AccountDeleted extends AccountProcessed {}

class AccountsRetrieved extends AccountState implements ListState {
  final List<Account> _accounts;

  const AccountsRetrieved(this._accounts);

  @override
  List<Object> get props => [_accounts];

  @override
  List<ListViewModel> get items =>
      _accounts.map((e) => AccountViewModel(e)).toList();
}

class AccountRetrieved extends AccountState implements ObjectState {
  final Account _account;

  const AccountRetrieved(this._account);

  @override
  AccountViewModel get object => AccountViewModel(_account);
}
