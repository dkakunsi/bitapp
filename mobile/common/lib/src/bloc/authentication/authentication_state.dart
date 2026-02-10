part of 'authentication_bloc.dart';

abstract class AuthenticationState extends Equatable {
  const AuthenticationState();

  @override
  List<Object?> get props => [];
}

class AuthenticationInitial extends AuthenticationState {}

class Authenticating extends AuthenticationState {}

class Authenticated extends AuthenticationState {}

class AuthenticationFailed extends AuthenticationState {}

class Deauthenticating extends AuthenticationState {}

class Deauthenticated extends AuthenticationState {}

class DeauthenticationFailed extends AuthenticationState {}
