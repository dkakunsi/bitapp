import 'package:bitapp/common/presentation/app_style.dart';
import 'package:bitapp/common/presentation/widget/app_button.dart';
import 'package:bitapp/common/presentation/widget/app_modal.dart';
import 'package:bitapp/l10n/localization_extension.dart';
import 'package:bitapp/features/loan/presentation/bloc/loan_bloc.dart';
import 'package:bitapp/features/loan/data/loan.dart';
import 'package:bitapp/features/transaction/data/transaction.dart';
import 'package:bitapp/features/loan/presentation/form/loan_form.dart';
import 'package:bitapp/features/transaction/presentation/form/transaction_form.dart';
import 'package:bitapp/features/app/presentation/screen/money_screen.dart';
import 'package:bitapp/features/loan/presentation/viewmodel/loan_viewmodel.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class LoanScreenAction extends StatelessWidget {
  const LoanScreenAction({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<LoanBloc, LoanState>(
      builder: (builderContext, state) {
        if (state is LoanRetrieved) {
          return Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              AppButton(
                label: context.locale.detail,
                icon: Icon(Icons.edit, color: AppColor.mainDark),
                fontSize: AppFontSize.small,
                width: 120,
                onTap: (context) => _onEditLoan(context, state.object),
              ),
              SizedBox(width: 8),
              AppButton(
                label: context.locale.pay,
                icon: Icon(Icons.add, color: AppColor.mainDark),
                onTap: (context) => _onAddTransaction(context, state.object),
              ),
              SizedBox(width: 8),
              AppButton(
                label: context.locale.export,
                icon: Icon(Icons.file_download, color: AppColor.mainDark),
                onTap: (context) {},
              ),
            ],
          );
        }
        return Container();
      },
    );
  }

  void _onAddTransaction(BuildContext context, LoanViewModel loan) {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        final key = GlobalKey<TransactionFormState>();
        return AppModal(
          modalKey: key,
          modalContent: TransactionForm(
            key: key,
            transactionType:
                loan.type == LoanType.debt
                    ? TransactionType.debit
                    : TransactionType.credit,
            loan: loan,
            title: context.locale.transaction,
          ),
          routeOnDelete: MoneyScreen.routeName,
          deleteLabel: context.locale.delete,
          saveLabel: context.locale.save,
        );
      },
    );
  }

  void _onEditLoan(BuildContext context, LoanViewModel lvm) {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        final key = GlobalKey<LoanFormState>();
        return AppModal(
          modalKey: key,
          modalContent: LoanForm(
            key: key,
            loanViewModel: lvm,
            type: lvm.type,
            title: context.locale.loan,
          ),
          routeOnDelete: MoneyScreen.routeName,
          deleteLabel: context.locale.delete,
          saveLabel: context.locale.save,
        );
      },
    );
  }
}
