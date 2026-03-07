import 'package:bitapp/common/presentation/app_style.dart';
import 'package:bitapp/common/presentation/widget/loading_indicator.dart';
import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
import 'package:go_router/go_router.dart';

extension NavigationExtension on BuildContext {
  void nextRoute(String nextRouteName, {Object? argument}) {
    // SchedulerBinding.instance.addPostFrameCallback((_) {
    //   goNamed(nextRouteName, extra: argument);
    // });
    goNamed(nextRouteName, extra: argument);
  }

  void errorMessage(String message, {bool pop = false}) {
    _showMessage(
      message,
      backgroundColor: AppColor.red,
      textColor: AppColor.white,
      pop: pop,
    );
  }

  void successMessage(String message, {bool pop = false}) {
    _showMessage(
      message,
      backgroundColor: AppColor.green,
      textColor: AppColor.white,
      pop: pop,
    );
  }

  void infoMessage(String message, {bool pop = false}) {
    _showMessage(
      message,
      backgroundColor: AppColor.white,
      textColor: AppColor.mainDark,
      pop: pop,
    );
  }

  void _showMessage(
    String message, {
    Color backgroundColor = AppColor.white,
    Color textColor = AppColor.mainDark,
    Duration duration = const Duration(seconds: 3),
    pop = false,
  }) {
    if (pop) {
      Navigator.of(this).pop();
    }
    SchedulerBinding.instance.addPostFrameCallback((_) {
      ScaffoldMessenger.of(this).showSnackBar(
        SnackBar(
          dismissDirection: DismissDirection.up,
          duration: duration,
          behavior: SnackBarBehavior.floating,
          backgroundColor: backgroundColor,
          margin: EdgeInsets.only(
            bottom: MediaQuery.of(this).size.height * 0.65,
            left: 16,
            right: 16,
          ),
          content: Center(
            child: Text(
              message,
              style: TextStyles.appDetail(fontColor: textColor),
            ),
          ),
        ),
      );
    });
  }

  LoadingIndicator showLoadingIndicator() => LoadingIndicator();
}
