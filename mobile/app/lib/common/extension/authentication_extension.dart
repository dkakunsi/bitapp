import 'dart:async';

import 'package:bitapp/common/common.dart';
import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';

StreamSubscription? authenticationBlocSubscription;
PeriodicAction? silentLoginAction;

extension AuthenticationExtension on BuildContext {
  void logout({bool isRedirect = true}) {
    read<AuthenticationBloc>().add(Logout());
    if (isRedirect) {
      SchedulerBinding.instance.addPostFrameCallback((_) {
        goNamed(AuthenticationScreen.routeName);
      });
    }
  }

  void registerSilentLoginPeriodicAction() {
    final authenticationBloc = read<AuthenticationBloc>();
    authenticationBloc.stream.listen((state) {
      if (state is Authenticated) {
        silentLoginAction ??= PeriodicAction(
          action: () {
            _silentLogin(authenticationBloc);
            return Future.value();
          },
          durationInMinutes: 50,
        );
        silentLoginAction?.start();
      }
    });
  }

  void _silentLogin(AuthenticationBloc authenticationBloc) {
    authenticationBloc.add(SilentLoginWithGoogle());
  }

  void silentLogin() {
    SchedulerBinding.instance.addPostFrameCallback((_) {
      _silentLogin(read<AuthenticationBloc>());
    });
  }
}
