part of 'transaction_bloc.dart';

abstract class TransactionState extends Equatable {
  const TransactionState();
  @override
  List<Object> get props => [];
}

class TransactionInitial extends TransactionState {}

class TransactionProcessing extends TransactionState {}

class TransactionAdditionFailed extends TransactionState {}

class TransactionUpdateFailed extends TransactionState {}

class TransactionDeletionFailed extends TransactionState {}

class TransactionsRetrievalFailed extends TransactionState {}

class TransactionsFetchingFailed extends TransactionState {}

class TransactionSynchronizationFailed extends TransactionState {}

abstract class TransactionProcessed extends TransactionState {}

class TransactionAdded extends TransactionProcessed {}

class TransactionDeleted extends TransactionProcessed {}

abstract class TransactionsRetrieved extends TransactionState
    implements ListState {
  final List<Transaction> _transactions;

  const TransactionsRetrieved(this._transactions);

  @override
  List<Object> get props => [_transactions];

  @override
  List<ListViewModel> get items =>
      _transactions.map((e) => TransactionViewModel(e)).toList();
}

class UserTransactionsRetrieved extends TransactionsRetrieved
    implements ListState {
  const UserTransactionsRetrieved(super.transactions);
}

class AccountTransactionsRetrieved extends TransactionsRetrieved
    implements ListState {
  const AccountTransactionsRetrieved(super.transactions);
}

class LoanTransactionsRetrieved extends TransactionsRetrieved
    implements ListState {
  const LoanTransactionsRetrieved(super.transactions);
}
