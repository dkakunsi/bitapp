import 'package:bitapp/common/data/model/object_status.dart';
import 'package:bitapp/features/account/data/account_model.dart';
import 'package:bitapp/features/account/domain/account_type.dart';

class Account {
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

  AccountModel toModel() => AccountModel(
    id: id,
    userId: userId,
    name: name,
    balance: balance,
    type: type,
    themeColor: themeColor,
    status: status,
  );

  static Account fromModel(AccountModel model) => Account(
    id: model.id,
    userId: model.userId,
    name: model.name,
    balance: model.balance,
    type: model.type,
    themeColor: model.themeColor,
    status: model.status,
  );
}
