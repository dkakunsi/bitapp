import 'package:bitapp/common/presentation/app_style.dart';
import 'package:bitapp/common/presentation/viewmodel/viewmodel.dart';
import 'package:bitapp/features/loan/domain/loan_type.dart';
import 'package:bitapp/features/transaction/domain/transaction.dart';
import 'package:bitapp/features/transaction/domain/transaction_type.dart';
import 'package:flutter/material.dart';

class TransactionViewModel extends Transaction implements ListViewModel {
  TransactionViewModel({
    super.id,
    required super.userId,
    required super.title,
    required super.amount,
    required super.date,
    required super.time,
    required super.type,
    required super.transactionCategory,
    super.description,
    super.sourceAccount,
    super.destinationAccount,
    super.loan,
    super.status,
  });

  TransactionViewModel.fromTransaction(Transaction transaction)
    : this(
        id: transaction.id,
        userId: transaction.userId,
        title: transaction.title,
        amount: transaction.amount,
        date: transaction.date,
        time: transaction.time,
        type: transaction.type,
        transactionCategory: transaction.transactionCategory,
        description: transaction.description,
        sourceAccount: transaction.sourceAccount,
        destinationAccount: transaction.destinationAccount,
        loan: transaction.loan,
        status: transaction.status,
      );

  String get sourceAccountName => sourceAccount?.name ?? '';
  String get destinationAccountName => destinationAccount?.name ?? '';
  String get loanTitle => loan?.title ?? '';
  LoanType? get loanType => loan?.type;

  @override
  IconData get icon => transactionCategory?.icon ?? Icons.highlight_off;

  @override
  String get objectType => type.value;

  @override
  String get category => type.value;

  @override
  Color get categoryColor =>
      {
        TransactionType.debit: AppColor.red,
        TransactionType.credit: AppColor.green,
      }[type] ??
      AppColor.black;

  @override
  String get subtitle => sourceAccount?.name ?? destinationAccount?.name ?? '';

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
  Color get listAmountColor =>
      {
        TransactionType.debit: AppColor.red,
        TransactionType.credit: AppColor.green,
      }[type] ??
      AppColor.black;
  
  @override
  double get listAmount => amount;
}
