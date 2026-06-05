import 'package:bitapp/common/util/language.dart';
import 'package:bitapp/features/configuration/data/configuration_store.dart';
import 'package:bitapp/features/user/data/user_api.dart';
import 'package:bitapp/features/user/data/user_model.dart';
import 'package:bitapp/features/user/data/user_store.dart';

class UserRepository {
  final UserApi _userApi;
  final UserStore _userStore;
  final ConfigurationStore _configurationStore;

  UserRepository({
    required UserApi userApi,
    required UserStore userStore,
    required ConfigurationStore configurationStore,
  }) : _configurationStore = configurationStore,
       _userApi = userApi,
       _userStore = userStore;

  Future<UserModel> save(UserModel userModel) async {
    if (await _configurationStore.isRemoteEnabled) {
      final savedUser = await _userApi.add(userModel);
      await _userStore.save(savedUser);
      return savedUser;
    } else {
      await _userStore.save(userModel);
      return userModel;
    }
  }

  Future<UserModel> updateLanguage(String userId, Language language) async {
    UserModel? result;
    if (await _configurationStore.isRemoteEnabled) {
      result = await _userApi.updateUserLanguage(userId, language);
    } else {
      final user = await _userStore.get(userId);
      if (user == null) {
        throw Exception('User not found');
      }
      result = user.updateLanguage(language);
    }
    await _userStore.save(result);
    return result;
  }
}
