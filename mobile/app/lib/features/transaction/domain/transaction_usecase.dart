import 'package:bitapp/common/util/processing_result.dart';
import 'package:bitapp/features/configuration/data/configuration_store.dart';
import 'package:bitapp/features/transaction/data/transaction_api.dart';
import 'package:bitapp/features/transaction/data/transaction_model.dart';
import 'package:bitapp/features/transaction/domain/local_tansaction_service.dart';
import 'package:bitapp/features/transaction/domain/transaction.dart';
import 'package:bitapp/features/transaction/data/transaction_store.dart';
import 'package:logging/logging.dart';

class TransactionUseCase {
  final _logger = Logger("TransactionUseCase");
  final TransactionApi _transactionApi;
  final TransactionStore _transactionStore;
  final ConfigurationStore _configurationStore;
  final LocalTransactionService _localTransactionService;

  TransactionUseCase({
    required TransactionApi transactionApi,
    required TransactionStore transactionStore,
    required ConfigurationStore configurationStore,
    required LocalTransactionService localTransactionService,
  }) : _transactionApi = transactionApi,
       _transactionStore = transactionStore,
       _configurationStore = configurationStore,
       _localTransactionService = localTransactionService;

  Future<ProcessingResult<void>> addTransaction(Transaction transaction) async {
    try {
      if (await _configurationStore.isRemoteEnabled) {
        final result = await _transactionApi.add(transaction.toModel());
        await _transactionStore.save(result);
      } else {
        await _localTransactionService.save(transaction);
      }
      return ProcessingResult();
    } on Exception catch (e) {
      _logger.warning('Error creating transaction: $e');
      return ProcessingResult(exception: e);
    }
  }

  Future<ProcessingResult<void>> deleteTransaction(String id) async {
    try {
      if (await _configurationStore.isRemoteEnabled) {
        await _transactionApi.delete(id);
        await _transactionStore.delete(id);
      } else {
        final transactionModel = await _transactionStore.get(id);
        await _transactionStore.delete(id);
        await _localTransactionService.postDeletion(transactionModel!);
      }
      return ProcessingResult();
    } on Exception catch (e) {
      _logger.warning('Error deleting transaction: $e');
      return ProcessingResult(exception: e);
    }
  }

  Future<ProcessingResult<List<Transaction>>> fetchTransactions({
    required String userId,
  }) async {
    try {
      List<TransactionModel> transactionModels;
      if (await _configurationStore.isRemoteEnabled) {
        transactionModels = await _transactionApi.fetchByUser(userId);
        if (transactionModels.isNotEmpty) {
          await _transactionStore.clear();
          await _transactionStore.addAll(transactionModels);
        }
      } else {
        transactionModels = await _transactionStore.getList(userId);
      }

      final transactions = await Future.wait(
        transactionModels.map(
          (model) => _localTransactionService.buildTransaction(model),
        ),
      );
      return ProcessingResult(data: transactions);
    } on Exception catch (e) {
      _logger.warning('Error fetching transactions: $e');
      return ProcessingResult(exception: e);
    }
  }

  Future<ProcessingResult<List<Transaction>>> getTransactions(
    String userId,
  ) async {
    try {
      final transactionModels = await _transactionStore.getList(userId);
      final transactions = await Future.wait(
        transactionModels.map(
          (model) => _localTransactionService.buildTransaction(model),
        ),
      );
      return ProcessingResult(data: transactions);
    } on Exception catch (e) {
      _logger.warning('Error retrieving transactions from store: $e');
      return ProcessingResult(exception: e);
    }
  }

  Future<ProcessingResult<List<Transaction>>> getTransactionsByAccount({
    required String accountId,
  }) async {
    try {
      final transactionModels = await _transactionStore.getListByAccount(
        accountId,
      );
      final transactions = await Future.wait(
        transactionModels.map(
          (model) => _localTransactionService.buildTransaction(model),
        ),
      );
      return ProcessingResult(data: transactions);
    } on Exception catch (e) {
      _logger.warning(
        'Error retrieving transactions by account from store: $e',
      );
      return ProcessingResult(exception: e);
    }
  }

  Future<ProcessingResult<List<Transaction>>> getTransactionsByLoan({
    required String loanId,
  }) async {
    try {
      final transactionModels = await _transactionStore.getListByLoan(loanId);
      final transactions = await Future.wait(
        transactionModels.map(
          (model) => _localTransactionService.buildTransaction(model),
        ),
      );
      return ProcessingResult(data: transactions);
    } on Exception catch (e) {
      _logger.warning('Error retrieving transactions by loan from store: $e');
      return ProcessingResult(exception: e);
    }
  }
}
