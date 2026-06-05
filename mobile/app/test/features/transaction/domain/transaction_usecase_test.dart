import 'package:bitapp/common/data/model/object_status.dart';
import 'package:bitapp/features/account/data/account_store.dart';
import 'package:bitapp/features/configuration/data/configuration_store.dart';
import 'package:bitapp/features/loan/data/loan_store.dart';
import 'package:bitapp/features/transaction/data/transaction_api.dart';
import 'package:bitapp/features/transaction/data/transaction_model.dart';
import 'package:bitapp/features/transaction/data/transaction_store.dart';
import 'package:bitapp/features/transaction/domain/local_tansaction_service.dart';
import 'package:bitapp/features/transaction/domain/transaction.dart' as app_transaction;
import 'package:bitapp/features/transaction/domain/transaction_category.dart';
import 'package:bitapp/features/transaction/domain/transaction_type.dart';
import 'package:bitapp/features/transaction/domain/transaction_usecase.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:sembast/sembast_memory.dart';

class TestConfigurationStore extends ConfigurationStore {
  bool remoteEnabled = false;

  TestConfigurationStore(super.database);

  @override
  Future<bool> get isRemoteEnabled async => remoteEnabled;
}

class TestTransactionApi extends TransactionApi {
  int addCalls = 0;
  int deleteCalls = 0;
  int fetchByUserCalls = 0;

  TransactionModel? addResponse;
  List<TransactionModel> fetchByUserResponse = [];

  TestTransactionApi({required super.configurationStore});

  @override
  Future<TransactionModel> add(TransactionModel t) async {
    addCalls += 1;
    return addResponse ?? t;
  }

  @override
  Future<void> delete(String id) async {
    deleteCalls += 1;
  }

  @override
  Future<List<TransactionModel>> fetchByUser(String userId) async {
    fetchByUserCalls += 1;
    return fetchByUserResponse;
  }
}

class TrackingTransactionStore extends TransactionStore {
  int clearCalls = 0;
  int addAllCalls = 0;
  int deleteCalls = 0;
  final Map<String, TransactionModel> _records = {};

  TrackingTransactionStore(super.database);

  @override
  Future<void> save(TransactionModel t) async {
    _records[t.id] = t;
  }

  @override
  Future<void> addAll(List<TransactionModel> listOfT) async {
    addAllCalls += 1;
    for (final item in listOfT) {
      _records[item.id] = item;
    }
  }

  @override
  Future<TransactionModel?> get(String id) async {
    return _records[id];
  }

  @override
  Future<List<TransactionModel>> getList(String userId) async {
    return _records.values.where((item) => item.userId == userId).toList();
  }

  @override
  Future<void> clear() async {
    clearCalls += 1;
    _records.clear();
  }

  @override
  Future<void> delete(String id) async {
    deleteCalls += 1;
    _records.remove(id);
  }
}

class TrackingLocalTransactionService extends LocalTransactionService {
  int saveCalls = 0;
  int postDeletionCalls = 0;
  int buildTransactionCalls = 0;

  TrackingLocalTransactionService({
    required super.transactionStore,
    required super.accountStore,
    required super.loanStore,
  });

  @override
  Future<void> save(app_transaction.Transaction transaction) async {
    saveCalls += 1;
  }

  @override
  Future<void> postDeletion(TransactionModel transactionModel) async {
    postDeletionCalls += 1;
  }

  @override
  Future<app_transaction.Transaction> buildTransaction(TransactionModel transactionModel) async {
    buildTransactionCalls += 1;
    return app_transaction.Transaction.fromModel(transactionModel, null, null, null);
  }
}

void main() {
  group('TransactionUseCase', () {
    late Database database;
    late TestConfigurationStore configurationStore;
    late TestTransactionApi transactionApi;
    late TrackingTransactionStore transactionStore;
    late TrackingLocalTransactionService localTransactionService;
    late TransactionUseCase useCase;

    setUp(() async {
      database = await databaseFactoryMemory.openDatabase('transaction-test.db');
      configurationStore = TestConfigurationStore(database);
      transactionApi = TestTransactionApi(configurationStore: configurationStore);
      transactionStore = TrackingTransactionStore(database);
      localTransactionService = TrackingLocalTransactionService(
        transactionStore: transactionStore,
        accountStore: AccountStore(database),
        loanStore: LoanStore(database),
      );

      useCase = TransactionUseCase(
        transactionApi: transactionApi,
        transactionStore: transactionStore,
        configurationStore: configurationStore,
        localTransactionService: localTransactionService,
      );
    });

    tearDown(() async {
      await database.close();
    });

    test('addTransaction uses api and store when remote enabled', () async {
      configurationStore.remoteEnabled = true;
      transactionApi.addResponse = _transactionModel(id: 'remote-id');

      final result = await useCase.addTransaction(_transaction());

      expect(result.isSuccess, isTrue);
      expect(transactionApi.addCalls, 1);
      expect(localTransactionService.saveCalls, 0);
      final stored = await transactionStore.get('remote-id');
      expect(stored, isNotNull);
    });

    test('addTransaction uses local service when remote disabled', () async {
      configurationStore.remoteEnabled = false;

      final result = await useCase.addTransaction(_transaction());

      expect(result.isSuccess, isTrue);
      expect(localTransactionService.saveCalls, 1);
      expect(transactionApi.addCalls, 0);
    });

    test('deleteTransaction uses api and store when remote enabled', () async {
      configurationStore.remoteEnabled = true;
      await transactionStore.save(_transactionModel(id: 'tx-1'));

      final result = await useCase.deleteTransaction('tx-1');

      expect(result.isSuccess, isTrue);
      expect(transactionApi.deleteCalls, 1);
      expect(transactionStore.deleteCalls, 1);
      expect(localTransactionService.postDeletionCalls, 0);
    });

    test('deleteTransaction runs local postDeletion when remote disabled', () async {
      configurationStore.remoteEnabled = false;
      await transactionStore.save(_transactionModel(id: 'tx-2'));

      final result = await useCase.deleteTransaction('tx-2');

      expect(result.isSuccess, isTrue);
      expect(transactionApi.deleteCalls, 0);
      expect(transactionStore.deleteCalls, 1);
      expect(localTransactionService.postDeletionCalls, 1);
    });

    test('fetchTransactions syncs remote data and builds domain models', () async {
      configurationStore.remoteEnabled = true;
      transactionApi.fetchByUserResponse = [
        _transactionModel(id: 'm1'),
        _transactionModel(id: 'm2'),
      ];

      final result = await useCase.fetchTransactions(userId: 'user-1');

      expect(result.isSuccess, isTrue);
      expect(result.data.length, 2);
      expect(transactionApi.fetchByUserCalls, 1);
      expect(transactionStore.clearCalls, 1);
      expect(transactionStore.addAllCalls, 1);
      expect(localTransactionService.buildTransactionCalls, 2);
    });

    test('fetchTransactions uses local store when remote disabled', () async {
      configurationStore.remoteEnabled = false;
      await transactionStore.save(_transactionModel(id: 'm1'));

      final result = await useCase.fetchTransactions(userId: 'user-1');

      expect(result.isSuccess, isTrue);
      expect(result.data.length, 1);
      expect(transactionApi.fetchByUserCalls, 0);
      expect(transactionStore.clearCalls, 0);
      expect(localTransactionService.buildTransactionCalls, 1);
    });
  });
}

app_transaction.Transaction _transaction({String? id}) {
  return app_transaction.Transaction(
    id: id,
    userId: 'user-1',
    title: 'Sample',
    amount: 100,
    date: DateTime(2026, 6, 1),
    time: const TimeOfDay(hour: 8, minute: 0),
    type: TransactionType.credit,
    transactionCategory: TransactionCategory.salary,
    status: ObjectStatus.active,
  );
}

TransactionModel _transactionModel({required String id}) {
  return TransactionModel(
    id: id,
    userId: 'user-1',
    title: 'Sample',
    amount: 100,
    date: DateTime(2026, 6, 1),
    time: const TimeOfDay(hour: 8, minute: 0),
    transactionType: TransactionType.credit,
    category: TransactionCategory.salary,
    status: ObjectStatus.active,
  );
}
