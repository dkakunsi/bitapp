
import 'package:bitapp/common/data/model/object_status.dart';
import 'package:bitapp/features/loan/domain/loan_type.dart';
import 'package:flutter/material.dart';

class Loan  {
  final String? id;
  final String title;
  final double amount;
  final DateTime date;
  final TimeOfDay time;
  final String? partyName;
  final String? description;
  final LoanType type;
  final String userId;
  final double? remainingAmount;
  final ObjectStatus? status;

  Loan({
    this.id,
    required this.title,
    this.description,
    required this.amount,
    required this.partyName,
    required this.date,
    required this.time,
    required this.type,
    required this.userId,
    this.remainingAmount,
    this.status,
  });
}
