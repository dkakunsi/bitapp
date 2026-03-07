part of 'money_tab_bloc.dart';

abstract class MoneyTabState extends AppTabState {}

class InitialMoneyTabState extends MoneyTabState {}

class MoneyTabSelected extends MoneyTabState implements AppTabSelected {
  @override
  final String label;

  MoneyTabSelected({required this.label});

  String get tabName => label;
}
