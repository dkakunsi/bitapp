import 'package:bitapp/common/data/model/configuration.dart';
import 'package:bitapp/common/data/store/app_store.dart';
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
