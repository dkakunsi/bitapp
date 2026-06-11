import 'package:bitapp/features/configuration/data/configuration_model.dart';
import 'package:bitapp/features/user/domain/user.dart';
import 'package:bitapp/common/util/language.dart';
import 'package:flutter/material.dart';

class Configuration {
  static const String storeId = 'configuration';

  final String id;

  // UI configs
  final Color startColor;
  final Color endColor;
  final String appLogoUrl;
  final String appName;
  final String appMotto;
  final String appVersion;
  final String buildNumber;
  final String contact;
  final String developerName;

  final Language language;

  // Backend configs
  final String backendBaseUrl;
  final bool remoteEnabled;

  // User configs
  final User? user;
  final String? token;

  Configuration({
    this.id = storeId,
    required this.appName,
    required this.appMotto,
    required this.language,
    required this.backendBaseUrl,
    required this.remoteEnabled,
    required this.startColor,
    required this.endColor,
    required this.appLogoUrl,
    required this.appVersion,
    required this.buildNumber,
    required this.contact,
    required this.developerName,
    this.user,
    this.token,
  });

  static Configuration defaultConfiguration = Configuration(
    language: Language.defaultLanguage,
    backendBaseUrl: 'http://localhost:8081',
    remoteEnabled: false,
    startColor: Colors.white,
    endColor: Colors.cyanAccent,
    appLogoUrl: 'assets/images/default.png',
    appName: 'Cortech App',
    appMotto: 'My Cortech App',
    appVersion: '1.0.0',
    buildNumber: '1',
    contact: 'contact@cortech.com',
    developerName: 'Cortech',
  );

  static Configuration from(ConfigurationModel model, User? user) {
    return Configuration(
      id: model.id,
      appName: model.appName,
      appMotto: model.appMotto,
      appLogoUrl: model.appLogoUrl,
      appVersion: model.appVersion,
      buildNumber: model.buildNumber,
      contact: model.contact,
      developerName: model.developerName,
      startColor: model.startColor,
      endColor: model.endColor,
      language: model.language,
      backendBaseUrl: model.backendBaseUrl,
      remoteEnabled: model.remoteEnabled,
      user: user,
      token: model.token,
    );
  }

  ConfigurationModel toModel() {
    return ConfigurationModel(
      id: id,
      appName: appName,
      appMotto: appMotto,
      appLogoUrl: appLogoUrl,
      appVersion: appVersion,
      buildNumber: buildNumber,
      contact: contact,
      developerName: developerName,
      startColor: startColor,
      endColor: endColor,
      language: language,
      backendBaseUrl: backendBaseUrl,
      remoteEnabled: remoteEnabled,
      userModel: user?.toModel(),
      token: token,
    );
  }

  Configuration copyWith({
    String? newAppLogoUrl,
    Color? newStartColor,
    Color? newEndColor,
    Language? newLanguage,
    String? newBackendBaseUrl,
    bool? newRemoteEnabled,
    User? newUser,
    String? newToken,
    String? newAppName,
    String? newAppMotto,
    String? newAppVersion,
    String? newBuildNumber,
    String? newContact,
    String? newDeveloperName,
  }) {
    return Configuration(
      appLogoUrl: newAppLogoUrl ?? appLogoUrl,
      startColor: newStartColor ?? startColor,
      endColor: newEndColor ?? endColor,
      language: newLanguage ?? language,
      backendBaseUrl: newBackendBaseUrl ?? backendBaseUrl,
      remoteEnabled: newRemoteEnabled ?? remoteEnabled,
      user: newUser ?? user,
      token: newToken ?? token,
      appName: newAppName ?? appName,
      appMotto: newAppMotto ?? appMotto,
      appVersion: newAppVersion ?? appVersion,
      buildNumber: newBuildNumber ?? buildNumber,
      contact: newContact ?? contact,
      developerName: newDeveloperName ?? developerName,
    );
  }

  Configuration copyWithoutSession() {
    return Configuration(
      appName: appName,
      appLogoUrl: appLogoUrl,
      startColor: startColor,
      endColor: endColor,
      language: language,
      backendBaseUrl: backendBaseUrl,
      remoteEnabled: remoteEnabled,
      appMotto: appMotto,
      appVersion: appVersion,
      buildNumber: buildNumber,
      contact: contact,
      developerName: developerName,
      user: null,
      token: null,
    );
  }
}
