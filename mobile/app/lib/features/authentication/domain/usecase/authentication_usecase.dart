import 'package:bitapp/common/util/processing_result.dart';
import 'package:bitapp/features/authentication/data/google_authentication_api.dart';
import 'package:bitapp/features/authentication/data/session.dart';
import 'package:logging/logging.dart';

class AuthenticationUseCase {
  final _logger = Logger("AuthenticationUseCase");
  final GoogleAuthenticationApi _authenticationApi;

  AuthenticationUseCase({required GoogleAuthenticationApi authenticationApi})
    : _authenticationApi = authenticationApi;

  Future<ProcessingResult<Session>> login() async {
    Session? session;
    try {
      session = await _authenticationApi.login();
    } on Exception catch (e) {
      _logger.warning('Failed to authenticate. $e');
      return ProcessingResult(exception: e);
    }

    return ProcessingResult(data: session);
  }

  Future<ProcessingResult<void>> logout() async {
    try {
      await _authenticationApi.logout();
      return ProcessingResult();
    } on Exception catch (e) {
      _logger.warning('Failed to authenticate. $e');
      return ProcessingResult(exception: e);
    }
  }

  Future<ProcessingResult<Session>> silentLogin() async {
    try {
      final session = await _authenticationApi.silentLogin();
      return ProcessingResult(data: session);
    } on Exception catch (e) {
      _logger.warning('Failed to authenticate. $e');
      return ProcessingResult(exception: e);
    }
  }
}
