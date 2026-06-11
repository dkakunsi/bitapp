import 'package:bitapp/features/configuration/presentation/viewmodel/configuration_viewmodel.dart';
import 'package:bitapp/features/user/domain/user.dart';
import 'package:bitapp/features/user/presentation/viewmodel/user_viewmodel.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:bitapp/features/configuration/presentation/bloc/configuration_bloc.dart';

extension SessionExtension on BuildContext {
  ConfigurationViewModel get _configuration =>
      (read<ConfigurationBloc>().state as ConfigurationProcessed).object;

  User? get user => _configuration.user;

  UserViewModel? get userViewModel => _configuration.userViewModel;

  String get userId => _configuration.userId;

  ImageProvider get userImage => _configuration.userImage;

  bool get isLoggedIn => _configuration.isLoggedIn;
}
