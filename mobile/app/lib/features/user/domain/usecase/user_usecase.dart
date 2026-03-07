import 'package:bitapp/features/user/data/user.dart';
import 'package:bitapp/features/user/data/user_store.dart';
import 'package:bitapp/common/util/language.dart';
import 'package:bitapp/common/util/processing_result.dart';
import 'package:bitapp/features/configuration/data/configuration_store.dart';
import 'package:bitapp/features/user/data/user_api.dart';
import 'package:logging/logging.dart';

class UserUseCase {
  final _logger = Logger("UserUseCase");
  final UserApi _userApi;
  final UserStore _userStore;
  final ConfigurationStore _configurationStore;

  UserUseCase(this._userApi, this._userStore, this._configurationStore);

  Future<ProcessingResult<User>> createUser(
    String userId,
    String name,
    String email,
    String? phone,
    String? photoUrl,
  ) async {
    final user = User(
      id: userId,
      name: name,
      email: email,
      phone: phone,
      photoUrl: photoUrl,
      language: Language.defaultLanguage,
    );
    try {
      User? createdUser = user;
      if (await _configurationStore.isRemoteEnabled) {
        createdUser = await _userApi.add(user);
      }
      await _userStore.save(createdUser);
      return ProcessingResult(data: createdUser);
    } on Exception catch (e) {
      _logger.warning('Error creating user: $e');
      return ProcessingResult(exception: e);
    }
  }

  Future<ProcessingResult<User>> updateLanguage(
    String userId,
    Language language,
  ) async {
    try {
      User? updatedUser;
      if (await _configurationStore.isRemoteEnabled) {
        updatedUser = await _userApi.updateUserLanguage(userId, language);
        await _userStore.save(updatedUser);
      } else {
        final user = await _userStore.get(userId);
        if (user == null) {
          return ProcessingResult(exception: Exception('User not found'));
        }
        updatedUser = user.copyWith(language: language);
        await _userStore.save(updatedUser);
      }
      return ProcessingResult(data: updatedUser);
    } on Exception catch (e) {
      return ProcessingResult(exception: e);
    }
  }
}
