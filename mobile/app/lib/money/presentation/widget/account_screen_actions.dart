import 'package:bitapp/common/common.dart';
import 'package:bitapp/l10n/localization_extension.dart';
import 'package:bitapp/money/bloc/account/account_bloc.dart';
import 'package:bitapp/money/data/model/transaction.dart';
import 'package:bitapp/money/presentation/form/account_form.dart';
import 'package:bitapp/money/presentation/form/transaction_form.dart';
import 'package:bitapp/money/presentation/screen/money_screen.dart';
import 'package:bitapp/money/presentation/viewmodel/account_viewmodel.dart';
import 'package:bitapp/money/presentation/viewmodel/transaction_viewmodel.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class AccountScreenActions extends StatelessWidget {
  const AccountScreenActions({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<AccountBloc, AccountState>(
      builder: (context, state) {
        if (state is AccountRetrieved) {
          return Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              AppButton(
                label: context.locale.detail,
                icon: Icon(Icons.edit, color: AppColor.mainDark),
                fontSize: AppFontSize.small,
                width: 120,
                onTap: (context) => _onEditAccount(context, state.object),
              ),
              SizedBox(width: 8),
              AppButton(
                label: context.locale.addTransaction,
                icon: Icon(Icons.add, color: AppColor.mainDark),
                fontSize: AppFontSize.small,
                width: 120,
                onTap: (context) => _onAddTransaction(context, state.object),
              ),
              SizedBox(width: 8),
              AppButton(
                label: context.locale.export,
                icon: Icon(Icons.file_download, color: AppColor.mainDark),
                fontSize: AppFontSize.small,
                onTap: (context) {},
              ),
            ],
          );
        } else {
          return Container();
        }
      },
    );
  }

  void _onAddTransaction(BuildContext context, AccountViewModel account) {
    showModalBottomSheet(
      context: context,
      builder: (_) {
        var options = {
          context.locale.addCredit:
              (key) => TransactionForm(
                key: key,
                transactionType: TransactionType.credit,
                destinationAccount: account,
                title: context.locale.transaction,
              ),
          context.locale.addDebit:
              (key) => TransactionForm(
                key: key,
                transactionType: TransactionType.debit,
                sourceAccount: account,
                title: context.locale.transaction,
              ),
          context.locale.addCreditTransfer:
              (key) => TransactionForm(
                key: key,
                transactionType: TransactionType.transfer,
                destinationAccount: account,
                title: context.locale.transaction,
              ),
          context.locale.addDebitTransfer:
              (key) => TransactionForm(
                key: key,
                transactionType: TransactionType.transfer,
                sourceAccount: account,
                title: context.locale.transaction,
              ),
        };
        return AppOptionModal<
          TransactionFormState,
          TransactionForm,
          TransactionViewModel
        >(
          options: options,
          saveLabel: context.locale.save,
          deleteLabel: context.locale.delete,
        );
      },
    );
  }

  void _onEditAccount(BuildContext context, AccountViewModel account) {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        final key = GlobalKey<AccountFormState>();
        return AppModal(
          modalKey: key,
          modalContent: AccountForm(
            key: key,
            accountViewModel: account,
            title: context.locale.account,
          ),
          routeOnDelete: MoneyScreen.routeName,
          deleteLabel: context.locale.delete,
          saveLabel: context.locale.save,
        );
      },
    );
  }
}
