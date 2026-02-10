import 'package:app_common/app_common.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

extension ConfigurationExtension on BuildContext {
  ConfigurationViewModel get _configuration =>
      (read<ConfigurationBloc>().state as ConfigurationProcessed).object;

  String get appLogo => _configuration.appLogo;

  String get appName => _configuration.appName;

  String get appMotto => _configuration.appMotto;

  String get appVersion => _configuration.appVersion;

  String get buildNumber => _configuration.buildNumber;

  String get contact => _configuration.contact;

  String get developerName => _configuration.developerName;

  bool get isRemoteEnabled => _configuration.isRemoteEnabled;
}
