import 'dart:convert';

import 'package:bitapp/common/common.dart';
import 'package:flutter/material.dart';

class Loan implements ApiData, StoreData {
  @override
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

  Loan copyWith({
    String? id,
    String? title,
    double? amount,
    DateTime? date,
    TimeOfDay? time,
    String? partyName,
    String? description,
    LoanType? type,
    String? userId,
    double? remainingAmount,
    ObjectStatus? status,
  }) {
    return Loan(
      id: id ?? this.id,
      title: title ?? this.title,
      amount: amount ?? this.amount,
      date: date ?? this.date,
      time: time ?? this.time,
      partyName: partyName ?? this.partyName,
      description: description ?? this.description,
      type: type ?? this.type,
      userId: userId ?? this.userId,
      remainingAmount: remainingAmount ?? this.remainingAmount,
      status: status ?? this.status,
    );
  }

  @override
  String toRequestJson() {
    var json = toStoreJson();
    json.remove('remainingAmount');
    return jsonEncode(json);
  }

  @override
  Map<String, dynamic> toStoreJson() {
    return {
      'id': id,
      'title': title,
      'description': description,
      'amount': amount,
      'partyName': partyName,
      'date': date.millisecondsSinceEpoch,
      'time': time.toInt(),
      'type': type.value,
      'user': userId,
      'remainingAmount': remainingAmount,
      'status': status?.value,
    };
  }

  static List<Loan> fromListResponsePayload(String s) {
    final Map<String, dynamic> data = jsonDecode(s);
    final List<dynamic> loans = data['loans'];
    return loans.isNotEmpty ? loans.map((e) => from(e)).toList() : [];
  }

  static Loan fromResponsePayload(String s) {
    final data = jsonDecode(s);
    return from(data);
  }

  static Loan from(dynamic data) {
    return Loan(
      id: data['id'],
      title: data['title'],
      description: data['description'],
      amount: (data['amount'] as num).toDouble(),
      partyName: data['partyName'],
      date: DateTime.fromMillisecondsSinceEpoch(data['date']),
      time: TimeFormatter.fromInt(data['time'] as int),
      type: LoanType.valueOf(data['type']),
      userId: data['user'] ?? '',
      remainingAmount:
          data['remainingAmount'] != null
              ? (data['remainingAmount'] as num).toDouble()
              : (data['amount'] as num).toDouble(),
      status:
          data['status'] != null
              ? ObjectStatus.valueOf(data['status'])
              : ObjectStatus.active,
    );
  }
}

const _debt = 'DEBT';
const _receivable = 'RECEIVABLE';

enum LoanType {
  debt(_debt),
  receivable(_receivable);

  final String value;

  const LoanType(this.value);

  static LoanType valueOf(String s) {
    switch (s) {
      case _debt:
        return LoanType.debt;
      case _receivable:
        return LoanType.receivable;
      default:
        throw Exception('DebtType not found');
    }
  }

  static List<LoanType> types() {
    return [debt, receivable];
  }
}
