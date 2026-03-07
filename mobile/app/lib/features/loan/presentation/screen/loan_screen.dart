import 'dart:async';

import 'package:bitapp/common/presentation/app_style.dart';
import 'package:bitapp/features/app/presentation/screen/app_screen.dart';
import 'package:bitapp/l10n/localization_extension.dart';
import 'package:bitapp/features/loan/presentation/bloc/loan_bloc.dart';
import 'package:bitapp/features/transaction/presentation/bloc/transaction_bloc.dart';
import 'package:bitapp/features/app/presentation/screen/money_screen.dart';
import 'package:bitapp/features/loan/presentation/viewmodel/loan_viewmodel.dart';
import 'package:bitapp/features/loan/presentation/widget/loan_screen_action.dart';
import 'package:bitapp/features/loan/presentation/widget/loan_summary.dart';
import 'package:bitapp/features/transaction/presentation/widget/transaction_list.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class LoanScreen extends AppScreen {
  static final String routeName = '/loan';

  final String loanId;
  final String title;

  const LoanScreen({
    super.key,
    super.listener,
    required this.loanId,
    required this.title,
  });

  @override
  String get moduleName => title;

  @override
  String get backRouteName => MoneyScreen.routeName;

  @override
  AppScreenContent buildContent(BuildContext context) =>
      LoanScreenContent(loanId: loanId);
}

class LoanScreenContent extends AppScreenContent {
  final String loanId;

  const LoanScreenContent({super.key, required this.loanId});

  @override
  Future<void> reload(BuildContext context) async {
    context.read<LoanBloc>().add(GetLoan(id: loanId));
    context.read<TransactionBloc>().add(GetLoanTransactions(loanId: loanId));
  }

  @override
  Widget build(BuildContext context) {
    return BlocListener<LoanBloc, LoanState>(
      listener: (context, state) {
        if (state is LoanProcessed) {
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
            LoanSummary(),
            Padding(
              padding: EdgeInsets.only(top: 16),
              child: LoanScreenAction(),
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
                  Container(),
                ],
              ),
            ),
            TransactionList(transactionSource: LoanViewModel),
          ],
        ),
      ),
    );
  }
}
