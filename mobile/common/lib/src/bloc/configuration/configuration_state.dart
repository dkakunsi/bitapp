part of 'configuration_bloc.dart';

abstract class ConfigurationState {
  const ConfigurationState();
}

class ConfigurationInitial extends ConfigurationState {
  const ConfigurationInitial();
}

class ConfigurationProcessed extends ConfigurationState implements ObjectState {
  final Configuration _configuration;

  const ConfigurationProcessed(this._configuration);

  @override
  ConfigurationViewModel get object => ConfigurationViewModel(_configuration);
}

class AppSettingsConfigured extends ConfigurationProcessed {
  const AppSettingsConfigured(super._configuration);
}

class SessionConfigured extends ConfigurationProcessed {
  const SessionConfigured(super._configuration);
}

class TokenConfigured extends ConfigurationProcessed {
  const TokenConfigured(super._configuration);
}

class LanguageConfigured extends ConfigurationProcessed {
  const LanguageConfigured(super._configuration);
}
