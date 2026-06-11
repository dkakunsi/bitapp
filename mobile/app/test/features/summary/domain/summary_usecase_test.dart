import 'package:bitapp/features/account/data/account_model.dart';
import 'package:bitapp/features/account/data/account_store.dart';
import 'package:bitapp/features/account/domain/account_type.dart';
import 'package:bitapp/features/loan/data/loan_model.dart';
import 'package:bitapp/features/loan/data/loan_store.dart';
import 'package:bitapp/features/loan/domain/loan_type.dart';
import 'package:bitapp/features/summary/domain/usecase/summary_usecase.dart';
import 'package:bitapp/features/transaction/data/transaction_model.dart';
import 'package:bitapp/features/transaction/data/transaction_store.dart';
import 'package:bitapp/features/transaction/domain/transaction_category.dart';
import 'package:bitapp/features/transaction/domain/transaction_type.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:sembast/sembast_memory.dart';

class StubAccountStore extends AccountStore {
  List<AccountModel> accounts = [];

  StubAccountStore(super.database);

  @override
  Future<List<AccountModel>> getList(String userId) async {
    return accounts.where((account) => account.userId == userId).toList();
  }
}

class StubLoanStore extends LoanStore {
  List<LoanModel> loans = [];

  StubLoanStore(super.database);

  @override
  Future<List<LoanModel>> getListByType(String userId, LoanType type) async {
    return loans
        .where((loan) => loan.userId == userId && loan.type == type)
        .toList();
  }
}

class StubTransactionStore extends TransactionStore {
  List<TransactionModel> transactions = [];

  StubTransactionStore(super.database);

  @override
  Future<List<TransactionModel>> getListByTypeAndDateRange(
    String user,
    TransactionType type,
    int start,
    int end,
  ) async {
    return transactions
        .where(
          (transaction) =>
              transaction.userId == user &&
              transaction.transactionType == type &&
              transaction.date.millisecondsSinceEpoch >= start &&
              transaction.date.millisecondsSinceEpoch <= end,
        )
        .toList();
  }
}

void main() {
  group('SummaryUseCase', () {
    late Database database;
    late StubAccountStore accountStore;
    late StubLoanStore loanStore;
    late StubTransactionStore transactionStore;
    late SummaryUseCase useCase;

    const userId = 'user-1';

    setUp(() async {
      database = await databaseFactoryMemory.openDatabase('summary-test.db');
      accountStore = StubAccountStore(database);
      loanStore = StubLoanStore(database);
      transactionStore = StubTransactionStore(database);
      useCase = SummaryUseCase(
        accountStore: accountStore,
        loanStore: loanStore,
        transactionStore: transactionStore,
      );
    });

    tearDown(() async {
      await database.close();
    });

    test('returns zero summary for empty data', () async {
      final result = await useCase.calculateSummary(userId);

      expect(result.isSuccess, isTrue);
      expect(result.data.totalAsset, 0);
      expect(result.data.totalDebt, 0);
      expect(result.data.totalIncome, 0);
      expect(result.data.totalExpense, 0);
    });

    test('aggregates totals for asset debt income and expense', () async {
      accountStore.accounts = [
        AccountModel(
          id: 'a1',
          userId: userId,
          name: 'Cash',
          type: AccountType.cash,
          themeColor: 'FFFFFF',
          balance: 150,
        ),
        AccountModel(
          id: 'a2',
          userId: userId,
          name: 'Bank',
          type: AccountType.bank,
          themeColor: '000000',
          balance: null,
        ),
      ];
      loanStore.loans = [
        LoanModel(
          id: 'l1',
          title: 'Debt',
          amount: 500,
          date: DateTime(2026, 6, 1),
          time: const TimeOfDay(hour: 9, minute: 0),
          partyName: null,
          description: null,
          type: LoanType.debt,
          userId: userId,
          remainingAmount: 200,
          status: null,
        ),
        LoanModel(
          id: 'l2',
          title: 'Receivable',
          amount: 500,
          date: DateTime(2026, 6, 1),
          time: const TimeOfDay(hour: 9, minute: 0),
          partyName: null,
          description: null,
          type: LoanType.receivable,
          userId: userId,
          remainingAmount: 400,
          status: null,
        ),
      ];
      transactionStore.transactions = [
        TransactionModel(
          id: 't1',
          userId: userId,
          title: 'Salary',
          amount: 1000,
          date: DateTime.now(),
          time: const TimeOfDay(hour: 8, minute: 0),
          transactionType: TransactionType.credit,
          category: TransactionCategory.salary,
        ),
        TransactionModel(
          id: 't2',
          userId: userId,
          title: 'Food',
          amount: 300,
          date: DateTime.now(),
          time: const TimeOfDay(hour: 12, minute: 0),
          transactionType: TransactionType.debit,
          category: TransactionCategory.food,
        ),
      ];

      final result = await useCase.calculateSummary(userId);

      expect(result.data.totalAsset, 150);
      expect(result.data.totalDebt, 200);
      expect(result.data.totalIncome, 1000);
      expect(result.data.totalExpense, 300);
    });

    test('includes only current month transactions', () async {
      final now = DateTime.now();
      final previousMonth = DateTime(now.year, now.month - 1, 15);

      transactionStore.transactions = [
        TransactionModel(
          id: 'in-month-credit',
          userId: userId,
          title: 'Salary',
          amount: 2000,
          date: DateTime(now.year, now.month, 5),
          time: const TimeOfDay(hour: 8, minute: 0),
          transactionType: TransactionType.credit,
          category: TransactionCategory.salary,
        ),
        TransactionModel(
          id: 'old-credit',
          userId: userId,
          title: 'Old salary',
          amount: 800,
          date: previousMonth,
          time: const TimeOfDay(hour: 8, minute: 0),
          transactionType: TransactionType.credit,
          category: TransactionCategory.salary,
        ),
        TransactionModel(
          id: 'in-month-debit',
          userId: userId,
          title: 'Food',
          amount: 400,
          date: DateTime(now.year, now.month, 7),
          time: const TimeOfDay(hour: 12, minute: 0),
          transactionType: TransactionType.debit,
          category: TransactionCategory.food,
        ),
        TransactionModel(
          id: 'old-debit',
          userId: userId,
          title: 'Old food',
          amount: 100,
          date: previousMonth,
          time: const TimeOfDay(hour: 12, minute: 0),
          transactionType: TransactionType.debit,
          category: TransactionCategory.food,
        ),
      ];

      final result = await useCase.calculateSummary(userId);

      expect(result.data.totalIncome, 2000);
      expect(result.data.totalExpense, 400);
    });
  });
}
