import 'dart:convert';

import 'package:bitapp/common/data/model/api_data.dart';
import 'package:bitapp/common/data/model/object_status.dart';
import 'package:bitapp/common/data/model/store_data.dart';
import 'package:bitapp/features/account/domain/account_type.dart';

class AccountModel implements ApiData, StoreData {
  @override
  final String? id;

  final String userId;
  final String name;
  final double? balance;
  final AccountType type;
  final String themeColor;
  final ObjectStatus? status;

  AccountModel({
    this.id,
    required this.userId,
    required this.name,
    required this.type,
    required this.themeColor,
    this.balance,
    this.status,
  });

  @override
  String toRequestJson() {
    var json = toStoreJson();
    json.remove('balance');
    json.remove('status');
    return jsonEncode(json);
  }

  @override
  Map<String, dynamic> toStoreJson() => {
    'id': id,
    'name': name,
    'type': type.value,
    'themeColor': themeColor,
    'user': userId,
    'balance': balance,
    'status': status?.value,
  };

  static List<AccountModel> fromListResponsePayload(String s) {
    final List<dynamic> accounts = jsonDecode(s);
    return accounts.isNotEmpty ? accounts.map((e) => from(e)).toList() : [];
  }

  static AccountModel fromResponsePayload(String s) {
    final data = jsonDecode(s);
    return from(data);
  }

  static AccountModel from(dynamic data) {
    final balance =
        data['balance'] != null ? (data['balance'] as num).toDouble() : 0.0;
    final type = AccountType.valueOf(data['type']);
    final status =
        data['status'] != null
            ? ObjectStatus.valueOf(data['status'])
            : ObjectStatus.active;
    return AccountModel(
      id: data['id'],
      userId: data['user'],
      name: data['name'],
      balance: balance,
      type: type,
      themeColor: data['themeColor'],
      status: status,
    );
  }

  AccountModel copyWith({
    String? id,
    double? balance,
    ObjectStatus? status,
    String? name,
    AccountType? type,
    String? themeColor,
  }) => AccountModel(
    id: id ?? this.id,
    userId: userId,
    name: name ?? this.name,
    balance: balance ?? this.balance,
    type: type ?? this.type,
    themeColor: themeColor ?? this.themeColor,
    status: status ?? this.status,
  );
}
