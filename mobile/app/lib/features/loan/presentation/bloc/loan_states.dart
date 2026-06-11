part of 'loan_bloc.dart';

abstract class LoanState extends Equatable {
  const LoanState();

  @override
  List<Object> get props => [];
}

class LoanInitial extends LoanState {}

class LoanProcessing extends LoanState {}

class LoansFetchingFailed extends LoanState {}

class LoansRetrievalFailed extends LoanState {}

class LoanRetrievalFailed extends LoanState {}

class LoanAdditionFailed extends LoanState {}

class LoanUpdateFailed extends LoanState {}

class LoanDeletionFailed extends LoanState {}

abstract class LoanProcessed extends LoanState {}

class LoanAdded extends LoanProcessed {}

class LoanUpdated extends LoanProcessed {}

class LoanDeleted extends LoanProcessed {}

class LoansRetrieved extends LoanState implements ListState {
  final List<Loan> _loans;

  const LoansRetrieved(this._loans);

  @override
  List<Object> get props => [_loans];

  @override
  List<ListViewModel> get items =>
      _loans.map((e) => LoanViewModel.fromLoan(e)).toList();
}

class LoanRetrieved extends LoanState implements ObjectState {
  final Loan _loan;

  const LoanRetrieved(this._loan);

  @override
  LoanViewModel get object => LoanViewModel.fromLoan(_loan);
}
