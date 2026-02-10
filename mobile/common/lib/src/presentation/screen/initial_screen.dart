import 'dart:async';

import 'package:app_common/app_common.dart';
import 'package:flutter/material.dart';

class InitialScreen extends AppScreen {
  static final String routeName = '/';
  final String mainRoute;
  final String authRoute;

  InitialScreen({super.key, required this.mainRoute, required this.authRoute});

  @override
  void onConfigurationChange(BuildContext context, ConfigurationState state) {
    if (state is AppSettingsConfigured) {
      // this is for initial opening of the app
      if (!context.isLoggedIn) {
        Timer(const Duration(seconds: 1), () => context.nextRoute(authRoute));
      } else {
        context.silentLogin();
        context.registerSilentLoginPeriodicAction();
        Timer(const Duration(seconds: 3), () => context.nextRoute(mainRoute));
      }
    } else if (state is ConfigurationProcessed) {
      Timer(const Duration(seconds: 1), () => context.nextRoute(mainRoute));
    }
  }

  @override
  AppScreenContent buildContent(BuildContext context) => InitialScreenContent();
}

class InitialScreenContent extends AppScreenContent {
  InitialScreenContent({super.key});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.only(top: MediaQuery.of(context).size.height * 0.4),
      child: Column(
        children: [
          Image.asset(context.appLogo, height: 46, width: 46),
          const SizedBox(height: 8),
          Text(
            context.appName,
            style: TextStyles.appMain(fontSize: AppFontSize.extraLarge),
          ),
          const SizedBox(height: 8),
          LoadingIndicator(),
        ],
      ),
    );
  }
}
