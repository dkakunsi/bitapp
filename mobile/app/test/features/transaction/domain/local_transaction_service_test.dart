import 'package:bitapp/common/data/model/object_status.dart';
import 'package:bitapp/features/account/data/account_model.dart';
import 'package:bitapp/features/account/data/account_store.dart';
import 'package:bitapp/features/account/domain/account.dart';
import 'package:bitapp/features/account/domain/account_type.dart';
import 'package:bitapp/features/loan/data/loan_model.dart';
import 'package:bitapp/features/loan/data/loan_store.dart';
import 'package:bitapp/features/loan/domain/loan.dart';
import 'package:bitapp/features/loan/domain/loan_type.dart';
import 'package:bitapp/features/transaction/data/transaction_model.dart';
import 'package:bitapp/features/transaction/data/transaction_store.dart';
import 'package:bitapp/features/transaction/domain/local_tansaction_service.dart';
import 'package:bitapp/features/transaction/domain/transaction.dart' as app_transaction;
import 'package:bitapp/features/transaction/domain/transaction_category.dart';
import 'package:bitapp/features/transaction/domain/transaction_type.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:sembast/sembast_memory.dart';

class InMemoryAccountStore extends AccountStore {
  final Map<String, AccountModel> _accounts = {};

  InMemoryAccountStore(super.database);

  @override
  Future<AccountModel?> get(String id) async => _accounts[id];

  @override
  Future<void> save(AccountModel t) async {
    final id = t.id;
    if (id == null) {
      throw Exception('Account id cannot be null in test store');
    }
    _accounts[id] = t;
  }

  void seed(AccountModel account) {
    if (account.id != null) {
      _accounts[account.id!] = account;
    }
  }
}

class InMemoryLoanStore extends LoanStore {
  final Map<String, LoanModel> _loans = {};

  InMemoryLoanStore(super.database);

  @override
  Future<LoanModel?> get(String id) async => _loans[id];

  @override
  Future<void> save(LoanModel t) async {
    final id = t.id;
    if (id == null) {
      throw Exception('Loan id cannot be null in test store');
    }
    _loans[id] = t;
  }

  void seed(LoanModel loan) {
    if (loan.id != null) {
      _loans[loan.id!] = loan;
    }
  }
}

class InMemoryTransactionStore extends TransactionStore {
  int saveCalls = 0;
  final Map<String, TransactionModel> _transactions = {};

  InMemoryTransactionStore(super.database);

  @override
  Future<void> save(TransactionModel t) async {
    saveCalls += 1;
    _transactions[t.id] = t;
  }

  @override
  Future<TransactionModel?> get(String id) async => _transactions[id];
}

void main() {
  group('LocalTransactionService', () {
    late InMemoryAccountStore accountStore;
    late InMemoryLoanStore loanStore;
    late InMemoryTransactionStore transactionStore;
    late LocalTransactionService service;

    setUp(() async {
      final database = await databaseFactoryMemory.openDatabase(
        'local-transaction-service-test.db',
      );
      accountStore = InMemoryAccountStore(database);
      loanStore = InMemoryLoanStore(database);
      transactionStore = InMemoryTransactionStore(database);
      service = LocalTransactionService(
        transactionStore: transactionStore,
        accountStore: accountStore,
        loanStore: loanStore,
      );

      accountStore.seed(_accountModel(id: 'source', balance: 1000));
      accountStore.seed(_accountModel(id: 'destination', balance: 200));
      loanStore.seed(_loanModel(id: 'loan-1', amount: 500, remainingAmount: 500));
    });

    test('save stores transaction and applies debit side effect', () async {
      final transaction = _transaction(
        id: 'tx-1',
        type: TransactionType.debit,
        sourceAccountId: 'source',
        amount: 150,
      );

      await service.save(transaction);

      final source = await accountStore.get('source');
      expect(transactionStore.saveCalls, 1);
      expect(source?.balance, 850);
    });

    test('postCreation credit increases destination account balance', () async {
      final transaction = _transaction(
        id: 'tx-2',
        type: TransactionType.credit,
        destinationAccountId: 'destination',
        amount: 50,
      );

      await service.postCreation(transaction);

      final destination = await accountStore.get('destination');
      expect(destination?.balance, 250);
    });

    test('postCreation transfer updates both source and destination balances', () async {
      final transaction = _transaction(
        id: 'tx-3',
        type: TransactionType.transfer,
        sourceAccountId: 'source',
        destinationAccountId: 'destination',
        amount: 300,
      );

      await service.postCreation(transaction);

      final source = await accountStore.get('source');
      final destination = await accountStore.get('destination');
      expect(source?.balance, 700);
      expect(destination?.balance, 500);
    });

    test('postCreation with loan decreases remaining amount', () async {
      final transaction = _transaction(
        id: 'tx-4',
        type: TransactionType.debit,
        sourceAccountId: 'source',
        amount: 120,
        loanId: 'loan-1',
      );

      await service.postCreation(transaction);

      final loan = await loanStore.get('loan-1');
      expect(loan?.remainingAmount, 380);
    });

    test('postDeletion reverses transfer and loan side effects', () async {
      final model = TransactionModel(
        id: 'tx-5',
        userId: 'user-1',
        title: 'Transfer loan',
        amount: 100,
        date: DateTime(2026, 6, 5),
        time: const TimeOfDay(hour: 10, minute: 0),
        transactionType: TransactionType.transfer,
        category: TransactionCategory.savings,
        sourceAccountId: 'source',
        destinationAccountId: 'destination',
        loanId: 'loan-1',
        status: ObjectStatus.active,
      );

      await service.postCreation(
        _transaction(
          id: 'tx-5',
          type: TransactionType.transfer,
          sourceAccountId: 'source',
          destinationAccountId: 'destination',
          amount: 100,
          loanId: 'loan-1',
        ),
      );

      await service.postDeletion(model);

      final source = await accountStore.get('source');
      final destination = await accountStore.get('destination');
      final loan = await loanStore.get('loan-1');
      expect(source?.balance, 1000);
      expect(destination?.balance, 200);
      expect(loan?.remainingAmount, 500);
    });
  });
}

AccountModel _accountModel({required String id, required double balance}) {
  return AccountModel(
    id: id,
    userId: 'user-1',
    name: id,
    type: AccountType.cash,
    themeColor: 'FFFFFF',
    balance: balance,
    status: ObjectStatus.active,
  );
}

LoanModel _loanModel({
  required String id,
  required double amount,
  required double remainingAmount,
}) {
  return LoanModel(
    id: id,
    title: 'Loan',
    amount: amount,
    date: DateTime(2026, 6, 5),
    time: const TimeOfDay(hour: 9, minute: 0),
    partyName: null,
    description: null,
    type: LoanType.debt,
    userId: 'user-1',
    remainingAmount: remainingAmount,
    status: ObjectStatus.active,
  );
}

app_transaction.Transaction _transaction({
  required String id,
  required TransactionType type,
  required double amount,
  String? sourceAccountId,
  String? destinationAccountId,
  String? loanId,
}) {
  final source =
      sourceAccountId == null
          ? null
          : Account(
            id: sourceAccountId,
            userId: 'user-1',
            name: sourceAccountId,
            balance: 0,
            type: AccountType.cash,
            themeColor: 'FFFFFF',
            status: ObjectStatus.active,
          );
  final destination =
      destinationAccountId == null
          ? null
          : Account(
            id: destinationAccountId,
            userId: 'user-1',
            name: destinationAccountId,
            balance: 0,
            type: AccountType.cash,
            themeColor: 'FFFFFF',
            status: ObjectStatus.active,
          );
  final loan =
      loanId == null
          ? null
          : Loan(
            id: loanId,
            title: 'Loan',
            amount: 500,
            date: DateTime(2026, 6, 5),
            time: const TimeOfDay(hour: 9, minute: 0),
            partyName: null,
            description: null,
            type: LoanType.debt,
            userId: 'user-1',
            remainingAmount: 500,
            status: ObjectStatus.active,
          );

  return app_transaction.Transaction(
    id: id,
    userId: 'user-1',
    title: 'Txn',
    amount: amount,
    date: DateTime(2026, 6, 5),
    time: const TimeOfDay(hour: 10, minute: 0),
    type: type,
    transactionCategory: TransactionCategory.other,
    sourceAccount: source,
    destinationAccount: destination,
    loan: loan,
    status: ObjectStatus.active,
  );
}
