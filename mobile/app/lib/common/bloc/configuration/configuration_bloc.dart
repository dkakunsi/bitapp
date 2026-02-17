import 'dart:async';

import 'package:bitapp/common/common.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

part 'configuration_events.dart';
part 'configuration_state.dart';

class ConfigurationBloc extends Bloc<ConfigurationEvent, ConfigurationState> {
  final ConfigurationUseCase configurationUseCase;

  ConfigurationBloc(this.configurationUseCase) : super(ConfigurationInitial()) {
    on<SetAppSettings>(_setAppSettings);
    on<SetSession>(_setSession);
    on<SetToken>(_setToken);
    on<SetLanguage>(_setLanguage);
    on<DeleteSession>(_deleteSession);
  }

  Future<void> _deleteSession(
    DeleteSession event,
    Emitter<ConfigurationState> emit,
  ) async {
    final result = await configurationUseCase.clearSession();
    emit(SessionConfigured(result.data));
  }

  Future<void> _setAppSettings(
    SetAppSettings event,
    Emitter<ConfigurationState> emit,
  ) async {
    final updateResult = await configurationUseCase.updateConfiguration(
      backendBaseUrl: event.baseUrl,
      remoteEnabled: event.remoteEnabled,
      startColor: event.startColor,
      endColor: event.endColor,
      appLogoUrl: event.appLogoUrl,
      appName: event.appName,
      appMotto: event.appMotto,
      appVersion: event.appVersion,
      buildNumber: event.buildNumber,
      contact: event.contact,
      developerName: event.developerName,
    );
    emit(AppSettingsConfigured(updateResult.data));
  }

  Future<void> _setSession(
    SetSession event,
    Emitter<ConfigurationState> emit,
  ) async {
    final updateResult = await configurationUseCase.updateConfiguration(
      user: event.user,
      language: event.user.language,
    );
    emit(SessionConfigured(updateResult.data));
  }

  Future<void> _setToken(
    SetToken event,
    Emitter<ConfigurationState> emit,
  ) async {
    final updateResult = await configurationUseCase.updateConfiguration(
      token: event.token,
    );
    emit(TokenConfigured(updateResult.data));
  }

  Future<void> _setLanguage(
    SetLanguage event,
    Emitter<ConfigurationState> emit,
  ) async {
    final updateResult = await configurationUseCase.updateConfiguration(
      language: event.language,
    );
    emit(LanguageConfigured(updateResult.data));
  }
}
