import 'package:flutter/material.dart';

class AppFontSize {
  static const double extraSmall = 12;
  static const double small = 14;
  static const double medium = 16;
  static const double large = 24;
  static const double extraLarge = 32;
}

class AppColor {
  static const Color mainDark = Color(0xFF154875);
  static const Color mainLight = Color(0xFF53c8d2);
  static const Color disabledLight = Color(0xFFd9d9d9);
  static const Color disabledDark = Color(0xFF545454);
  static const Color white = Colors.white;
  static const Color green = Colors.green;
  static const Color red = Colors.red;
  static const Color transparent = Colors.transparent;
  static const Color black = Colors.black;
}

class AppFonts {
  static const String appMain = 'YesevaOne';
  static const String appDetail = 'GlacialIndifference';
}

class TextStyles {
  static TextStyle app({
    String fontFamily = AppFonts.appDetail,
    Color fontColor = AppColor.mainDark,
    double fontSize = AppFontSize.medium,
    FontWeight fontWeight = FontWeight.normal,
  }) {
    return TextStyle(
      fontSize: fontSize,
      color: fontColor,
      fontFamily: fontFamily,
      fontWeight: fontWeight,
    );
  }

  static TextStyle appDetail({
    Color fontColor = AppColor.mainDark,
    double fontSize = AppFontSize.medium,
    FontWeight fontWeight = FontWeight.normal,
  }) {
    return app(
      fontSize: fontSize,
      fontColor: fontColor,
      fontFamily: AppFonts.appDetail,
      fontWeight: fontWeight,
    );
  }

  static TextStyle appMain({
    Color fontColor = AppColor.mainDark,
    double fontSize = AppFontSize.medium,
    FontWeight fontWeight = FontWeight.normal,
  }) {
    return app(
      fontSize: fontSize,
      fontColor: fontColor,
      fontFamily: AppFonts.appMain,
      fontWeight: fontWeight,
    );
  }
}
