import 'package:bitapp/common/presentation/app_style.dart';
import 'package:bitapp/features/authentication/extension/session_extension.dart';
import 'package:bitapp/common/presentation/viewmodel/viewmodel.dart';
import 'package:bitapp/common/presentation/widget/app_list.dart';
import 'package:bitapp/common/presentation/widget/app_modal.dart';
import 'package:bitapp/common/presentation/widget/app_tab.dart';
import 'package:bitapp/common/presentation/widget/loading_indicator.dart';
import 'package:bitapp/common/util/formatter.dart';
import 'package:bitapp/features/app/extension/navigation_extension.dart';
import 'package:bitapp/features/app/presentation/widget/currency_amount.dart';
import 'package:bitapp/features/summary/presentation/bloc/summary_bloc.dart';
import 'package:bitapp/l10n/localization_extension.dart';
import 'package:bitapp/features/loan/presentation/bloc/loan_bloc.dart';
import 'package:bitapp/features/loan/data/loan.dart';
import 'package:bitapp/features/loan/presentation/screen/loan_screen.dart';
import 'package:bitapp/features/loan/presentation/form/loan_form.dart';
import 'package:bitapp/features/loan/presentation/viewmodel/loan_viewmodel.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_speed_dial/flutter_speed_dial.dart';

class LoanPage extends AppTabPage {
  const LoanPage({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocListener<LoanBloc, LoanState>(
      listener: (context, state) {
        if (state is LoanProcessed) {
          onLoad(context);
        }
      },
      child: Column(
        children: <Widget>[
          AppTabPageHeader(headerLabel: context.locale.thisIsYourLoans),
          const SizedBox(height: 16),
          LoanList(),
        ],
      ),
    );
  }

  @override
  void onLoad(BuildContext context) {
    context.read<LoanBloc>().add(GetLoans(userId: context.userId));
    context.read<SummaryBloc>().add(CalculateSummary(userId: context.userId));
  }

  @override
  List<SpeedDialChild> buildFloatingActionButtons(BuildContext context) {
    return [
      SpeedDialChild(
        label: context.locale.addDebt,
        onTap: () => _onAddDebt(context),
      ),
      SpeedDialChild(
        label: context.locale.addReceivable,
        onTap: () => _onAddReceivable(context),
      ),
      SpeedDialChild(
        label: context.locale.export,
        onTap: () => _onExport(context),
      ),
    ];
  }

  void _onAddDebt(BuildContext context) => _onAdd(context, LoanType.debt);

  void _onAddReceivable(BuildContext context) =>
      _onAdd(context, LoanType.receivable);

  void _onAdd(BuildContext context, LoanType type) => showDialog(
    context: context,
    builder: (_) {
      final key = GlobalKey<LoanFormState>();
      return AppModal<LoanForm, LoanViewModel>(
        modalKey: key,
        modalContent: LoanForm(
          key: key,
          type: type,
          title: context.locale.receivable,
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

class LoanList extends StatelessWidget {
  const LoanList({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<LoanBloc, LoanState>(
      bloc: context.read<LoanBloc>(),
      builder: (context, state) {
        if (state is LoanProcessing) {
          return LoadingIndicator();
        } else if (state is LoansRetrieved) {
          return _buildListView(context, state.items);
        } else {
          return Container();
        }
      },
    );
  }

  AppList _buildListView(BuildContext context, List<ListViewModel> items) {
    return AppList(
      items: items,
      onItemTap: (l) {
        context.nextRoute(
          LoanScreen.routeName,
          argument: (l as LoanViewModel).id,
        );
      },
      showSubtitle: true,
      groupByFunction: (pl) => (pl as LoanViewModel).date.toDateFormat(context),
      getIcon: (pl) => (pl as LoanViewModel).icon,
      hightlight: (item) {
        item as LoanViewModel;
        return [
          (item.amount <= 0 && item.showPaid)
              ? Text(
                context.locale.paid,
                style: TextStyles.appDetail(
                  fontWeight: FontWeight.bold,
                  fontColor: item.amountColor,
                ),
              )
              : CurrencyAmount(
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
