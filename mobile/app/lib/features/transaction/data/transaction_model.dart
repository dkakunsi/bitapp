import 'dart:convert';

import 'package:bitapp/common/data/model/api_data.dart';
import 'package:bitapp/common/data/model/object_status.dart';
import 'package:bitapp/common/data/model/store_data.dart';
import 'package:bitapp/common/util/formatter.dart';
import 'package:bitapp/features/transaction/domain/transaction_category.dart';
import 'package:bitapp/features/transaction/domain/transaction_type.dart';
import 'package:flutter/material.dart';

class TransactionModel implements ApiData, StoreData {
  @override
  final String id;

  final String title;
  final double amount;
  final DateTime date;
  final String userId;
  final String? description;
  final TimeOfDay time;
  final TransactionType transactionType;
  final String? sourceAccountId;
  final String? destinationAccountId;
  final String? loanId;
  final ObjectStatus? status;
  final TransactionCategory? category;

  TransactionModel({
    required this.id,
    required this.userId,
    required this.title,
    required this.amount,
    required this.date,
    required this.time,
    required this.transactionType,
    required this.category,
    this.description,
    this.sourceAccountId,
    this.destinationAccountId,
    this.loanId,
    this.status,
  });

  @override
  String toRequestJson() {
    var json = {
      'id': id,
      'user': userId,
      'title': title,
      'description': description,
      'date': date.millisecondsSinceEpoch,
      'time': time.toInt(),
      'type': transactionType.value,
      'category': category?.value,
      'source': sourceAccountId,
      'destination': destinationAccountId,
      'amount': amount,
      'loan': loanId,
    };
    return jsonEncode(json);
  }

  @override
  Map<String, dynamic> toStoreJson() {
    return {
      'id': id,
      'user': userId,
      'title': title,
      'description': description,
      'date': date.millisecondsSinceEpoch,
      'time': time.toInt(),
      'type': transactionType.value,
      'category': category?.name,
      'sourceId': sourceAccountId,
      'destinationId': destinationAccountId,
      'amount': amount,
      'loanId': loanId,
      'status': status?.value,
    };
  }

  static List<TransactionModel> fromListResponsePayload(String response) {
    final List<dynamic> transactions = jsonDecode(response);
    return transactions.isNotEmpty
        ? transactions.map((e) => from(e)).toList()
        : [];
  }

  static TransactionModel fromResponsePayload(String response) {
    final data = jsonDecode(response);
    return from(data);
  }

  static TransactionModel from(dynamic data) => TransactionModel(
    id: data['id'],
    userId: data['user'],
    title: data['title'],
    description: data['description'],
    amount: (data['amount'] as num).toDouble(),
    date: DateTime.fromMillisecondsSinceEpoch(data['date']),
    time: TimeFormatter.fromInt(data['time'] as int),
    transactionType: TransactionType.valueOf(data['type']),
    category: TransactionCategory.valueOf(data['category']),
    sourceAccountId: data['sourceAccountId'],
    destinationAccountId: data['destinationAccountId'],
    loanId: data['loanId'],
    status:
        data['status'] != null
            ? ObjectStatus.valueOf(data['status'])
            : ObjectStatus.active,
  );
}
