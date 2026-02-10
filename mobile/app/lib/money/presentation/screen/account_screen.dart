import 'package:app_common/app_common.dart';
import 'package:bitapp/l10n/localization_extension.dart';
import 'package:bitapp/money/bloc/account/account_bloc.dart';
import 'package:bitapp/money/bloc/transaction/transaction_bloc.dart';
import 'package:bitapp/money/presentation/screen/money_screen.dart';
import 'package:bitapp/money/presentation/viewmodel/account_viewmodel.dart';
import 'package:bitapp/money/presentation/widget/account_screen_actions.dart';
import 'package:bitapp/money/presentation/widget/account_summary.dart';
import 'package:bitapp/money/presentation/widget/transaction_list.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class AccountScreen extends AppScreen {
  static final String routeName = '/account';

  final String accountId;
  final String title;

  const AccountScreen({
    super.key,
    super.listener,
    required this.accountId,
    required this.title,
  });

  @override
  String get moduleName => title;

  @override
  String get backRouteName => MoneyScreen.routeName;

  @override
  AppScreenContent buildContent(BuildContext context) {
    return AccountScreenContent(accountId: accountId);
  }
}

class AccountScreenContent extends AppScreenContent {
  final String accountId;

  const AccountScreenContent({super.key, required this.accountId});

  @override
  Future<void> reload(BuildContext context) async {
    context.read<AccountBloc>().add(GetAccount(id: accountId));
    context.read<TransactionBloc>().add(
      GetAccountTransactions(accountId: accountId),
    );
  }

  @override
  Widget build(BuildContext context) {
    return BlocListener<AccountBloc, AccountState>(
      listener: (context, state) {
        if (state is AccountProcessed || state is AccountsRetrieved) {
          reload(context);
        }
      },
      child: BlocListener<TransactionBloc, TransactionState>(
        listener: (context, state) {
          if (state is TransactionProcessed) {
            reload(context);
          }
        },
        child: Column(
          children: [
            AccountSummary(),
            Padding(
              padding: EdgeInsets.only(top: 16),
              child: AccountScreenActions(),
            ),
            Padding(
              padding: EdgeInsets.only(top: 16, bottom: 16),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    context.locale.thisIsYourTransactions,
                    style: TextStyles.appDetail(fontSize: AppFontSize.small),
                  ),
                ],
              ),
            ),
            TransactionList(transactionSource: AccountViewModel),
          ],
        ),
      ),
    );
  }
}
