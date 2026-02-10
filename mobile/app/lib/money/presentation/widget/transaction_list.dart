import 'package:app_common/app_common.dart';
import 'package:bitapp/l10n/localization_extension.dart';
import 'package:bitapp/money/bloc/transaction/transaction_bloc.dart';
import 'package:bitapp/money/presentation/form/transaction_form.dart';
import 'package:bitapp/money/presentation/viewmodel/account_viewmodel.dart';
import 'package:bitapp/money/presentation/viewmodel/loan_viewmodel.dart';
import 'package:bitapp/money/presentation/viewmodel/transaction_viewmodel.dart';
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
                transactionType: l.transactionType,
                title: context.locale.transaction,
              ),
              deleteLabel: context.locale.delete,
              saveLabel: context.locale.save,
            );
          },
        );
      },
      showSubtitle: true,
      groupByFunction:
          (pl) => (pl as TransactionViewModel).date.toDateFormat(context),
      getIcon: (pl) => (pl as TransactionViewModel).icon,
      hightlight: (item) {
        item as TransactionViewModel;
        return [
          CurrencyAmount(
            amount: item.amount,
            currency: context.locale.idr,
            color: item.amountColor,
            fontWeight: FontWeight.bold,
          ),
        ];
      },
    );
  }
}
