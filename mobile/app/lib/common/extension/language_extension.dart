import 'package:bitapp/common/common.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

extension LanguageExtension on BuildContext {
  Language get language =>
      (read<ConfigurationBloc>().state as ConfigurationProcessed)
          .object
          .language;
}
