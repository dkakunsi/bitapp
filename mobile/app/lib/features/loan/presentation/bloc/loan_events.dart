part of 'loan_bloc.dart';

abstract class LoanEvent extends Equatable {
  const LoanEvent();

  @override
  List<Object> get props => [];
}

class FetchLoans extends LoanEvent {
  final String userId;

  const FetchLoans({required this.userId});
}

class GetLoans extends LoanEvent {
  final String userId;

  const GetLoans({required this.userId});

  @override
  List<Object> get props => [userId];
}

class GetLoan extends LoanEvent {
  final String id;

  const GetLoan({required this.id});
}

class AddLoan extends LoanEvent {
  final String userId;
  final String title;
  final DateTime date;
  final TimeOfDay time;
  final LoanType type;
  final double amount;
  final String? description;
  final String? partyName;

  const AddLoan({
    required this.userId,
    required this.title,
    required this.amount,
    required this.date,
    required this.time,
    required this.type,
    this.description,
    this.partyName,
  });

  @override
  List<Object> get props => [userId, title, date, time, type, amount];
}

class UpdateLoan extends LoanEvent {
  final String id;
  final String title;
  final String? description;
  final String? partyName;
  final double amount;
  final DateTime date;
  final TimeOfDay time;
  final LoanType type;
  final String userId;

  const UpdateLoan({
    required this.id,
    required this.title,
    required this.amount,
    required this.date,
    required this.time,
    required this.type,
    required this.userId,
    this.description,
    this.partyName,
  });

  @override
  List<Object> get props => [id, title, amount, date, time, type, userId];
}

class DeleteLoan extends LoanEvent {
  final String id;

  const DeleteLoan({required this.id});

  @override
  List<Object> get props => [id];
}
