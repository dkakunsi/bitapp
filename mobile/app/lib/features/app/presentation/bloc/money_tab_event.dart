part of 'money_tab_bloc.dart';

abstract class MoneyTabEvent extends AppTabEvent {}

class SelectMoneyTab extends MoneyTabEvent implements SelectTabEvent {
  @override
  final String tabName;

  SelectMoneyTab(this.tabName);
}
