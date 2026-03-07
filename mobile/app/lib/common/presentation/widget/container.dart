import 'package:bitapp/common/presentation/app_style.dart';
import 'package:flutter/material.dart';

class BoxContainer extends Container {
  BoxContainer({
    super.key,
    required Widget child,
    super.padding = const EdgeInsets.all(8),
    super.margin = const EdgeInsets.all(2),
    super.height,
    super.width,
    double borderWidth = 1,
    double borderRadius = 10,
    Color color = AppColor.transparent,
    Color borderColor = AppColor.mainDark,
  }) : super(
         decoration: BoxDecoration(
           border: Border.all(color: borderColor, width: borderWidth),
           borderRadius: BorderRadius.circular(borderRadius),
           color: color,
         ),
         child: child,
       );
}

class BackgroundContainer extends Container {
  BackgroundContainer({super.key, required Widget child})
    : super(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [Colors.white, Colors.cyanAccent],
          ),
        ),
        child: Scaffold(
          backgroundColor: Colors.transparent,
          body: Padding(
            padding: const EdgeInsets.only(top: 80, left: 12, right: 12),
            child: child,
          ),
        ),
      );
}
