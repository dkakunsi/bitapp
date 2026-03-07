import 'package:bitapp/common/presentation/app_style.dart';
import 'package:bitapp/common/presentation/widget/container.dart';
import 'package:bitapp/features/app/extension/navigation_extension.dart';
import 'package:flutter/material.dart';

class AppButton extends StatelessWidget {
  final String label;
  final Color color;
  final Color borderColor;
  final Color textColor;
  final double borderWidth;
  final double _height;
  final double _width;
  final Icon? icon;
  final bool sideIcon;
  final double fontSize;
  final bool visible;
  final EdgeInsetsGeometry padding;
  final MainAxisAlignment mainAxisAlignment;
  final CrossAxisAlignment crossAxisAlignment;

  final void Function(BuildContext context) onTap;

  const AppButton({
    super.key,
    required this.label,
    required this.onTap,
    this.color = AppColor.transparent,
    this.borderColor = AppColor.transparent,
    this.textColor = AppColor.mainDark,
    double height = 30,
    double width = 90,
    this.borderWidth = 0,
    this.fontSize = AppFontSize.medium,
    this.icon,
    this.sideIcon = false,
    this.visible = true,
    this.padding = const EdgeInsets.all(0),
    this.mainAxisAlignment = MainAxisAlignment.center,
    this.crossAxisAlignment = CrossAxisAlignment.center,
  }) : _height = icon != null && !sideIcon ? height + 25 : height,
       _width = icon != null && sideIcon ? width + 25 : width;

  @override
  Widget build(BuildContext context) {
    return Visibility(
      visible: visible,
      child: Padding(
        padding: padding,
        child: InkWell(
          onTap: () => onTap(context),
          child: BoxContainer(
            padding: EdgeInsets.all(2),
            borderWidth: borderWidth,
            color: color,
            borderColor: borderColor,
            width: _width,
            height: _height,
            child: _buildButtonIconAndText(context),
          ),
        ),
      ),
    );
  }

  Widget _buildButtonIconAndText(BuildContext context) {
    return sideIcon
        ? Row(
          mainAxisAlignment: mainAxisAlignment,
          crossAxisAlignment: crossAxisAlignment,
          children: [
            if (icon != null) ...[icon!],
            SizedBox(width: 8),
            Text(
              label,
              style: TextStyles.appDetail(
                fontColor: textColor,
                fontSize: fontSize,
              ),
            ),
          ],
        )
        : Column(
          mainAxisAlignment: mainAxisAlignment,
          crossAxisAlignment: crossAxisAlignment,
          children: [
            if (icon != null) ...[icon!],
            Text(
              label,
              style: TextStyles.appDetail(
                fontColor: textColor,
                fontSize: fontSize,
              ),
            ),
          ],
        );
  }
}

class ModalNavigationButton extends AppButton {
  final Widget modalContent;
  final double modalHeight;

  ModalNavigationButton({
    super.key,
    required String title,
    required this.modalContent,
    this.modalHeight = 200,
  }) : super(
         label: title,
         onTap: (context) {
           showModalBottomSheet(
             context: context,
             builder: (BuildContext context) {
               return Row(
                 children: [
                   Expanded(
                     child: SizedBox(height: modalHeight, child: modalContent),
                   ),
                 ],
               );
             },
           );
         },
         width: 200,
         crossAxisAlignment: CrossAxisAlignment.start,
       );
}

class ModuleNavigationButton extends AppButton {
  final String routeName;

  final notAvailableSnackbar = SnackBar(
    content: Text('Sorry, this feature is not available'),
  );

  ModuleNavigationButton({
    super.key,
    required String title,
    required this.routeName,
    Function? onClick,
  }) : super(
         label: title,
         onTap: (context) {
           if (onClick != null) {
             onClick();
           } else {
             context.nextRoute(routeName);
           }
         },
         width: 200,
         crossAxisAlignment: CrossAxisAlignment.start,
       );
}
