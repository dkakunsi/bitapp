import 'package:bitapp/features/user/data/user_repository.dart';
import 'package:bitapp/features/user/domain/user.dart';
import 'package:bitapp/common/util/language.dart';
import 'package:bitapp/common/util/processing_result.dart';
import 'package:logging/logging.dart';

class UserUseCase {
  final _logger = Logger("UserUseCase");
  final UserRepository _userRepository;

  UserUseCase(this._userRepository);

  Exception _toException(Object error) {
    return error is Exception ? error : Exception(error.toString());
  }

  Future<ProcessingResult<User>> createUser({
    required String userId,
    required String name,
    required String email,
    String? phone,
    String? photoUrl,
  }) async {
    final user = User(
      id: userId,
      name: name,
      email: email,
      phone: phone,
      photoUrl: photoUrl,
      language: Language.defaultLanguage,
    );
    try {
      await _userRepository.save(user.toModel());
      return ProcessingResult(data: user);
    } catch (e, stackTrace) {
      _logger.warning('Error creating user', e, stackTrace);
      return ProcessingResult(exception: _toException(e));
    }
  }

  Future<ProcessingResult<User>> updateLanguage(
    String userId,
    Language language,
  ) async {
    try {
      final updatedUserModel = await _userRepository.updateLanguage(
        userId,
        language,
      );
      final updatedUser = User.fromModel(updatedUserModel);
      return ProcessingResult(data: updatedUser);
    } catch (e, stackTrace) {
      _logger.warning('Error updating language for userId=$userId', e, stackTrace);
      return ProcessingResult(exception: _toException(e));
    }
  }
}
