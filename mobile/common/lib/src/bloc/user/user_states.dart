part of 'user_bloc.dart';

class UserState extends Equatable {
  const UserState();

  @override
  List<Object?> get props => [];
}

class UserInitializing extends UserState {}

class UserProcessing extends UserState {}

class UserRetrieved extends UserState implements ObjectState {
  final User _user;

  const UserRetrieved(this._user);

  @override
  List<Object?> get props => [_user];

  @override
  UserViewModel get object => UserViewModel(_user);
}

class UserRetrievalFailed extends UserState {}
