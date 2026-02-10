import 'package:app_common/app_common.dart';
import 'package:bitapp/l10n/localization_extension.dart';
import 'package:bitapp/money/bloc/account/account_bloc.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

// use stateful widget to manage the state of the account summary

class AccountSummary extends StatelessWidget {
  const AccountSummary({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<AccountBloc, AccountState>(
      builder: (builderContext, state) {
        if (state is AccountProcessing) {
          return LoadingIndicator();
        } else if (state is AccountRetrieved) {
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
                              state.object.name,
                              style: TextStyles.appMain(
                                fontSize: AppFontSize.extraLarge,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                            Row(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                Text(
                                  context.locale.idr,
                                  style: TextStyles.appMain(
                                    fontSize: AppFontSize.large,
                                    fontColor: AppColor.green,
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                                SizedBox(width: 8),
                                Text(
                                  state.object.balance.toCurrencyFormat(
                                    context,
                                  ),
                                  style: TextStyles.appMain(
                                    fontSize: AppFontSize.large,
                                    fontColor: AppColor.green,
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                              ],
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
}
