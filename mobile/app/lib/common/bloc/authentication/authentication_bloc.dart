import 'dart:async';

import 'package:bitapp/common/common.dart';
import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

part 'authentication_event.dart';
part 'authentication_state.dart';

class AuthenticationBloc
    extends Bloc<AuthenticationEvent, AuthenticationState> {
  final AuthenticationUseCase _authenticationUseCase;
  final ConfigurationBloc _configurationBloc;
  final UserBloc _userBloc;

  AuthenticationBloc(
    this._authenticationUseCase,
    this._configurationBloc,
    this._userBloc,
  ) : super(AuthenticationInitial()) {
    on<LoginWithGoogle>(_loginWithGoogle);
    on<SilentLoginWithGoogle>(_silentLoginWithGoogle);
    on<Logout>(_logout);
  }

  Future<void> _loginWithGoogle(
    LoginWithGoogle event,
    Emitter<AuthenticationState> emit,
  ) async {
    emit(Authenticating());

    final result = await _authenticationUseCase.login();
    if (result.isFailure) {
      emit(AuthenticationFailed());
      return;
    }

    final session = result.data;
    _configurationBloc.add(SetToken(token: session.token));
    _userBloc.add(
      RegisterUser(
        userId: session.email,
        name: session.name,
        email: session.email,
        phone: session.phone,
        photoUrl: session.photoUrl,
      ),
    );
    emit(Authenticated());
  }

  Future<void> _silentLoginWithGoogle(
    SilentLoginWithGoogle event,
    Emitter<AuthenticationState> emit,
  ) async {
    final result = await _authenticationUseCase.silentLogin();
    if (result.isFailure) {
      emit(AuthenticationFailed());
      return;
    }

    final session = result.data;
    _configurationBloc.add(SetToken(token: session.token));
    emit(Authenticated());
  }

  Future<void> _logout(Logout event, Emitter<AuthenticationState> emit) async {
    emit(Deauthenticating());
    final logoutResult = await _authenticationUseCase.logout();
    if (logoutResult.isFailure) {
      emit(DeauthenticationFailed());
      return;
    }

    _configurationBloc.add(DeleteSession());
    emit(Deauthenticated());
  }
}
