import 'package:bitapp/common/util/processing_result.dart';
import 'package:bitapp/features/authentication/data/google_authentication_api.dart';
import 'package:bitapp/features/authentication/data/session.dart';
import 'package:google_sign_in/google_sign_in.dart';
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
    } on GoogleSignInException catch (e) {
      _logGoogleSignInFailure(e, isSilentLogin: false);
      return ProcessingResult(exception: e);
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
    } on GoogleSignInException catch (e) {
      _logGoogleSignInFailure(e, isSilentLogin: true);
      return ProcessingResult(exception: e);
    } on Exception catch (e) {
      _logger.warning('Failed to authenticate. $e');
      return ProcessingResult(exception: e);
    }
  }

  void _logGoogleSignInFailure(
    GoogleSignInException exception, {
    required bool isSilentLogin,
  }) {
    final description = exception.description;
    final message =
        description == null
            ? exception.code.name
            : '${exception.code.name}, $description';

    if (exception.code == GoogleSignInExceptionCode.canceled) {
      final prefix =
          isSilentLogin
              ? 'Silent Google sign-in unavailable'
              : 'Google sign-in canceled';
      _logger.info('$prefix. $message');
      return;
    }

    _logger.warning('Failed to authenticate. $message');
  }
}
