import 'package:bitapp/common/presentation/viewmodel/viewmodel.dart';
import 'package:bitapp/common/presentation/widget/app_list.dart';
import 'package:bitapp/common/presentation/widget/app_modal.dart';
import 'package:bitapp/common/presentation/widget/loading_indicator.dart';
import 'package:bitapp/common/util/formatter.dart';
import 'package:bitapp/features/app/presentation/widget/currency_amount.dart';
import 'package:bitapp/l10n/localization_extension.dart';
import 'package:bitapp/features/transaction/presentation/bloc/transaction_bloc.dart';
import 'package:bitapp/features/transaction/presentation/form/transaction_form.dart';
import 'package:bitapp/features/account/presentation/viewmodel/account_viewmodel.dart';
import 'package:bitapp/features/loan/presentation/viewmodel/loan_viewmodel.dart';
import 'package:bitapp/features/transaction/presentation/viewmodel/transaction_viewmodel.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class TransactionList extends StatelessWidget {
  final Type transactionSource;

  const TransactionList({super.key, required this.transactionSource});

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<TransactionBloc, TransactionState>(
      builder: (context, state) {
        if (state is TransactionProcessing) {
          return LoadingIndicator();
        } else if (state is TransactionsRetrieved ||
            state is TransactionInitial) {
          if (canRender(state)) {
            return _buildListView(
              context,
              (state as TransactionsRetrieved).items,
            );
          }
        }
        return Container();
      },
    );
  }

  bool canRender(TransactionState state) {
    return (state is UserTransactionsRetrieved &&
            transactionSource == TransactionViewModel) ||
        (state is LoanTransactionsRetrieved &&
            transactionSource == LoanViewModel) ||
        (state is AccountTransactionsRetrieved &&
            transactionSource == AccountViewModel);
  }

  AppList _buildListView(BuildContext context, List<ListViewModel> items) {
    return AppList(
      items: items,
      onItemTap: (l) {
        l as TransactionViewModel;
        showDialog(
          context: context,
          builder: (BuildContext context) {
            final key = GlobalKey<TransactionFormState>();
            return AppModal(
              modalKey: key,
              modalContent: TransactionForm(
                key: key,
                transaction: l,
                transactionType: l.type,
                title: context.locale.transaction,
              ),
              deleteLabel: context.locale.delete,
              saveLabel: context.locale.save,
            );
          },
        );
      },
      showSubtitle: true,
      groupByFunction: (pl) => pl.date?.toDateFormat(context) ?? '',
      getIcon: (pl) => pl.icon,
      hightlight: (item) {
        return [
          CurrencyAmount(
            amount: item.listAmount,
            currency: context.locale.idr,
            color: item.listAmountColor,
            fontWeight: FontWeight.bold,
          ),
        ];
      },
    );
  }
}
