import 'package:bitapp/common/data/model/store_data.dart';
import 'package:bitapp/features/user/data/user.dart';
import 'package:bitapp/common/util/language.dart';
import 'package:flutter/material.dart';

class Configuration extends StoreData {
  static const String storeId = 'configuration';

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
    super.id = storeId,
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

  static Configuration from(Map<String, dynamic> json) {
    return Configuration(
      appName: json['appName'] as String,
      appMotto: json['appMotto'] as String,
      appLogoUrl: json['appLogoUrl'] as String,
      appVersion: json['appVersion'] as String,
      buildNumber: json['buildNumber'] as String,
      contact: json['contact'] as String,
      developerName: json['developerName'] as String,
      startColor: Color(json['startColor'] as int),
      endColor: Color(json['endColor'] as int),
      language: Language.valueOf(json['language'] as String),
      backendBaseUrl: json['backendBaseUrl'] as String,
      remoteEnabled: json['remoteEnabled'] as bool,
      user:
          json['user'] != null
              ? User.from(json['user'] as Map<String, dynamic>)
              : null,
      token: json['token'] as String?,
    );
  }

  @override
  Map<String, dynamic> toStoreJson() {
    return {
      'appName': appName,
      'appMotto': appMotto,
      'appVersion': appVersion,
      'buildNumber': buildNumber,
      'contact': contact,
      'developerName': developerName,
      'language': language.value,
      'backendBaseUrl': backendBaseUrl,
      'remoteEnabled': remoteEnabled,
      'startColor': startColor.toARGB32(),
      'endColor': endColor.toARGB32(),
      'appLogoUrl': appLogoUrl,
      'user': user?.toStoreJson(),
      'token': token,
    };
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
