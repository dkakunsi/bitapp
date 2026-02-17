import 'dart:async';

import 'package:app_common/app_common.dart';
import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

part 'user_events.dart';
part 'user_states.dart';

class UserBloc extends Bloc<UserEvent, UserState> {
  final UserUseCase _userUseCase;
  final ConfigurationBloc _configurationBloc;

  UserBloc(this._userUseCase, this._configurationBloc)
    : super(UserInitializing()) {
    on<RegisterUser>(_registerUser);
    on<UpdateUserLanguage>(_updateUserLanguage);
  }

  Future<void> _registerUser(
    RegisterUser event,
    Emitter<UserState> emit,
  ) async {
    emit(UserProcessing());
    final result = await _userUseCase.createUser(
      event.userId,
      event.name,
      event.email,
      event.phone,
      event.photoUrl,
    );
    if (result.isFailure) {
      emit(UserRetrievalFailed());
      return;
    }

    _configurationBloc.add(SetSession(user: result.data));
    emit(UserRetrieved(result.data));
  }

  Future<void> _updateUserLanguage(
    UpdateUserLanguage event,
    Emitter<UserState> emit,
  ) async {
    emit(UserProcessing());
    final result = await _userUseCase.updateLanguage(
      event.userId,
      event.language,
    );
    if (result.isFailure) {
      emit(UserRetrievalFailed());
      return;
    }

    _configurationBloc.add(SetLanguage(language: event.language));
    emit(UserRetrieved(result.data));
  }
}
