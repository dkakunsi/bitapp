import 'package:bitapp/common/presentation/app_style.dart';
import 'package:bitapp/common/presentation/viewmodel/viewmodel.dart';
import 'package:bitapp/features/loan/data/loan.dart';
import 'package:flutter/material.dart';

class LoanViewModel
    implements
        ListViewModel,
        DateViewModel,
        CategoryViewModel,
        IconViewModel,
        AmountViewModel {
  final Loan loan;

  LoanViewModel(this.loan);

  String? get id => loan.id;

  double get principalAmount => loan.amount;

  String get description => loan.description ?? '';

  String get partyName => loan.partyName ?? '';

  TimeOfDay get time => loan.time;

  LoanType get type => loan.type;

  bool get isPaid => loan.remainingAmount == 0;

  Color get loanColor => isPaid ? AppColor.green : categoryColor;

  @override
  String? get objectType => loan.type.value;

  @override
  String get subtitle => loan.partyName ?? '';

  @override
  String get category => loan.type.value;

  @override
  Color get categoryColor =>
      loan.type == LoanType.debt ? AppColor.red : AppColor.green;

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
  DateTime get date => loan.date;

  @override
  String get title => loan.title;

  @override
  double get amount => loan.remainingAmount ?? loan.amount;

  @override
  Color get amountColor =>
      loan.type == LoanType.debt ? AppColor.red : AppColor.green;

  @override
  bool get showPaid => true;
}
