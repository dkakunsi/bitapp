import 'package:bitapp/common/presentation/viewmodel/viewmodel.dart';
import 'package:bitapp/common/util/language.dart';
import 'package:bitapp/features/configuration/data/configuration.dart';
import 'package:bitapp/features/user/presentation/viewmodel/user_viewmodel.dart';
import 'package:flutter/material.dart';

class ConfigurationViewModel extends ViewModel {
  static const String _defaultImageUrl = 'assets/images/default.png';
  final Configuration _configuration;

  ConfigurationViewModel(this._configuration);

  Language get language => _configuration.language;

  Color get startColor => _configuration.startColor;

  Color get endColor => _configuration.endColor;

  // TODO: rename, to indicate that user was logged in before
  bool get isLoggedIn => _configuration.token != null;

  bool get isRemoteEnabled => _configuration.remoteEnabled;

  String get appLogo => _configuration.appLogoUrl;

  String get appName => _configuration.appName;

  String get appMotto => _configuration.appMotto;

  String get appVersion => _configuration.appVersion;

  String get buildNumber => _configuration.buildNumber;

  String get contact => _configuration.contact;

  String get developerName => _configuration.developerName;

  ImageProvider get userImage =>
      _configuration.user?.photoUrl != null
          ? NetworkImage(_configuration.user!.photoUrl!)
          : const AssetImage(_defaultImageUrl) as ImageProvider;

  String get userId => _configuration.user?.id ?? '';

  UserViewModel? get user =>
      _configuration.user != null ? UserViewModel(_configuration.user!) : null;
}
