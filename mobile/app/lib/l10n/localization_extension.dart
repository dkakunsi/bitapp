import 'package:bitapp/common/util/language.dart';
import 'package:bitapp/features/app/extension/language_extension.dart';
import 'package:bitapp/l10n/app_localizations.dart';
import 'package:bitapp/l10n/app_localizations_en.dart';
import 'package:bitapp/l10n/app_localizations_id.dart';
import 'package:flutter/material.dart';

extension LocalizationExtension on BuildContext {
  AppLocalizations get locale {
    return language == Language.en
        ? AppLocalizationsEn()
        : AppLocalizationsId();
    // return AppLocalizations.of(this)!;
  }
}
