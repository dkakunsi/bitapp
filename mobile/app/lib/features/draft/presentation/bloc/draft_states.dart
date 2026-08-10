part of 'draft_bloc.dart';

abstract class DraftState {}

class DraftInitial extends DraftState {}

class DraftProcessing extends DraftState {}

class DraftFailed extends DraftState {
  final Exception? exception;

  DraftFailed({this.exception});
}

class DraftCreated extends DraftState {
  final Draft draft;

  DraftCreated({required this.draft});
}

class DraftConfirmed extends DraftState {
  final String draftId;

  DraftConfirmed({required this.draftId});
}