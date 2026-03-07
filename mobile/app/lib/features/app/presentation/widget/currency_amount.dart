import 'package:bitapp/common/presentation/app_style.dart';
import 'package:bitapp/common/util/formatter.dart';
import 'package:flutter/material.dart';

class CurrencyAmount extends StatelessWidget {
  final String currency;
  final double amount;
  final Color color;
  final double fontSize;
  final FontWeight fontWeight;

  const CurrencyAmount({
    super.key,
    required this.amount,
    required this.currency,
    this.color = AppColor.mainDark,
    this.fontSize = AppFontSize.small,
    this.fontWeight = FontWeight.normal,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Text(
          currency,
          style: TextStyles.appDetail(
            fontSize: fontSize,
            fontWeight: fontWeight,
            fontColor: color,
          ),
        ),
        SizedBox(width: 2),
        Text(
          amount.toCurrencyFormat(context),
          style: TextStyles.appDetail(
            fontSize: fontSize,
            fontWeight: fontWeight,
            fontColor: color,
          ),
        ),
      ],
    );
  }
}
