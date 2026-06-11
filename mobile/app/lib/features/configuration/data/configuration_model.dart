import 'package:bitapp/common/data/model/store_data.dart';
import 'package:bitapp/common/util/language.dart';
import 'package:bitapp/features/configuration/domain/configuration.dart';
import 'package:bitapp/features/user/data/user_model.dart';
import 'package:flutter/material.dart';

class ConfigurationModel implements StoreData {
  @override
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
  final UserModel? userModel;
  final String? token;

  ConfigurationModel({
    required this.id,
    required this.startColor,
    required this.endColor,
    required this.appLogoUrl,
    required this.appName,
    required this.appMotto,
    required this.appVersion,
    required this.buildNumber,
    required this.contact,
    required this.developerName,
    required this.language,
    required this.backendBaseUrl,
    required this.remoteEnabled,
    required this.userModel,
    required this.token,
  });

  static ConfigurationModel from(Map<String, dynamic> json) {
    final userModel = json['user'] != null
        ? UserModel.from(json['user'] as Map<String, dynamic>)
        : null;
    return ConfigurationModel(
      id: json['id'] as String? ?? Configuration.storeId,
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
      userModel: userModel,
      token: json['token'] as String?,
    );
  }

  @override
  Map<String, dynamic> toStoreJson() => {
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
    'user': userModel?.toStoreJson(),
    'token': token,
  };

  ConfigurationModel copyWith({
    String? newAppLogoUrl,
    Color? newStartColor,
    Color? newEndColor,
    Language? newLanguage,
    String? newBackendBaseUrl,
    bool? newRemoteEnabled,
    UserModel? newUserModel,
    String? newToken,
    String? newAppName,
    String? newAppMotto,
    String? newAppVersion,
    String? newBuildNumber,
    String? newContact,
    String? newDeveloperName,
  }) => ConfigurationModel(
    id: id,
    appLogoUrl: newAppLogoUrl ?? appLogoUrl,
    startColor: newStartColor ?? startColor,
    endColor: newEndColor ?? endColor,
    language: newLanguage ?? language,
    backendBaseUrl: newBackendBaseUrl ?? backendBaseUrl,
    remoteEnabled: newRemoteEnabled ?? remoteEnabled,
    userModel: newUserModel ?? userModel,
    token: newToken ?? token,
    appName: newAppName ?? appName,
    appMotto: newAppMotto ?? appMotto,
    appVersion: newAppVersion ?? appVersion,
    buildNumber: newBuildNumber ?? buildNumber,
    contact: newContact ?? contact,
    developerName: newDeveloperName ?? developerName,
  );

  ConfigurationModel copyWithoutSession() => ConfigurationModel(
    id: id,
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
    userModel: null,
    token: null,
  );
}
