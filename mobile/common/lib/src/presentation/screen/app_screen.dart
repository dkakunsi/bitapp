import 'package:app_common/app_common.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

abstract class AppScreen extends StatelessWidget {
  final BlocListener Function(BuildContext, Widget)? listener;

  const AppScreen({super.key, this.listener});

  String? get moduleName => null;

  String? get backRouteName => null;

  void onConfigurationChange(BuildContext context, ConfigurationState state) {}

  AppScreenContent buildContent(BuildContext context);

  @override
  Widget build(BuildContext context) {
    return BlocConsumer<ConfigurationBloc, ConfigurationState>(
      builder: (builderContext, state) {
        if (state is ConfigurationProcessed) {
          return (listener != null)
              ? listener!(builderContext, _buildScreen(builderContext, state))
              : _buildScreen(builderContext, state);
        }
        return LoadingIndicator();
      },
      listener: onConfigurationChange,
    );
  }

  Widget _buildScreen(BuildContext context, ConfigurationProcessed state) {
    final screenContent = buildContent(context);

    return Container(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [state.object.startColor, state.object.endColor],
        ),
      ),
      child: Scaffold(
        appBar:
            moduleName != null
                ? AppBar(
                  backgroundColor: Colors.white10,
                  elevation: 0,
                  title: ModuleName(
                    moduleName: moduleName!,
                    appLogo: state.object.appLogo,
                  ),
                  actions: [
                    if (backRouteName != null)
                      IconButton(
                        icon: const Icon(Icons.arrow_back),
                        iconSize: 40,
                        color: AppColor.mainDark,
                        onPressed: () {
                          context.nextRoute(backRouteName!);
                        },
                      ),
                    Padding(
                      padding: EdgeInsets.only(right: 12, bottom: 8),
                      child: ProfilePicture(
                        userSettingsRouteName: UserScreen.routeName,
                        userImage: state.object.userImage,
                      ),
                    ),
                  ],
                )
                : null,
        backgroundColor: Colors.transparent,
        body: Padding(
          padding: EdgeInsets.only(top: 32, left: 12, right: 12),
          child: RefreshIndicator(
            child: SingleChildScrollView(child: screenContent),
            onRefresh: () => screenContent.reload(context),
          ),
        ),
        bottomNavigationBar: screenContent.buildNavigationBar(context),
        floatingActionButton: screenContent.buildFloatingActionButton(context),
      ),
    );
  }
}

abstract class AppScreenContent extends StatelessWidget {
  const AppScreenContent({super.key});

  Widget? buildNavigationBar(BuildContext context) => null;

  Widget? buildFloatingActionButton(BuildContext context) => null;

  Future<void> reload(BuildContext context) => Future.value();
}
