import 'dart:convert';

import 'package:bitapp/common/data/model/api_data.dart';
import 'package:bitapp/common/data/model/object_status.dart';
import 'package:bitapp/common/data/model/store_data.dart';
import 'package:bitapp/common/util/formatter.dart';
import 'package:bitapp/features/loan/domain/loan_type.dart';
import 'package:flutter/material.dart';

class LoanModel implements ApiData, StoreData {
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

  LoanModel({
    required this.id,
    required this.title,
    required this.amount,
    required this.date,
    required this.time,
    required this.partyName,
    required this.description,
    required this.type,
    required this.userId,
    required this.remainingAmount,
    required this.status,
  });

  LoanModel copyWith({
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
  }) => LoanModel(
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

  @override
  String toRequestJson() {
    var json = toStoreJson();
    json.remove('remainingAmount');
    return jsonEncode(json);
  }

  @override
  Map<String, dynamic> toStoreJson() => {
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

  static List<LoanModel> fromListResponsePayload(String s) {
    final List<dynamic> loans = jsonDecode(s);
    return loans.isNotEmpty ? loans.map((e) => from(e)).toList() : [];
  }

  static LoanModel fromResponsePayload(String s) {
    final data = jsonDecode(s);
    return from(data);
  }

  static LoanModel from(dynamic data) {
    final remainingAmount =
        data['remainingAmount'] != null
            ? (data['remainingAmount'] as num).toDouble()
            : (data['amount'] as num).toDouble();
    final status =
        data['status'] != null
            ? ObjectStatus.valueOf(data['status'])
            : ObjectStatus.active;
    return LoanModel(
      id: data['id'],
      title: data['title'],
      description: data['description'],
      amount: (data['amount'] as num).toDouble(),
      partyName: data['partyName'],
      date: DateTime.fromMillisecondsSinceEpoch(data['date']),
      time: TimeFormatter.fromInt(data['time'] as int),
      type: LoanType.valueOf(data['type']),
      userId: data['user'] ?? '',
      remainingAmount: remainingAmount,
      status: status,
    );
  }
}
