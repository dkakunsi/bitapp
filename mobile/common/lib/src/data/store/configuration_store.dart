import 'package:app_common/app_common.dart';
import 'package:sembast/sembast_io.dart';

class ConfigurationStore extends AppStore<Configuration> {
  ConfigurationStore(Database database) : super(database, 'configuration');

  @override
  Configuration from(Map<String, dynamic> data) {
    return Configuration.from(data);
  }

  Future<bool> get isRemoteEnabled async =>
      (await get(Configuration.storeId))?.remoteEnabled ?? false;
}
