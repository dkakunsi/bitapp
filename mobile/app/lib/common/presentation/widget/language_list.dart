import 'dart:async';

import 'package:bitapp/common/common.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class LanguageList extends StatelessWidget {
  final Map<Language, String> availableLanguages;

  const LanguageList({super.key, required this.availableLanguages});

  @override
  Widget build(BuildContext context) {
    final userBloc = context.read<UserBloc>();
    return Padding(
      padding: EdgeInsets.only(top: 16),
      child: ListView.builder(
        itemCount: availableLanguages.length,
        itemBuilder: (context, index) {
          return ListTile(
            title: Text(
              availableLanguages[availableLanguages.keys.elementAt(index)]!,
            ),
            textColor:
                context.language == Language.values[index]
                    ? AppColor.mainDark
                    : AppColor.disabledDark,
            tileColor:
                context.language == Language.values[index]
                    ? AppColor.mainLight
                    : AppColor.transparent,
            onTap: () {
              // Extract userId, so we don't need to access context from timer callback.
              // Accessing from context will depends on the widget tree.
              final userId = context.userId;
              if (context.language != Language.values[index]) {
                context.nextRoute(
                  InitialScreen.routeName,
                  argument: UserScreen.routeName,
                );
                // Wait for 1 second to allow the initial screen to be ready to listen to state changes.
                Timer(
                  const Duration(seconds: 1),
                  () => userBloc.add(
                    UpdateUserLanguage(
                      userId: userId,
                      language: availableLanguages.keys.elementAt(index),
                    ),
                  ),
                );
              }
            },
          );
        },
      ),
    );
  }
}
