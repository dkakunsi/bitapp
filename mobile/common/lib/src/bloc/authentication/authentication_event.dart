part of 'authentication_bloc.dart';

abstract class AuthenticationEvent extends Equatable {
  @override
  List<Object?> get props => [];
}

class LoginWithGoogle extends AuthenticationEvent {}

class SilentLoginWithGoogle extends AuthenticationEvent {}

class Logout extends AuthenticationEvent {}
