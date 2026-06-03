import 'package:bitapp/common/presentation/app_style.dart';
import 'package:bitapp/common/presentation/viewmodel/viewmodel.dart';
import 'package:bitapp/common/util/formatter.dart';
import 'package:bitapp/features/account/data/account.dart';
import 'package:flutter/material.dart';

class AccountViewModel
    implements
        ListViewModel,
        CategoryViewModel,
        IconViewModel,
        ColorViewModel,
        AmountViewModel {
  final Account account;

  AccountViewModel(this.account);

  @override
  String? get category => account.type.value;

  @override
  Color get categoryColor => colorFromString(account.themeColor);

  @override
  IconData get icon {
    if (account.type == AccountType.ewallet) {
      return Icons.account_balance_wallet;
    }
    if (account.type == AccountType.bank) {
      return Icons.account_balance;
    }
    return Icons.wallet;
  }

  @override
  Color get color => colorFromString(account.themeColor);

  @override
  String? get objectType => account.type.value;

  @override
  String get title => account.name;

  @override
  String get subtitle => title;

  String get name => account.name;

  AccountType get type => account.type;

  String get themeColor => account.themeColor;

  String? get id => account.id;

  double get balance => account.balance ?? 0;

  @override
  double get amount => account.balance ?? 0;

  @override
  Color get amountColor => AppColor.mainDark;

  @override
  bool get showPaid => false;
  @override
  int compareTo(ListViewModel other) =>
      other is AccountViewModel ? title.compareTo(other.title) : 0;
}
