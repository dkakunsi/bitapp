import 'package:bitapp/common/data/model/object_status.dart';
import 'package:bitapp/common/util/processing_result.dart';
import 'package:bitapp/features/configuration/data/configuration_store.dart';
import 'package:bitapp/features/transaction/data/transaction_api.dart';
import 'package:bitapp/features/transaction/data/transaction.dart';
import 'package:bitapp/features/account/data/account_store.dart';
import 'package:bitapp/features/loan/data/loan_store.dart';
import 'package:bitapp/features/transaction/data/transaction_store.dart';
import 'package:logging/logging.dart';
import 'package:uuid/uuid.dart';

class TransactionUseCase {
  final _logger = Logger("TransactionUseCase");
  final TransactionApi _transactionApi;
  final TransactionStore _transactionStore;
  final ConfigurationStore _configurationStore;
  final LocalTansactionService _localTansactionService;

  TransactionUseCase(
    this._transactionApi,
    this._transactionStore,
    this._configurationStore,
    this._localTansactionService,
  );

  Future<ProcessingResult<void>> addTransaction(Transaction transaction) async {
    try {
      Transaction? result;
      if (await _configurationStore.isRemoteEnabled) {
        result = await _transactionApi.add(transaction);
        await _transactionStore.save(result);
      } else {
        await _localTansactionService.save(transaction);
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
        final transaction = await _transactionStore.get(id);
        await _transactionStore.delete(id);
        await _localTansactionService.postDeletion(transaction!);
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
      List<Transaction> transactions;
      if (await _configurationStore.isRemoteEnabled) {
        transactions = await _transactionApi.fetchByUser(userId);
        if (transactions.isNotEmpty) {
          await _transactionStore.clear();
          await _transactionStore.addAll(transactions);
        }
      } else {
        transactions = await _transactionStore.getList(userId);
      }
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
      final transactions = await _transactionStore.getList(userId);
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
      final transactions = await _transactionStore.getListByAccount(accountId);
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
      final transactions = await _transactionStore.getListByLoan(loanId);
      return ProcessingResult(data: transactions);
    } on Exception catch (e) {
      _logger.warning('Error retrieving transactions by loan from store: $e');
      return ProcessingResult(exception: e);
    }
  }
}

class LocalTansactionService {
  final AccountStore _accountStore;
  final LoanStore _loanStore;
  final TransactionStore _transactionStore;

  LocalTansactionService(
    this._transactionStore,
    this._accountStore,
    this._loanStore,
  );

  Future<void> save(Transaction transaction) async {
    if (transaction.id == null) {
      transaction = await _enrichTransaction(transaction);
    }
    await _transactionStore.save(transaction);
    await postCreation(transaction);
  }

  Future<Transaction> _enrichTransaction(Transaction transaction) async {
    return transaction.copyWith(
      id: Uuid().v4(),
      status: ObjectStatus.active,
      sourceAccount: await _getAccount(transaction.sourceAccountId),
      destinationAccount: await _getAccount(transaction.destinationAccountId),
      loan: await _getLoan(transaction.loanId),
    );
  }

  Future<TransactionAccount?> _getAccount(String? accountId) async {
    if (accountId == null) {
      return null;
    }
    final account = await _accountStore.get(accountId);
    if (account == null) {
      return null;
    }
    return TransactionAccount.fromAccount(account);
  }

  Future<TransactionLoan?> _getLoan(String? loanId) async {
    if (loanId == null) {
      return null;
    }
    final loan = await _loanStore.get(loanId);
    if (loan == null) {
      return null;
    }
    return TransactionLoan.fromLoan(loan);
  }

  Future<void> postCreation(Transaction transaction) async {
    if (transaction.transactionType == TransactionType.debit) {
      await debitAccount(transaction.sourceAccountId!, transaction.amount);
    } else if (transaction.transactionType == TransactionType.credit) {
      await creditAccount(
        transaction.destinationAccountId!,
        transaction.amount,
      );
    } else {
      await debitAccount(transaction.sourceAccountId!, transaction.amount);
      await creditAccount(
        transaction.destinationAccountId!,
        transaction.amount,
      );
    }
    if (transaction.loanId != null) {
      await decreaseLoan(transaction.loanId!, transaction.amount);
    }
  }

  Future<void> debitAccount(String accountId, double amount) async {
    final existingAccount = await _accountStore.get(accountId);
    if (existingAccount == null) {
      return;
    }
    final updatedAccount = existingAccount.copyWith(
      balance: (existingAccount.balance ?? 0) - amount,
    );
    _accountStore.save(updatedAccount);
  }

  Future<void> creditAccount(String accountId, double amount) async {
    final existingAccount = await _accountStore.get(accountId);
    if (existingAccount == null) {
      return;
    }
    final updatedAccount = existingAccount.copyWith(
      balance: (existingAccount.balance ?? 0) + amount,
    );
    _accountStore.save(updatedAccount);
  }

  Future<void> decreaseLoan(String loanId, double amount) async {
    final loan = await _loanStore.get(loanId);
    if (loan == null) {
      return;
    }
    _loanStore.save(
      loan.copyWith(
        remainingAmount: (loan.remainingAmount ?? loan.amount) - amount,
      ),
    );
  }

  Future<void> postDeletion(Transaction transaction) async {
    if (transaction.transactionType == TransactionType.debit) {
      await creditAccount(transaction.sourceAccountId!, transaction.amount);
    } else if (transaction.transactionType == TransactionType.credit) {
      await debitAccount(transaction.destinationAccountId!, transaction.amount);
    } else {
      await debitAccount(transaction.destinationAccountId!, transaction.amount);
      await creditAccount(transaction.sourceAccountId!, transaction.amount);
    }
    if (transaction.loanId != null) {
      await increaseLoan(transaction.loanId!, transaction.amount);
    }
  }

  Future<void> increaseLoan(String loanId, double amount) async {
    final loan = await _loanStore.get(loanId);
    if (loan == null) {
      return;
    }
    _loanStore.save(
      loan.copyWith(remainingAmount: loan.remainingAmount! + amount),
    );
  }
}
