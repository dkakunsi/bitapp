import 'package:bitapp/common/data/model/object_status.dart';
import 'package:bitapp/features/account/domain/account.dart';
import 'package:bitapp/features/loan/domain/loan.dart';
import 'package:bitapp/features/transaction/data/transaction_model.dart';
import 'package:bitapp/features/transaction/domain/transaction_category.dart';
import 'package:bitapp/features/transaction/domain/transaction_type.dart';
import 'package:flutter/material.dart';

class Transaction {
  final String? id;
  final String title;
  final double amount;
  final DateTime date;
  final String userId;
  final String? description;
  final TimeOfDay time;
  final TransactionType type;
  final Account? sourceAccount;
  final Account? destinationAccount;
  final Loan? loan;
  final ObjectStatus? status;
  final TransactionCategory? transactionCategory;

  Transaction({
    this.id,
    required this.userId,
    required this.title,
    required this.amount,
    required this.date,
    required this.time,
    required this.type,
    required this.transactionCategory,
    this.description,
    this.sourceAccount,
    this.destinationAccount,
    this.loan,
    this.status,
  });

  TransactionModel toModel() {
    return TransactionModel(
      id: id ?? '',
      userId: userId,
      title: title,
      amount: amount,
      date: date,
      time: time,
      transactionType: type,
      category: transactionCategory,
      description: description,
      sourceAccountId: sourceAccount?.id,
      destinationAccountId: destinationAccount?.id,
      loanId: loan?.id,
      status: status,
    );
  }

  static Transaction fromModel(
    TransactionModel transactionModel,
    Account? sourceAccount,
    Account? destinationAccount,
    Loan? loan,
  ) {
    return Transaction(
      id: transactionModel.id,
      userId: transactionModel.userId,
      title: transactionModel.title,
      amount: transactionModel.amount,
      date: transactionModel.date,
      time: transactionModel.time,
      type: transactionModel.transactionType,
      transactionCategory: transactionModel.category,
      description: transactionModel.description,
      sourceAccount: sourceAccount,
      destinationAccount: destinationAccount,
      loan: loan,
      status: transactionModel.status,
    );
  }
}
