import 'package:app_common/app_common.dart';
import 'package:bitapp/money/data/model/loan.dart';
import 'package:bitapp/money/data/model/transaction.dart';
import 'package:flutter/material.dart';

class TransactionViewModel
    implements
        ListViewModel,
        DateViewModel,
        CategoryViewModel,
        IconViewModel,
        AmountViewModel {
  final Transaction _transaction;

  TransactionViewModel(this._transaction);

  String? get id => _transaction.id;

  TransactionType get transactionType => _transaction.transactionType;

  String get description => _transaction.description ?? '';

  TimeOfDay get time => _transaction.time;

  String get sourceAccountId => _transaction.sourceAccount?.id ?? '';
  String get sourceAccountName => _transaction.sourceAccount?.name ?? '';
  String get destinationAccountId => _transaction.destinationAccount?.id ?? '';
  String get destinationAccountName =>
      _transaction.destinationAccount?.name ?? '';
  String get loanId => _transaction.loan?.id ?? '';
  String get loanTitle => _transaction.loan?.title ?? '';
  LoanType? get loanType => _transaction.loan?.type;

  @override
  String get title => _transaction.title;

  @override
  DateTime get date => _transaction.date;

  @override
  String get objectType => _transaction.transactionType.value;

  @override
  String get category => _transaction.transactionType.value;

  TransactionCategory? get transactionCategory => _transaction.category;

  @override
  IconData get icon => transactionCategory?.icon ?? Icons.highlight_off;

  @override
  Color get categoryColor =>
      {
        TransactionType.debit: AppColor.red,
        TransactionType.credit: AppColor.green,
      }[_transaction.transactionType] ??
      AppColor.black;

  @override
  String get subtitle =>
      _transaction.sourceAccount?.name ??
      _transaction.destinationAccount?.name ??
      '';

  @override
  int compareTo(ListViewModel other) {
    if (other is TransactionViewModel) {
      // from latest to oldest
      final dateCompared = other.date.compareTo(date);
      if (dateCompared != 0) return dateCompared;
      return other.time.compareTo(time);
    }
    return 0;
  }

  @override
  double get amount => _transaction.amount;

  @override
  Color get amountColor =>
      {
        TransactionType.debit: AppColor.red,
        TransactionType.credit: AppColor.green,
      }[_transaction.transactionType] ??
      AppColor.black;

  @override
  bool get showPaid => false;
}
