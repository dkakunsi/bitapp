import 'package:bitapp/common/presentation/app_style.dart';
import 'package:bitapp/common/presentation/widget/app_button.dart';
import 'package:bitapp/common/presentation/widget/container.dart';
import 'package:bitapp/common/util/language.dart';
import 'package:bitapp/features/app/presentation/widget/language_list.dart';
import 'package:bitapp/features/configuration/extension/configuration_extension.dart';
import 'package:bitapp/features/user/presentation/viewmodel/user_viewmodel.dart';
import 'package:flutter/material.dart';

class Settings extends StatelessWidget {
  final UserViewModel? user;

  final String appInfoLabel;
  final String appVersionLabel;
  final String contactUsLabel;
  final String developedByLabel;
  final String languageLabel;
  final String synchronizeLabel;
  final void Function(BuildContext)? onSynchronize;
  final Map<Language, String> availableLanguages;

  const Settings({
    super.key,
    this.user,
    required this.appInfoLabel,
    required this.appVersionLabel,
    required this.contactUsLabel,
    required this.developedByLabel,
    required this.languageLabel,
    required this.availableLanguages,
    required this.synchronizeLabel,
    this.onSynchronize,
  });

  @override
  Widget build(BuildContext context) {
    return BoxContainer(
      child: Row(
        children: [
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              ModalNavigationButton(
                title: languageLabel,
                modalContent: Container(
                  padding: const EdgeInsets.only(top: 16),
                  child: LanguageList(availableLanguages: availableLanguages),
                ),
              ),
              SizedBox(height: 12),
              ModalNavigationButton(
                title: appInfoLabel,
                modalContent: Container(
                  padding: const EdgeInsets.only(
                    bottom: 16,
                    left: 16,
                    right: 16,
                  ),
                  alignment: Alignment.center,
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(
                        context.appName,
                        style: TextStyles.appMain(fontSize: 24),
                      ),
                      SizedBox(height: 24),
                      Text(
                        '$appVersionLabel ${context.appVersion} (${context.buildNumber})',
                        style: TextStyles.appDetail(),
                      ),
                      SizedBox(height: 8),
                      Text(
                        '$developedByLabel ${context.developerName}',
                        style: TextStyles.appDetail(),
                      ),
                      SizedBox(height: 8),
                      Text(
                        '$contactUsLabel at ${context.contact}',
                        style: TextStyles.appDetail(),
                      ),
                    ],
                  ),
                ),
              ),
              SizedBox(height: 12),
              AppButton(
                label: synchronizeLabel,
                crossAxisAlignment: CrossAxisAlignment.start,
                onTap: onSynchronize ?? (c) {},
                visible: context.isRemoteEnabled,
              ),
            ],
          ),
        ],
      ),
    );
  }
}
