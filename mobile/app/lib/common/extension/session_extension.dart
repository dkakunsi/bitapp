import 'package:bitapp/common/common.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

extension SessionExtension on BuildContext {
  ConfigurationViewModel get _configuration =>
      (read<ConfigurationBloc>().state as ConfigurationProcessed).object;

  UserViewModel? get user => _configuration.user;

  String get userId => _configuration.userId;

  ImageProvider get userImage => _configuration.userImage;

  bool get isLoggedIn => _configuration.isLoggedIn;
}
