import 'package:bitapp/common/common.dart';
import 'package:bitapp/money/data/model/loan.dart';
import 'package:flutter/material.dart';

class LoanViewModel
    implements
        ListViewModel,
        DateViewModel,
        CategoryViewModel,
        IconViewModel,
        AmountViewModel {
  final Loan _loan;

  LoanViewModel(this._loan);

  String? get id => _loan.id;

  double get principalAmount => _loan.amount;

  String get description => _loan.description ?? '';

  String get partyName => _loan.partyName ?? '';

  TimeOfDay get time => _loan.time;

  LoanType get type => _loan.type;

  bool get isPaid => _loan.remainingAmount == 0;

  Color get loanColor => isPaid ? AppColor.green : categoryColor;

  @override
  String? get objectType => _loan.type.value;

  @override
  String get subtitle => _loan.partyName ?? '';

  @override
  String get category => _loan.type.value;

  @override
  Color get categoryColor =>
      _loan.type == LoanType.debt ? AppColor.red : AppColor.green;

  @override
  IconData get icon =>
      type == LoanType.debt ? Icons.assignment_late : Icons.assignment_return;

  @override
  int compareTo(ListViewModel other) {
    if (other is LoanViewModel) {
      // from latest to oldest
      final dateCompared = other.date.compareTo(date);
      if (dateCompared != 0) return dateCompared;
      return other.time.compareTo(time);
    }
    return 0;
  }

  @override
  DateTime get date => _loan.date;

  @override
  String get title => _loan.title;

  @override
  double get amount => _loan.remainingAmount ?? _loan.amount;

  @override
  Color get amountColor =>
      _loan.type == LoanType.debt ? AppColor.red : AppColor.green;

  @override
  bool get showPaid => true;
}
