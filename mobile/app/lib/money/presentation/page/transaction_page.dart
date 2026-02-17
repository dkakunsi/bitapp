import 'package:bitapp/common/common.dart';
import 'package:bitapp/l10n/localization_extension.dart';
import 'package:bitapp/money/bloc/summary/summary_bloc.dart';
import 'package:bitapp/money/bloc/transaction/transaction_bloc.dart';
import 'package:bitapp/money/data/model/transaction.dart';
import 'package:bitapp/money/presentation/form/transaction_form.dart';
import 'package:bitapp/money/presentation/screen/transaction_analytics_screen.dart';
import 'package:bitapp/money/presentation/viewmodel/transaction_viewmodel.dart';
import 'package:bitapp/money/presentation/widget/transaction_list.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_speed_dial/flutter_speed_dial.dart';

class TransactionPage extends AppTabPage {
  const TransactionPage({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocListener<TransactionBloc, TransactionState>(
      listener: (context, state) {
        if (state is TransactionProcessed) {
          onLoad(context);
        }
      },
      child: Column(
        children: [
          AppTabPageHeader(
            headerLabel: context.locale.thisIsYourTransactions,
            analyticLabel: context.locale.analytics,
            analyticRoute: TransactionAnalyticsScreen.routeName,
          ),
          const SizedBox(height: 16),
          TransactionList(transactionSource: TransactionViewModel),
        ],
      ),
    );
  }

  @override
  void onLoad(BuildContext context) {
    context.read<TransactionBloc>().add(
      GetTransactions(userId: context.userId),
    );
    context.read<SummaryBloc>().add(CalculateSummary(userId: context.userId));
  }

  @override
  List<SpeedDialChild> buildFloatingActionButtons(BuildContext context) => [
    SpeedDialChild(
      label: context.locale.addCredit,
      onTap: () => _onAddCredit(context),
    ),
    SpeedDialChild(
      label: context.locale.addDebit,
      onTap: () => _onAddDebit(context),
    ),
    SpeedDialChild(
      label: context.locale.addTransfer,
      onTap: () => _onAddTransfer(context),
    ),
    SpeedDialChild(
      label: context.locale.export,
      onTap: () => _onExport(context),
    ),
  ];

  void _onAddCredit(BuildContext context) =>
      _onAdd(context, TransactionType.credit);

  void _onAddDebit(BuildContext context) =>
      _onAdd(context, TransactionType.debit);

  void _onAddTransfer(BuildContext context) =>
      _onAdd(context, TransactionType.transfer);

  void _onAdd(BuildContext context, TransactionType type) => showDialog(
    context: context,
    builder: (_) {
      final key = GlobalKey<TransactionFormState>();
      return AppModal(
        modalKey: key,
        modalContent: TransactionForm(
          key: key,
          transactionType: type,
          title: context.locale.transaction,
        ),
        deleteLabel: context.locale.delete,
        saveLabel: context.locale.save,
      );
    },
  );

  void _onExport(BuildContext context) {
    context.infoMessage(context.locale.exportNotAvailable);
  }
}
