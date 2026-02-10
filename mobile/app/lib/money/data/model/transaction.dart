import 'dart:convert';

import 'package:app_common/app_common.dart';
import 'package:bitapp/money/data/model/account.dart';
import 'package:bitapp/money/data/model/loan.dart';
import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';

class Transaction implements ApiData, StoreData {
  @override
  final String? id;
  final String title;
  final double amount;
  final DateTime date;
  final String userId;
  final String? description;
  final TimeOfDay time;
  final TransactionType transactionType;
  final TransactionAccount? sourceAccount;
  final String? sourceAccountId;
  final TransactionAccount? destinationAccount;
  final String? destinationAccountId;
  final TransactionLoan? loan;
  final String? loanId;
  final ObjectStatus? status;
  final TransactionCategory? category;

  Transaction({
    this.id,
    required this.userId,
    required this.title,
    required this.amount,
    required this.date,
    required this.time,
    required this.transactionType,
    required this.category,
    this.description,
    this.sourceAccount,
    this.sourceAccountId,
    this.destinationAccount,
    this.destinationAccountId,
    this.loan,
    this.loanId,
    this.status,
  });

  Transaction copyWith({
    String? id,
    String? title,
    double? amount,
    DateTime? date,
    String? userId,
    String? description,
    TimeOfDay? time,
    TransactionType? transactionType,
    TransactionCategory? category,
    TransactionAccount? sourceAccount,
    String? sourceAccountId,
    TransactionAccount? destinationAccount,
    String? destinationAccountId,
    TransactionLoan? loan,
    String? loanId,
    ObjectStatus? status,
  }) {
    return Transaction(
      id: id ?? this.id,
      userId: userId ?? this.userId,
      title: title ?? this.title,
      amount: amount ?? this.amount,
      date: date ?? this.date,
      time: time ?? this.time,
      transactionType: transactionType ?? this.transactionType,
      category: category ?? this.category,
      description: description ?? this.description,
      sourceAccount: sourceAccount ?? this.sourceAccount,
      sourceAccountId: sourceAccountId ?? this.sourceAccountId,
      destinationAccount: destinationAccount ?? this.destinationAccount,
      destinationAccountId: destinationAccountId ?? this.destinationAccountId,
      loan: loan ?? this.loan,
      loanId: loanId ?? this.loanId,
      status: status ?? this.status,
    );
  }

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
      'category': category?.name,
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
      'source': sourceAccount?.toStoreJson(),
      'sourceId': sourceAccountId,
      'destination': destinationAccount?.toStoreJson(),
      'destinationId': destinationAccountId,
      'amount': amount,
      'loan': loan?.toStoreJson(),
      'loanId': loanId,
      'status': status?.value,
    };
  }

  TransactionAccount? get account {
    if (transactionType == TransactionType.debit) {
      return sourceAccount;
    } else if (transactionType == TransactionType.credit) {
      return destinationAccount;
    } else {
      return destinationAccount;
    }
  }

  static List<Transaction> fromListResponsePayload(String s) {
    final Map<String, dynamic> data = jsonDecode(s);
    final List<dynamic> transactions = data['transactions'];
    return transactions.isNotEmpty
        ? transactions.map((e) => from(e)).toList()
        : [];
  }

  static Transaction fromResponsePayload(String s) {
    final data = jsonDecode(s);
    return from(data);
  }

  static Transaction from(dynamic data) {
    TransactionAccount? sourceAccount;
    if (data['source'] != null) {
      sourceAccount = TransactionAccount.from(data['source']);
    }

    TransactionAccount? destinationAccount;
    if (data['destination'] != null) {
      destinationAccount = TransactionAccount.from(data['destination']);
    }

    TransactionLoan? loan;
    if (data['loan'] != null) {
      loan = TransactionLoan.from(data['loan']);
    }

    String userId = '';
    if (data['user'] != null && data['user'] is String) {
      userId = data['user'];
    } else if (data['user'] != null && data['user'] is Map) {
      userId = data['user']['id'];
    }

    return Transaction(
      id: data['id'],
      userId: userId,
      title: data['title'],
      description: data['description'],
      amount: (data['amount'] as num).toDouble(),
      date: DateTime.fromMillisecondsSinceEpoch(data['date']),
      time: TimeFormatter.fromInt(data['time'] as int),
      transactionType: TransactionType.valueOf(data['type']),
      category: TransactionCategory.valueOf(data['category']),
      sourceAccount: sourceAccount,
      sourceAccountId: sourceAccount?.id,
      destinationAccount: destinationAccount,
      destinationAccountId: destinationAccount?.id,
      loan: loan,
      loanId: loan?.id,
      status:
          data['status'] != null
              ? ObjectStatus.valueOf(data['status'])
              : ObjectStatus.active,
    );
  }
}

const _debit = 'DEBIT';
const _credit = 'CREDIT';
const _transfer = 'TRANSFER';

enum TransactionType {
  debit(_debit),
  credit(_credit),
  transfer(_transfer);

  final String value;

  const TransactionType(this.value);

  static TransactionType valueOf(String s) {
    switch (s) {
      case _debit:
        return TransactionType.debit;
      case _credit:
        return TransactionType.credit;
      case _transfer:
        return TransactionType.transfer;
      default:
        throw Exception('TransactionType not found');
    }
  }

  static List<TransactionType> types() {
    return [debit, credit, transfer];
  }
}

enum TransactionCategory {
  bonus(
    isCredit: true,
    icon: FontAwesomeIcons.moneyBillTrendUp,
    color: Colors.amber,
  ),
  bills(isDebit: true, icon: FontAwesomeIcons.moneyBill1, color: Colors.blue),
  charity(
    isDebit: true,
    icon: Icons.volunteer_activism,
    color: Colors.blueGrey,
  ),
  education(isDebit: true, icon: Icons.school, color: Colors.brown),
  entertainment(isDebit: true, icon: Icons.movie, color: Colors.cyan),
  food(isDebit: true, icon: Icons.restaurant, color: Colors.deepOrange),
  gift(isDebit: true, icon: FontAwesomeIcons.gift, color: Colors.deepPurple),
  health(isDebit: true, icon: Icons.local_hospital, color: Colors.green),
  hobbies(isDebit: true, icon: Icons.sports_esports, color: Colors.grey),
  interest(isCredit: true, icon: Icons.receipt, color: Colors.indigo),
  investment(isDebit: true, icon: Icons.attach_money, color: Colors.lightBlue),
  loan(isDebit: true, icon: Icons.real_estate_agent, color: Colors.lightGreen),
  loanPayment(
    isCredit: true,
    isDebit: true,
    icon: Icons.payment,
    color: Colors.lime,
  ),
  other(
    isCredit: true,
    isDebit: true,
    isTransfer: true,
    icon: Icons.question_mark,
    color: Colors.orange,
  ),
  rent(isDebit: true, icon: Icons.apartment, color: Colors.pink),
  salary(
    isCredit: true,
    icon: FontAwesomeIcons.sackDollar,
    color: Colors.purple,
  ),
  savings(
    isCredit: true,
    isDebit: true,
    isTransfer: true,
    icon: Icons.savings,
    color: Colors.red,
  ),
  shopping(isDebit: true, icon: Icons.shopping_cart, color: Colors.teal),
  subscription(isDebit: true, icon: Icons.subscriptions, color: Colors.white),
  tax(isDebit: true, icon: Icons.receipt, color: Colors.yellow),
  transport(
    isDebit: true,
    icon: Icons.directions_car,
    color: Colors.amberAccent,
  ),
  travel(isDebit: true, icon: Icons.flight, color: Colors.blueAccent),
  utilities(
    isDebit: true,
    icon: Icons.electrical_services,
    color: Colors.cyanAccent,
  );

  final bool isCredit;
  final bool isDebit;
  final bool isTransfer;
  final IconData icon;
  final Color color;

  const TransactionCategory({
    this.isCredit = false,
    this.isDebit = false,
    this.isTransfer = false,
    required this.icon,
    required this.color,
  });

  bool canShow(TransactionType type) {
    return (isCredit && type == TransactionType.credit) ||
        (isDebit && type == TransactionType.debit) ||
        (isTransfer && type == TransactionType.transfer);
  }

  static TransactionCategory valueOf(String s) {
    return TransactionCategory.values.firstWhere(
      (e) => e.name == s,
      orElse: () => TransactionCategory.other,
    );
  }
}

class TransactionAccount {
  final String id;
  final String name;

  TransactionAccount({required this.id, required this.name});

  static TransactionAccount fromAccount(Account account) {
    return TransactionAccount(id: account.id ?? '', name: account.name);
  }

  Map<String, dynamic> toStoreJson() {
    return {'id': id, 'name': name};
  }

  static TransactionAccount from(dynamic data) {
    return TransactionAccount(id: data['id'], name: data['name']);
  }
}

class TransactionLoan {
  final String id;
  final String title;
  final LoanType type;

  TransactionLoan({required this.id, required this.title, required this.type});

  Map<String, dynamic> toStoreJson() {
    return {'id': id, 'title': title, 'type': type.value};
  }

  static TransactionLoan from(dynamic data) {
    return TransactionLoan(
      id: data['id'],
      title: data['title'],
      type: LoanType.valueOf(data['type']),
    );
  }

  static TransactionLoan fromLoan(Loan loan) {
    return TransactionLoan(
      id: loan.id ?? '',
      title: loan.title,
      type: loan.type,
    );
  }
}
