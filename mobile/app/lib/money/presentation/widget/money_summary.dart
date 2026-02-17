import 'package:bitapp/common/common.dart';
import 'package:bitapp/l10n/localization_extension.dart';
import 'package:bitapp/money/bloc/summary/summary_bloc.dart';
import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class MoneySummary extends StatelessWidget {
  static final double space = 16;

  const MoneySummary({super.key});

  void load(BuildContext context) {
    SchedulerBinding.instance.addPostFrameCallback((_) {
      context.read<SummaryBloc>().add(CalculateSummary(userId: context.userId));
    });
  }

  @override
  Widget build(BuildContext context) {
    return BoxContainer(
      padding: EdgeInsets.all(0),
      borderWidth: 2,
      child: BlocConsumer<SummaryBloc, SummaryState>(
        listener: (context, state) {
          if (state is SummaryCalculationFailed) {
            context.errorMessage(context.locale.summaryCalculationError);
          }
        },
        builder: (context, state) {
          return Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                padding: EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    _MoneySummaryDetail(
                      value: state.object.totalAsset.toCurrencyFormat(context),
                      fontSize: AppFontSize.extraLarge,
                      fontFamily: AppFonts.appMain,
                    ),
                    if (state.object.totalDebt > 0) ...[
                      SizedBox(height: space),
                      _MoneySummaryDetail(
                        value: state.object.totalDebt.toCurrencyFormat(context),
                        icon: Icons.credit_card,
                        label: context.locale.debt,
                        fontColor: AppColor.red,
                      ),
                    ],
                    if (state.object.totalIncome > 0) ...[
                      SizedBox(height: space),
                      _MoneySummaryDetail(
                        value: state.object.totalIncome.toCurrencyFormat(
                          context,
                        ),
                        icon: Icons.monetization_on,
                        label: context.locale.income,
                        fontColor: AppColor.green,
                      ),
                    ],
                  ],
                ),
              ),
              BoxContainer(
                borderRadius: 5,
                borderWidth: 0,
                color: AppColor.mainDark,
                width: double.infinity,
                padding: EdgeInsets.all(16),
                margin: EdgeInsets.all(0),
                child: _MoneySummaryDetail(
                  value: state.object.totalExpense.toCurrencyFormat(context),
                  icon: Icons.payments,
                  label: context.locale.hasBeenUsedThisMonth,
                  fontColor: AppColor.white,
                ),
              ),
            ],
          );
        },
      ),
    );
  }
}

class _MoneySummaryDetail extends StatelessWidget {
  static final double space = 8;
  final IconData? icon;
  final String value;
  final String? label;
  final String fontFamily;
  final Color fontColor;
  final double fontSize;

  const _MoneySummaryDetail({
    required this.value,
    this.icon,
    this.label,
    this.fontFamily = AppFonts.appDetail,
    this.fontColor = AppColor.mainDark,
    this.fontSize = AppFontSize.medium,
  });

  @override
  Widget build(BuildContext context) {
    List<Widget> details = List.empty(growable: true);
    if (icon != null) {
      details.add(Icon(icon!, size: 24, color: fontColor));
      details.add(SizedBox(width: space * 2));
    }

    details.add(_mainValue(context));

    if (label != null) {
      details.add(SizedBox(width: space));
      details.add(
        Text(
          label!,
          style: TextStyles.app(
            fontFamily: fontFamily,
            fontSize: fontSize,
            fontColor: fontColor,
          ),
        ),
      );
    }

    return Row(children: details);
  }

  Row _mainValue(BuildContext context) => Row(
    children: [
      Text(
        context.locale.idr,
        style: TextStyles.app(
          fontFamily: fontFamily,
          fontSize: fontSize,
          fontWeight: FontWeight.bold,
          fontColor: fontColor,
        ),
      ),
      SizedBox(width: space),
      Text(
        value,
        style: TextStyles.app(
          fontFamily: fontFamily,
          fontSize: fontSize,
          fontWeight: FontWeight.bold,
          fontColor: fontColor,
        ),
      ),
    ],
  );
}
