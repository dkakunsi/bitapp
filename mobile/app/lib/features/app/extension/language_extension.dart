import 'package:bitapp/common/util/language.dart';
import 'package:bitapp/features/configuration/presentation/bloc/configuration_bloc.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

extension LanguageExtension on BuildContext {
  Language get language =>
      (read<ConfigurationBloc>().state as ConfigurationProcessed)
          .object
          .language;
}
