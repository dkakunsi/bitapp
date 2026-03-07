import 'package:bitapp/common/presentation/viewmodel/viewmodel.dart';
import 'package:bitapp/common/presentation/widget/app_category.dart';
import 'package:bitapp/common/presentation/widget/app_list.dart';
import 'package:bitapp/common/presentation/widget/app_modal.dart';
import 'package:bitapp/common/presentation/widget/app_tab.dart';
import 'package:bitapp/common/presentation/widget/loading_indicator.dart';
import 'package:bitapp/features/app/extension/navigation_extension.dart';
import 'package:bitapp/features/app/presentation/widget/currency_amount.dart';
import 'package:bitapp/features/authentication/extension/session_extension.dart';
import 'package:bitapp/l10n/localization_extension.dart';
import 'package:bitapp/features/account/presentation/bloc/account_bloc.dart';
import 'package:bitapp/features/summary/presentation/bloc/summary_bloc.dart';
import 'package:bitapp/features/account/presentation/screen/account_screen.dart';
import 'package:bitapp/features/account/presentation/viewmodel/account_viewmodel.dart';
import 'package:bitapp/features/account/presentation/form/account_form.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_speed_dial/flutter_speed_dial.dart';

class AccountPage extends AppTabPage {
  const AccountPage({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocListener<AccountBloc, AccountState>(
      listener: (context, state) {
        if (state is AccountProcessed) {
          onLoad(context);
        }
      },
      child: Column(
        children: <Widget>[
          AppTabPageHeader(headerLabel: context.locale.thisIsYourAccounts),
          const SizedBox(height: 16),
          AccountList(),
        ],
      ),
    );
  }

  void onAdd(BuildContext context) {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        final key = GlobalKey<AccountFormState>();
        return AppModal(
          modalKey: key,
          modalContent: AccountForm(key: key, title: context.locale.account),
          deleteLabel: context.locale.delete,
          saveLabel: context.locale.save,
        );
      },
    );
  }

  void onExport(BuildContext context) {
    context.infoMessage(context.locale.exportNotAvailable);
  }

  @override
  void onLoad(BuildContext context) {
    context.read<AccountBloc>().add(GetAccounts(userId: context.userId));
    context.read<SummaryBloc>().add(CalculateSummary(userId: context.userId));
  }

  @override
  List<SpeedDialChild> buildFloatingActionButtons(BuildContext context) {
    return [
      SpeedDialChild(label: context.locale.add, onTap: () => onAdd(context)),
      SpeedDialChild(
        label: context.locale.export,
        onTap: () => onExport(context),
      ),
    ];
  }
}

class AccountList extends StatelessWidget {
  const AccountList({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<AccountBloc, AccountState>(
      bloc: context.read<AccountBloc>(),
      builder: (context, state) {
        if (state is AccountProcessing) {
          return LoadingIndicator();
        } else if (state is AccountsRetrieved) {
          return _buildListView(context, state.items);
        }
        return Container();
      },
    );
  }

  AppList _buildListView(BuildContext context, List<ListViewModel> items) {
    return AppList(
      items: items,
      onItemTap: (l) {
        context.nextRoute(
          AccountScreen.routeName,
          argument: (l as AccountViewModel).id,
        );
      },
      showSubtitle: true,
      showCategory: true,
      getIcon: (pl) => (pl as AccountViewModel).icon,
      hightlight: (item) {
        item as AccountViewModel;
        return [
          ColoredCategory(color: item.categoryColor),
          SizedBox(width: 8),
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
