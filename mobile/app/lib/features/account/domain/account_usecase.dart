import 'package:bitapp/common/util/processing_result.dart';
import 'package:bitapp/features/account/data/account_model.dart';
import 'package:bitapp/features/account/data/account_repository.dart';
import 'package:bitapp/features/account/domain/account.dart';
import 'package:bitapp/features/transaction/data/transaction_store.dart';
import 'package:logging/logging.dart';

class AccountUseCase {
  final _logger = Logger("AccountUseCase");
  final AccountRepository _accountRepository;
  final TransactionStore _transactionStore;

  AccountUseCase({
    required AccountRepository accountRepository,
    required TransactionStore transactionStore,
  }) : _accountRepository = accountRepository,
       _transactionStore = transactionStore;

  Exception _toException(Object error) {
    return error is Exception ? error : Exception(error.toString());
  }

  Future<ProcessingResult<Account>> saveAccount(Account account) async {
    try {
      final accountModel = await _accountRepository.save(account.toModel());
      final currentAccount = Account.fromModel(accountModel);
      return ProcessingResult(data: currentAccount);
    } catch (e, stackTrace) {
      _logger.warning('Error saving account', e, stackTrace);
      return ProcessingResult(exception: _toException(e));
    }
  }

  Future<ProcessingResult<void>> deleteAccount(String id) async {
    try {
      await _accountRepository.delete(id);
      await _transactionStore.deleteByAccountId(id);
      return ProcessingResult();
    } catch (e, stackTrace) {
      _logger.warning('Error deleting account', e, stackTrace);
      return ProcessingResult(exception: _toException(e));
    }
  }

  Future<ProcessingResult<List<Account>>> fetchAccounts(String userId) async {
    try {
      final accountModels = await _accountRepository.fetchByUser(userId);
      final accounts = await convertToAccounts(accountModels);
      return ProcessingResult(data: accounts);
    } catch (e, stackTrace) {
      _logger.warning('Error fetching accounts', e, stackTrace);
      return ProcessingResult(exception: _toException(e));
    }
  }

  Future<List<Account>> convertToAccounts(
    List<AccountModel> accountModels,
  ) async => await Future.wait(
    accountModels.map((model) async {
      return Account.fromModel(model);
    }),
  );

  Future<ProcessingResult<List<Account>>> getAccounts(String userId) async {
    try {
      final accountModels = await _accountRepository.getByUserId(userId);
      final accounts = await convertToAccounts(accountModels);
      return ProcessingResult(data: accounts);
    } catch (e, stackTrace) {
      _logger.warning('Error getting accounts from store', e, stackTrace);
      return ProcessingResult(exception: _toException(e));
    }
  }

  Future<ProcessingResult<Account>> getAccount(String accountId) async {
    try {
      final accountModel = await _accountRepository.getById(accountId);
      if (accountModel == null) {
        throw Exception('Account not found');
      }
      final account = Account.fromModel(accountModel);
      return ProcessingResult(data: account);
    } catch (e, stackTrace) {
      _logger.warning('Error getting account from store', e, stackTrace);
      return ProcessingResult(exception: _toException(e));
    }
  }
}
