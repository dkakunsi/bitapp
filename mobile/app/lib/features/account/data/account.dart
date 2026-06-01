import 'dart:convert';

import 'package:bitapp/common/data/model/api_data.dart';
import 'package:bitapp/common/data/model/object_status.dart';
import 'package:bitapp/common/data/model/store_data.dart';
import 'package:bitapp/common/util/json.dart';
import 'package:bitapp/features/user/data/user.dart';

class Account implements ApiData, StoreData {
  @override
  final String? id;
  final String userId;
  final String name;
  final double? balance;
  final AccountType type;
  final String themeColor;
  final ObjectStatus? status;

  Account({
    this.id,
    required this.userId,
    required this.name,
    required this.type,
    required this.themeColor,
    this.balance,
    this.status,
  });

  Account copyWith({
    String? id,
    String? userId,
    String? name,
    double? balance,
    AccountType? type,
    String? themeColor,
    ObjectStatus? status,
  }) {
    return Account(
      id: id ?? this.id,
      userId: userId ?? this.userId,
      name: name ?? this.name,
      balance: balance ?? this.balance,
      type: type ?? this.type,
      themeColor: themeColor ?? this.themeColor,
      status: status ?? this.status,
    );
  }

  @override
  String toRequestJson() {
    var json = toStoreJson();
    json.remove('balance');
    json.remove('status');
    return jsonEncode(json);
  }

  @override
  Map<String, dynamic> toStoreJson() {
    return {
      'id': id,
      'name': name,
      'type': type.value,
      'themeColor': themeColor,
      'user': userId,
      'balance': balance,
      'status': status?.value,
    };
  }

  static List<Account> fromListResponsePayload(String s) {
    final Map<String, dynamic> data = jsonDecode(s);
    final List<dynamic> accounts = data['accounts'];
    return accounts.isNotEmpty ? accounts.map((e) => from(e)).toList() : [];
  }

  static Account fromResponsePayload(String s) {
    final data = jsonDecode(s);
    return from(data);
  }

  static Account from(dynamic data) {
    String userId;
    if (isJsonObject(data['user'])) {
      var user = User.fromJson(data['user']);
      userId = user!.id!;
    } else {
      userId = data['user'];
    }

    return Account(
      id: data['id'],
      userId: userId,
      name: data['name'],
      balance:
          data['balance'] != null ? (data['balance'] as num).toDouble() : 0,
      type: AccountType.valueOf(data['type']),
      themeColor: data['themeColor'],
      status:
          data['status'] != null
              ? ObjectStatus.valueOf(data['status'])
              : ObjectStatus.active,
    );
  }
}

const _cash = 'CASH';
const _ewallet = 'EWALLET';
const _bank = 'BANK';

enum AccountType {
  cash(value: _cash),
  bank(value: _bank),
  ewallet(value: _ewallet);

  final String value;

  const AccountType({required this.value});

  static AccountType valueOf(String s) {
    switch (s) {
      case _bank:
        return AccountType.bank;
      case _ewallet:
        return AccountType.ewallet;
      case _cash:
        return AccountType.cash;
      default:
        throw Exception('AccountType not found');
    }
  }

  static List<String> types() {
    return [cash.value, bank.value, ewallet.value];
  }
}
