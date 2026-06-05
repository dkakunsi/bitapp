import 'package:bitapp/features/configuration/domain/configuration.dart';
import 'package:bitapp/common/data/app_store.dart';
import 'package:bitapp/features/configuration/data/configuration_model.dart';
import 'package:sembast/sembast_io.dart';

class ConfigurationStore extends AppStore<ConfigurationModel> {
  ConfigurationStore(Database database) : super(database, 'configuration');

  @override
  ConfigurationModel from(Map<String, dynamic> data) {
    return ConfigurationModel.from(data);
  }

  Future<bool> get isRemoteEnabled async =>
      (await get(Configuration.storeId))?.remoteEnabled ?? false;
}
