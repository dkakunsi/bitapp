import 'package:bitapp/common/presentation/app_style.dart';
import 'package:bitapp/common/presentation/widget/container.dart';
import 'package:bitapp/common/presentation/widget/loading_indicator.dart';
import 'package:bitapp/features/app/presentation/widget/currency_amount.dart';
import 'package:bitapp/features/loan/domain/loan_type.dart';
import 'package:bitapp/l10n/localization_extension.dart';
import 'package:bitapp/features/loan/presentation/bloc/loan_bloc.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class LoanSummary extends StatelessWidget {
  const LoanSummary({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<LoanBloc, LoanState>(
      builder: (builderContext, state) {
        if (state is LoanProcessing) {
          return LoadingIndicator();
        } else if (state is LoanRetrieved) {
          return BoxContainer(
            padding: EdgeInsets.all(0),
            borderWidth: 0,
            borderColor: AppColor.transparent,
            width: double.infinity,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Container(
                  padding: EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Center(
                        child: Column(
                          children: [
                            Icon(
                              state.object.icon,
                              size: 80,
                              color: AppColor.mainDark,
                            ),
                            Text(
                              state.object.title,
                              style: TextStyles.appMain(
                                fontSize: AppFontSize.extraLarge,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                            Row(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                state.object.isPaid
                                    ? Text(
                                      context.locale.paid,
                                      style: TextStyles.appDetail(
                                        fontSize: AppFontSize.large,
                                        fontWeight: FontWeight.bold,
                                        fontColor: state.object.loanColor,
                                      ),
                                    )
                                    : CurrencyAmount(
                                      amount: state.object.amount,
                                      currency: context.locale.idr,
                                      fontSize: getCurrencyFontSize(
                                        state.object.amount,
                                      ),
                                      color: state.object.categoryColor,
                                    ),
                                SizedBox(width: 8),
                                Text(
                                  context.locale.ofLabel,
                                  style: TextStyles.appDetail(
                                    fontSize: AppFontSize.large,
                                  ),
                                ),
                                SizedBox(width: 8),
                                CurrencyAmount(
                                  amount: state.object.principalAmount,
                                  currency: context.locale.idr,
                                  fontSize: getCurrencyFontSize(
                                    state.object.principalAmount,
                                  ),
                                ),
                              ],
                            ),
                            Text(
                              state.object.type == LoanType.debt
                                  ? context.locale.debt
                                  : context.locale.receivable,
                              style: TextStyles.appDetail(
                                fontWeight: FontWeight.bold,
                                fontColor: state.object.categoryColor,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          );
        } else {
          return Container();
        }
      },
    );
  }

  double getCurrencyFontSize(double amount) {
    if (amount < 100000000) {
      return AppFontSize.large;
    } else if (amount < 1000000000) {
      return AppFontSize.medium;
    } else {
      return AppFontSize.small;
    }
  }
}
