import 'package:bitapp/features/configuration/data/configuration_store.dart';
import 'package:bitapp/features/configuration/domain/configuration.dart';
import 'package:uuid/uuid.dart';

abstract class Api {
  final ConfigurationStore configurationStore;

  Api({required this.configurationStore});

  String get endpoint;

  Future<String> get url async => '${await baseUrl}/$endpoint';

  String buildRequestId() => Uuid().v4();

  Future<Map<String, String>> buildRequestHeaders() async {
    final activeToken = await token;
    return {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer $activeToken',
      'Request-Id': buildRequestId(),
    };
  }

  Future<String> get baseUrl async {
    final config = await configurationStore.get(Configuration.storeId);
    return config!.backendBaseUrl;
  }

  Future<String?> get token async {
    final config = await configurationStore.get(Configuration.storeId);
    return config!.token;
  }
}
