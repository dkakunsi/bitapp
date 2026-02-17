import 'package:app_common/app_common.dart';
import 'package:flutter/material.dart';
import 'package:logging/logging.dart';

class ConfigurationUseCase {
  final _logger = Logger("ConfigurationUseCase");
  final ConfigurationStore _configurationStore;

  ConfigurationUseCase(this._configurationStore);

  Future<ProcessingResult<Configuration>> getConfiguration() async {
    try {
      final configuration = await _configurationStore.get(
        Configuration.storeId,
      );
      if (configuration != null) {
        return ProcessingResult(data: configuration);
      } else {
        _logger.warning(
          'Configuration not found, returning default configuration',
        );
        return ProcessingResult(data: Configuration.defaultConfiguration);
      }
    } catch (e) {
      _logger.warning('Error getting configuration: $e');
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
              Configuration.defaultConfiguration)
          .copyWith(
            newAppName: appName,
            newBackendBaseUrl: backendBaseUrl,
            newLanguage: language,
            newRemoteEnabled: remoteEnabled,
            newStartColor: startColor,
            newEndColor: endColor,
            newAppLogoUrl: appLogoUrl,
            newUser: user,
            newToken: token,
            newAppMotto: appMotto,
            newAppVersion: appVersion,
            newBuildNumber: buildNumber,
            newContact: contact,
            newDeveloperName: developerName,
          );
      await _configurationStore.save(updatedConfiguration);
      return ProcessingResult(data: updatedConfiguration);
    } on Exception catch (e) {
      _logger.warning('Error updating configuration: $e');
      return ProcessingResult(exception: e);
    }
  }

  Future<ProcessingResult<Configuration>> clearSession() async {
    try {
      final existingConfiguration =
          await _configurationStore.get(Configuration.storeId) ??
          Configuration.defaultConfiguration;
      final updatedConfiguration = existingConfiguration.copyWithoutSession();
      await _configurationStore.save(updatedConfiguration);
      return ProcessingResult(data: updatedConfiguration);
    } on Exception catch (e) {
      _logger.warning('Error clearing token: $e');
      return ProcessingResult(exception: e);
    }
  }
}
