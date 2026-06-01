part of 'configuration_bloc.dart';

abstract class ConfigurationEvent {
  const ConfigurationEvent();
}

class ConfigurationInitialEvent extends ConfigurationEvent {
  const ConfigurationInitialEvent();
}

class SetAppSettings extends ConfigurationEvent {
  final String? baseUrl;
  final bool? remoteEnabled;
  final Color? startColor;
  final Color? endColor;
  final String? appLogoUrl;
  final String? appName;
  final String? appMotto;
  final String? appVersion;
  final String? buildNumber;
  final String? contact;
  final String? developerName;

  const SetAppSettings({
    this.baseUrl,
    this.remoteEnabled,
    this.startColor,
    this.endColor,
    this.appLogoUrl,
    this.appName,
    this.appMotto,
    this.appVersion,
    this.buildNumber,
    this.contact,
    this.developerName,
  });
}

class SetSession extends ConfigurationEvent {
  final User user;
  final String token;

  const SetSession({required this.user, required this.token});
}

class SetLanguage extends ConfigurationEvent {
  final Language? language;

  const SetLanguage({this.language});
}

class DeleteSession extends ConfigurationEvent {}
