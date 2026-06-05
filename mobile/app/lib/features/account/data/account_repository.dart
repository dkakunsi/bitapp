import 'package:bitapp/common/data/model/object_status.dart';
import 'package:bitapp/features/account/data/account_api.dart';
import 'package:bitapp/features/account/data/account_model.dart';
import 'package:bitapp/features/account/data/account_store.dart';
import 'package:bitapp/features/configuration/data/configuration_store.dart';
import 'package:uuid/uuid.dart';

class AccountRepository {
  final AccountApi _accountApi;
  final AccountStore _accountStore;
  final ConfigurationStore _configurationStore;

  AccountRepository({
    required AccountApi accountApi,
    required AccountStore accountStore,
    required ConfigurationStore configurationStore,
  }) : _accountApi = accountApi,
       _accountStore = accountStore,
       _configurationStore = configurationStore;

  Future<AccountModel> save(AccountModel accountModel) async {
    return await _configurationStore.isRemoteEnabled
        ? await _saveRemotely(accountModel)
        : await _saveLocally(accountModel);
  }

  Future<AccountModel> _saveRemotely(AccountModel accountModel) async {
    final result = await _accountApi.add(accountModel);
    await _accountStore.save(result);
    return result;
  }

  Future<AccountModel> _saveLocally(AccountModel accountModel) async {
    final existingAccountModel = await _accountStore.get(accountModel.id ?? '');
    final AccountModel updatedAccountModel;
    if (existingAccountModel == null) {
      updatedAccountModel = accountModel.copyWith(
        id: Uuid().v4(),
        balance: 0,
        status: ObjectStatus.active,
      );
    } else {
      updatedAccountModel = existingAccountModel.copyWith(
        name: accountModel.name,
        type: accountModel.type,
        themeColor: accountModel.themeColor,
      );
    }
    await _accountStore.save(updatedAccountModel);
    return updatedAccountModel;
  }

  Future<void> delete(String id) async {
    if (await _configurationStore.isRemoteEnabled) {
      await _accountApi.delete(id);
    }
    await _accountStore.delete(id);
  }

  Future<List<AccountModel>> fetchByUser(String userId) async {
    if (await _configurationStore.isRemoteEnabled) {
      final fetchedAccounts = await _accountApi.fetchByUser(userId);
      if (fetchedAccounts.isNotEmpty) {
        await _accountStore.clear();
        await _accountStore.addAll(fetchedAccounts);
      }
    }
    return await _accountStore.getList(userId);
  }

  Future<List<AccountModel>> getByUserId(String userId) async =>
      await _accountStore.getList(userId);

  Future<AccountModel?> getById(String accountId) async =>
      await _accountStore.get(accountId);
}
