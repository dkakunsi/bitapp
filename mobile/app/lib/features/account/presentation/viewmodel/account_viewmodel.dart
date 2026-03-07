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
  final Account _account;

  AccountViewModel(this._account);

  @override
  String? get category => _account.type.value;

  @override
  Color get categoryColor => colorFromString(_account.themeColor);

  @override
  IconData get icon {
    if (_account.type == AccountType.ewallet) {
      return Icons.account_balance_wallet;
    }
    if (_account.type == AccountType.bank) {
      return Icons.account_balance;
    }
    return Icons.wallet;
  }

  @override
  Color get color => colorFromString(_account.themeColor);

  @override
  String? get objectType => _account.type.value;

  @override
  String get title => _account.name;

  @override
  String get subtitle => title;

  String get name => _account.name;

  AccountType get type => _account.type;

  String get themeColor => _account.themeColor;

  String? get id => _account.id;

  double get balance => _account.balance ?? 0;

  @override
  double get amount => _account.balance ?? 0;

  @override
  Color get amountColor => AppColor.mainDark;

  @override
  bool get showPaid => false;
  @override
  int compareTo(ListViewModel other) =>
      other is AccountViewModel ? title.compareTo(other.title) : 0;
}
