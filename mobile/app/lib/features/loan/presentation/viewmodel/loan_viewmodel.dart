import 'package:bitapp/common/presentation/app_style.dart';
import 'package:bitapp/common/presentation/viewmodel/viewmodel.dart';
import 'package:bitapp/features/loan/domain/loan.dart';
import 'package:bitapp/features/loan/domain/loan_type.dart';
import 'package:flutter/material.dart';

class LoanViewModel extends Loan implements ListViewModel {

  LoanViewModel({
    super.id,
    required super.userId,
    required super.title,
    required super.amount,
    required super.date,
    required super.time,
    required super.type,
    super.description,
    super.partyName,
    super.remainingAmount,
  });
        
  LoanViewModel.fromLoan(Loan loan)
    : this(
        id: loan.id,
        userId: loan.userId,
        title: loan.title,
        amount: loan.amount,
        date: loan.date,
        time: loan.time,
        type: loan.type,
        description: loan.description,
        partyName: loan.partyName,
        remainingAmount: loan.remainingAmount,
      );

  double get principalAmount => amount;

  bool get isPaid => remainingAmount == 0;

  Color get loanColor => isPaid ? AppColor.green : categoryColor;

  @override
  IconData get icon =>
      type == LoanType.debt ? Icons.assignment_late : Icons.assignment_return;

  @override
  String? get objectType => type.value;

  @override
  String get subtitle => partyName ?? '';

  @override
  String get category => type.value;

  @override
  Color get categoryColor =>
      type == LoanType.debt ? AppColor.red : AppColor.green;

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
  double get listAmount => remainingAmount ?? amount;

  @override
  Color get listAmountColor =>
      type == LoanType.debt ? AppColor.red : AppColor.green;
}
