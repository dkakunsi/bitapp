part of 'transaction_bloc.dart';

abstract class TransactionEvent extends Equatable {
  const TransactionEvent();
  @override
  List<Object> get props => [];
}

class FetchTransactions extends TransactionEvent {
  final String userId;

  const FetchTransactions({required this.userId});

  @override
  List<Object> get props => [userId];
}

class GetTransactions extends TransactionEvent {
  final String userId;

  const GetTransactions({required this.userId});

  @override
  List<Object> get props => [userId];
}

class GetAccountTransactions extends TransactionEvent {
  final String accountId;

  const GetAccountTransactions({required this.accountId});

  @override
  List<Object> get props => [accountId];
}

class GetLoanTransactions extends TransactionEvent {
  final String loanId;

  const GetLoanTransactions({required this.loanId});

  @override
  List<Object> get props => [loanId];
}

class AddTransaction extends TransactionEvent {
  final String userId;
  final String title;
  final String description;
  final double amount;
  final DateTime date;
  final TimeOfDay time;
  final TransactionType transactionType;
  final TransactionCategory category;
  final Account? sourceAccount;
  final Account? destinationAccount;
  final Loan? loan;

  const AddTransaction({
    required this.userId,
    required this.title,
    required this.description,
    required this.amount,
    required this.date,
    required this.time,
    required this.transactionType,
    required this.category,
    this.sourceAccount,
    this.destinationAccount,
    this.loan,
  });

  @override
  List<Object> get props => [
    userId,
    title,
    description,
    amount,
    date,
    time,
    transactionType,
    category,
  ];
}

class DeleteTransaction extends TransactionEvent {
  final String id;

  const DeleteTransaction({required this.id});

  @override
  List<Object> get props => [id];
}
