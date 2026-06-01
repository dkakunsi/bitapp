import 'package:bitapp/common/data/model/object_status.dart';
import 'package:bitapp/common/util/processing_result.dart';
import 'package:bitapp/features/account/data/account_api.dart';
import 'package:bitapp/features/account/data/account.dart';
import 'package:bitapp/features/account/data/account_store.dart';
import 'package:bitapp/features/configuration/data/configuration_store.dart';
import 'package:bitapp/features/transaction/data/transaction_store.dart';
import 'package:logging/logging.dart';
import 'package:uuid/uuid.dart';

class AccountUseCase {
  final _logger = Logger("AccountUseCase");
  final AccountApi _accountApi;
  final AccountStore _accountStore;
  final TransactionStore _transactionStore;
  final ConfigurationStore _configurationStore;

  AccountUseCase(
    this._accountApi,
    this._accountStore,
    this._transactionStore,
    this._configurationStore,
  );

  Future<ProcessingResult<void>> saveAccount(Account account) async {
    try {
      Account? result;
      if (await _configurationStore.isRemoteEnabled) {
        result = await _accountApi.add(account);
      } else if (account.id != null) {
        result = (await _accountStore.get(account.id!))?.copyWith(
          name: account.name,
          type: account.type,
          themeColor: account.themeColor,
        );
      } else {
        result = account.copyWith(
          id: Uuid().v4(),
          balance: 0,
          status: ObjectStatus.active,
        );
      }
      await _accountStore.save(result ?? account);
      return ProcessingResult();
    } on Exception catch (e) {
      _logger.warning('Error saving account: $e');
      return ProcessingResult(exception: e);
    }
  }

  Future<ProcessingResult<void>> deleteAccount(String id) async {
    try {
      if (await _configurationStore.isRemoteEnabled) {
        await _accountApi.delete(id);
      }
      await _accountStore.delete(id);
      await _transactionStore.deleteByAccountId(id);
      return ProcessingResult();
    } on Exception catch (e) {
      _logger.warning('Error deleting account: $e');
      return ProcessingResult(exception: e);
    }
  }

  Future<ProcessingResult<List<Account>>> fetchAccounts(String userId) async {
    try {
      if (await _configurationStore.isRemoteEnabled) {
        final fetchedAccounts = await _accountApi.fetchByUser(userId);
        if (fetchedAccounts.isNotEmpty) {
          await _accountStore.clear();
          await _accountStore.addAll(fetchedAccounts);
        }
      }
      final accounts = await _accountStore.getList(userId);
      return ProcessingResult(data: accounts);
    } on Exception catch (e) {
      _logger.warning('Error fetching accounts: $e');
      return ProcessingResult(exception: e);
    }
  }

  Future<ProcessingResult<List<Account>>> getAccounts(String userId) async {
    try {
      final accounts = await _accountStore.getList(userId);
      return ProcessingResult(data: accounts);
    } on Exception catch (e) {
      _logger.warning('Error getting accounts from store: $e');
      return ProcessingResult(exception: e);
    }
  }

  Future<ProcessingResult<Account>> getAccount(String accountId) async {
    try {
      final account = await _accountStore.get(accountId);
      return ProcessingResult(data: account);
    } on Exception catch (e) {
      _logger.warning('Error getting account from store: $e');
      return ProcessingResult(exception: e);
    }
  }
}
