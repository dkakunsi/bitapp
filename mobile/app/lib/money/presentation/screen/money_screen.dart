import 'package:bitapp/common/common.dart';
import 'package:bitapp/l10n/localization_extension.dart';
import 'package:bitapp/money/bloc/account/account_bloc.dart';
import 'package:bitapp/money/bloc/loan/loan_bloc.dart';
import 'package:bitapp/money/bloc/money_tab/money_tab_bloc.dart';
import 'package:bitapp/money/bloc/summary/summary_bloc.dart';
import 'package:bitapp/money/bloc/transaction/transaction_bloc.dart';
import 'package:bitapp/money/presentation/page/account_page.dart';
import 'package:bitapp/money/presentation/page/loan_page.dart';
import 'package:bitapp/money/presentation/page/transaction_page.dart';
import 'package:bitapp/money/presentation/widget/money_summary.dart';
import 'package:bitapp/money/util/constant.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_speed_dial/flutter_speed_dial.dart';

class MoneyScreen extends AppScreen {
  static final String routeName = '/money';
  final String title;

  const MoneyScreen({super.key, super.listener, required this.title});

  @override
  String get moduleName => title;

  @override
  AppScreenContent buildContent(BuildContext context) => MoneyScreenContent();
}

// ignore: must_be_immutable
class MoneyScreenContent extends AppScreenContent {
  late Map<String, AppTabPage> _pages;

  MoneyScreenContent({super.key});

  @override
  Future<void> reload(BuildContext context) async {
    context.read<AccountBloc>().add(FetchAccounts(userId: context.userId));
    context.read<LoanBloc>().add(FetchLoans(userId: context.userId));
    context.read<TransactionBloc>().add(
      FetchTransactions(userId: context.userId),
    );
    context.read<SummaryBloc>().add(CalculateSummary(userId: context.userId));
  }

  @override
  Widget build(BuildContext context) {
    _pages = {
      MoneyTab.transaction.name: TransactionPage(),
      MoneyTab.loan.name: LoanPage(),
      MoneyTab.account.name: AccountPage(),
    };

    return Column(
      children: [
        MoneySummary(),
        SizedBox(height: 24),
        BlocBuilder<MoneyTabBloc, MoneyTabState>(
          builder: (context, state) {
            if (state is! MoneyTabSelected) {
              return LoadingIndicator();
            }
            return _pages[state.label]!;
          },
        ),
      ],
    );
  }

  @override
  Widget? buildNavigationBar(BuildContext context) =>
      BlocBuilder<MoneyTabBloc, MoneyTabState>(
        builder: (context, state) {
          if (state is! MoneyTabSelected) {
            return Container();
          }
          return BottomNavigationBar(
            currentIndex: _pages.keys.toList().indexOf(state.tabName),
            backgroundColor: AppColor.white,
            selectedItemColor: AppColor.mainDark,
            // unselectedItemColor: AppColor.disabledDark,
            unselectedFontSize: AppFontSize.small,
            selectedFontSize: AppFontSize.medium,
            selectedLabelStyle: TextStyles.appMain(),
            unselectedLabelStyle: TextStyles.appMain(),
            items: [
              BottomNavigationBarItem(
                icon: Icon(Icons.payments, size: 24),
                label: context.locale.transaction,
              ),
              BottomNavigationBarItem(
                icon: Icon(Icons.money, size: 24),
                label: context.locale.loan,
              ),
              BottomNavigationBarItem(
                icon: Icon(Icons.account_balance, size: 24),
                label: context.locale.account,
              ),
            ],
            onTap: (index) {
              final selectedKey = _pages.keys.elementAt(index);
              final selectedPage = _pages[selectedKey];
              context.read<MoneyTabBloc>().add(SelectMoneyTab(selectedKey));
              selectedPage?.onLoad(context);
            },
          );
        },
      );

  @override
  Widget? buildFloatingActionButton(BuildContext context) =>
      BlocBuilder<MoneyTabBloc, MoneyTabState>(
        builder: (context, state) {
          if (state is! MoneyTabSelected) {
            return Container();
          }
          final selectedPage = _pages[state.label];
          if (selectedPage!.buildFloatingActionButtons(context).isEmpty) {
            return Container();
          }
          return SpeedDial(
            icon: Icons.add,
            backgroundColor: AppColor.mainDark,
            foregroundColor: AppColor.white,
            activeBackgroundColor: AppColor.mainDark,
            activeForegroundColor: AppColor.white,
            children: selectedPage.buildFloatingActionButtons(context),
          );
        },
      );
}
