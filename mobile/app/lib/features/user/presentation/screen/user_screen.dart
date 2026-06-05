import 'package:bitapp/common/presentation/app_style.dart';
import 'package:bitapp/common/util/language.dart';
import 'package:bitapp/features/app/presentation/screen/app_screen.dart';
import 'package:bitapp/features/app/presentation/widget/module_list.dart';
import 'package:bitapp/features/app/presentation/widget/settings.dart';
import 'package:bitapp/features/authentication/extension/session_extension.dart';
import 'package:bitapp/features/user/presentation/widget/user_info.dart';
import 'package:flutter/material.dart';

class UserScreen extends AppScreen {
  static final String routeName = '/user';
  final List<ModuleConfig> modules;
  final String appInfoLabel;
  final String appVersionLabel;
  final String contactUsLabel;
  final String developedByLabel;
  final String moduleLabel;
  final String yourAppsLabel;
  final String yourSettingsLabel;
  final String logoutLabel;
  final String languageLabel;
  final String synchronizeLabel;
  final void Function(BuildContext)? onSynchronize;
  final Map<Language, String> availableLanguages;

  const UserScreen({
    super.key,
    required this.modules,
    required this.moduleLabel,
    required this.yourAppsLabel,
    required this.yourSettingsLabel,
    required this.logoutLabel,
    required this.languageLabel,
    required this.availableLanguages,
    required this.appInfoLabel,
    required this.contactUsLabel,
    required this.developedByLabel,
    required this.appVersionLabel,
    required this.synchronizeLabel,
    this.onSynchronize,
  });

  @override
  String get moduleName => moduleLabel;

  @override
  AppScreenContent buildContent(BuildContext context) => UserScreenContent(
    modules: modules,
    yourAppsLabel: yourAppsLabel,
    yourSettingsLabel: yourSettingsLabel,
    logoutLabel: logoutLabel,
    languageLabel: languageLabel,
    availableLanguages: availableLanguages,
    appInfoLabel: appInfoLabel,
    contactUsLabel: contactUsLabel,
    developedByLabel: developedByLabel,
    appVersionLabel: appVersionLabel,
    synchronizeLabel: synchronizeLabel,
    onSynchronize: onSynchronize,
  );
}

class UserScreenContent extends AppScreenContent {
  final List<ModuleConfig> modules;
  final String appInfoLabel;
  final String yourAppsLabel;
  final String yourSettingsLabel;
  final String appVersionLabel;
  final String contactUsLabel;
  final String developedByLabel;
  final String logoutLabel;
  final String languageLabel;
  final String synchronizeLabel;
  final void Function(BuildContext)? onSynchronize;
  final Map<Language, String> availableLanguages;

  const UserScreenContent({
    super.key,
    required this.appInfoLabel,
    required this.modules,
    required this.yourAppsLabel,
    required this.yourSettingsLabel,
    required this.logoutLabel,
    required this.contactUsLabel,
    required this.developedByLabel,
    required this.languageLabel,
    required this.availableLanguages,
    required this.appVersionLabel,
    required this.synchronizeLabel,
    this.onSynchronize,
  });

  @override
  Widget build(BuildContext context) {
    final user = context.userViewModel;
    if (user == null) {
      return Container();
    }
    return Column(
      children: <Widget>[
        UserInfo(user: user, logoutLabel: logoutLabel),
        SizedBox(height: 24),
        Container(
          alignment: Alignment.centerLeft,
          child: Text(
            yourAppsLabel,
            style: TextStyles.appDetail(fontSize: AppFontSize.medium),
          ),
        ),
        SizedBox(height: 8),
        ModuleList(modules: modules),
        SizedBox(height: 24),
        Container(
          alignment: Alignment.centerLeft,
          child: Text(yourSettingsLabel, style: TextStyles.appDetail()),
        ),
        SizedBox(height: 8),
        Settings(
          user: user,
          languageLabel: languageLabel,
          appVersionLabel: appVersionLabel,
          appInfoLabel: appInfoLabel,
          contactUsLabel: contactUsLabel,
          developedByLabel: developedByLabel,
          availableLanguages: availableLanguages,
          synchronizeLabel: synchronizeLabel,
          onSynchronize: onSynchronize,
        ),
      ],
    );
  }
}
