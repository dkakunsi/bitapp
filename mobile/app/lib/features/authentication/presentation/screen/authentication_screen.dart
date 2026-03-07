import 'package:bitapp/features/configuration/extension/configuration_extension.dart';
import 'package:bitapp/common/presentation/app_style.dart';
import 'package:bitapp/common/presentation/widget/container.dart';
import 'package:bitapp/common/presentation/widget/loading_indicator.dart';
import 'package:bitapp/features/app/extension/navigation_extension.dart';
import 'package:bitapp/features/app/presentation/screen/app_screen.dart';
import 'package:bitapp/features/authentication/presentation/bloc/authentication_bloc.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class AuthenticationScreen extends AppScreen {
  static final String routeName = '/authentication';

  final String appRouteName;
  final String signInWithGoogleLabel;
  final String authenticationFailedMessage;

  const AuthenticationScreen({
    super.key,
    required this.appRouteName,
    required this.signInWithGoogleLabel,
    required this.authenticationFailedMessage,
  });

  @override
  AppScreenContent buildContent(BuildContext context) =>
      AuthenticationScreenContent(
        appRouteName: appRouteName,
        signInWithGoogleLabel: signInWithGoogleLabel,
        authenticationFailedMessage: authenticationFailedMessage,
      );

  @override
  Widget build(BuildContext context) {
    return BackgroundContainer(child: buildContent(context));
  }
}

class AuthenticationScreenContent extends AppScreenContent {
  final String appRouteName;
  final String signInWithGoogleLabel;
  final String authenticationFailedMessage;

  const AuthenticationScreenContent({
    super.key,
    required this.appRouteName,
    required this.signInWithGoogleLabel,
    required this.authenticationFailedMessage,
  });

  @override
  Widget build(BuildContext context) {
    return BlocConsumer<AuthenticationBloc, AuthenticationState>(
      bloc: context.read<AuthenticationBloc>(),
      builder: (context, state) {
        if (state is Authenticating || state is Deauthenticating) {
          return LoadingIndicator();
        }
        return _buildContent(context);
      },
      listener: (context, state) {
        if (state is AuthenticationFailed) {
          context.errorMessage(authenticationFailedMessage);
        } else if (state is Authenticated) {
          context.nextRoute(appRouteName);
        }
      },
    );
  }

  Widget _buildContent(BuildContext context) {
    final marginTop = 430;
    final space = MediaQuery.of(context).size.height - marginTop;

    return Center(
      child: Padding(
        padding: const EdgeInsets.all(50),
        child: Column(
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Image.asset(context.appLogo, height: 46, width: 46),
                SizedBox(width: 18),
                Text(context.appName, style: TextStyles.appMain(fontSize: 46)),
              ],
            ),
            SizedBox(height: 4),
            Text(context.appMotto, style: TextStyles.appMain(fontSize: 16)),
            SizedBox(height: space / 2),
            ElevatedButton(
              onPressed:
                  () =>
                      context.read<AuthenticationBloc>().add(LoginWithGoogle()),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Image.asset(
                    'assets/images/google_logo.png',
                    height: 32.0,
                    width: 32.0,
                  ),
                  SizedBox(width: 8),
                  Text(
                    signInWithGoogleLabel,
                    style: TextStyles.appMain(fontSize: 16),
                  ),
                ],
              ),
            ),
            SizedBox(height: space / 2),
            Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Image.asset(
                  'assets/images/cortech_logo.png',
                  height: 70,
                  width: 70,
                ),
                SizedBox(height: 4),
                Text(
                  'CORTECH DIGITAL',
                  style: TextStyles.appMain(fontSize: 18),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
