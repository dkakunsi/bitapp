import 'package:bitapp/common/util/language.dart';
import 'package:bitapp/common/util/processing_result.dart';
import 'package:bitapp/features/configuration/domain/configuration.dart';
import 'package:bitapp/features/configuration/data/configuration_store.dart';
import 'package:bitapp/features/user/domain/user.dart';
import 'package:flutter/material.dart';
import 'package:logging/logging.dart';

class ConfigurationUseCase {
  final _logger = Logger("ConfigurationUseCase");
  final ConfigurationStore _configurationStore;

  ConfigurationUseCase(this._configurationStore);

  Exception _toException(Object error) {
    return error is Exception ? error : Exception(error.toString());
  }

  Future<ProcessingResult<Configuration>> getConfiguration() async {
    try {
      final model = await _configurationStore.get(Configuration.storeId);
      if (model != null) {
        final user =
            model.userModel != null ? User.fromModel(model.userModel!) : null;
        return ProcessingResult(data: Configuration.from(model, user));
      } else {
        _logger.warning(
          'Configuration not found, returning default configuration',
        );
        return ProcessingResult(data: Configuration.defaultConfiguration);
      }
    } catch (e, stackTrace) {
      _logger.warning('Error getting configuration', e, stackTrace);
      return ProcessingResult(data: Configuration.defaultConfiguration);
    }
  }

  Future<ProcessingResult<Configuration>> updateConfiguration({
    String? appName,
    String? appMotto,
    String? appLogoUrl,
    Color? startColor,
    Color? endColor,
    Language? language,
    String? backendBaseUrl,
    bool? remoteEnabled,
    User? user,
    String? token,
    String? appVersion,
    String? buildNumber,
    String? contact,
    String? developerName,
  }) async {
    try {
      final existingConfiguration = await _configurationStore.get(
        Configuration.storeId,
      );

      final updatedConfiguration = (existingConfiguration ??
              Configuration.defaultConfiguration.toModel())
          .copyWith(
            newAppName: appName,
            newBackendBaseUrl: backendBaseUrl,
            newLanguage: language,
            newRemoteEnabled: remoteEnabled,
            newStartColor: startColor,
            newEndColor: endColor,
            newAppLogoUrl: appLogoUrl,
            newUserModel: user?.toModel(),
            newToken: token,
            newAppMotto: appMotto,
            newAppVersion: appVersion,
            newBuildNumber: buildNumber,
            newContact: contact,
            newDeveloperName: developerName,
          );
      await _configurationStore.save(updatedConfiguration);
      return ProcessingResult(
        data: Configuration.from(updatedConfiguration, user),
      );
    } catch (e, stackTrace) {
      _logger.warning('Error updating configuration', e, stackTrace);
      return ProcessingResult(exception: _toException(e));
    }
  }

  Future<ProcessingResult<Configuration>> clearSession() async {
    try {
      final existingConfiguration =
          await _configurationStore.get(Configuration.storeId) ??
          Configuration.defaultConfiguration.toModel();
      final updatedConfiguration = existingConfiguration.copyWithoutSession();
      await _configurationStore.save(updatedConfiguration);
      return ProcessingResult(
        data: Configuration.from(updatedConfiguration, null),
      );
    } catch (e, stackTrace) {
      _logger.warning('Error clearing token', e, stackTrace);
      return ProcessingResult(exception: _toException(e));
    }
  }
}
