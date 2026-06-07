import 'package:bitapp/common/presentation/app_style.dart';
import 'package:bitapp/common/presentation/viewmodel/viewmodel.dart';
import 'package:bitapp/common/util/formatter.dart';
import 'package:bitapp/features/account/domain/account.dart';
import 'package:bitapp/features/account/domain/account_type.dart';
import 'package:flutter/material.dart';

class AccountViewModel extends Account implements ListViewModel {
  AccountViewModel({
    super.id,
    required super.name,
    required super.type,
    required super.userId,
    required super.themeColor,
    super.balance,
  });

  AccountViewModel.fromAccount(Account account)
    : this(
        id: account.id,
        name: account.name,
        type: account.type,
        balance: account.balance,
        themeColor: account.themeColor,
        userId: account.userId,
      );

  Color get color => colorFromString(themeColor);

  @override
    double get balance => super.balance ?? 0;

  @override
  IconData get icon {
    if (type == AccountType.ewallet) {
      return Icons.account_balance_wallet;
    }
    if (type == AccountType.bank) {
      return Icons.account_balance;
    }
    return Icons.wallet;
  }

  @override
  String? get category => type.value;

  @override
  Color get categoryColor => colorFromString(themeColor);

  @override
  String? get objectType => type.value;

  @override
  String get title => name;

  @override
  String get subtitle => title;

  @override
  double get listAmount => balance ?? 0;

  @override
  Color get listAmountColor => AppColor.mainDark;

  @override
  DateTime? get date => null;

  @override
  int compareTo(ListViewModel other) =>
      other is AccountViewModel ? title.compareTo(other.title) : 0;
}
