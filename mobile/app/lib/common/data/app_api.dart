import 'package:bitapp/common/data/model/api_data.dart';
import 'package:bitapp/features/configuration/domain/configuration.dart';
import 'package:bitapp/features/configuration/data/configuration_store.dart';
import 'package:http/http.dart' as http;
import 'package:uuid/uuid.dart';

abstract class AppApi<T extends ApiData> {
  final ConfigurationStore configurationStore;

  AppApi({required this.configurationStore});

  Future<String> get baseUrl async {
    final config = await configurationStore.get(Configuration.storeId);
    return config!.backendBaseUrl;
  }

  Future<String?> get token async {
    final config = await configurationStore.get(Configuration.storeId);
    return config!.token;
  }

  String buildRequestId() => Uuid().v4();

  String get dataName;

  String get pluralDataName => '${dataName}s';

  Future<String> get endpoint async => '${await baseUrl}/v1/$pluralDataName';

  List<T> fromList(String data);

  T from(String data);

  Future<Map<String, String>> buildRequestHeaders() async {
    final activeToken = await token;
    return {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer $activeToken',
      'Request-Id': buildRequestId(),
    };
  }

  Future<List<T>> fetchByUser(String userId) async {
    final url = '${await baseUrl}/v1/users/$userId/$pluralDataName';
    final response = await http.get(
      Uri.parse(url),
      headers: await buildRequestHeaders(),
    );

    if (response.statusCode == 200) {
      return fromList(response.body);
    } else {
      throw Exception(
        'Failed to fetch data. Response: ${response.statusCode} ${response.request?.url}',
      );
    }
  }

  Future<T> add(T t) async {
    final url = await endpoint;
    final response = await http.post(
      Uri.parse(url),
      headers: await buildRequestHeaders(),
      body: t.toRequestJson(),
    );
    if (response.statusCode == 200) {
      return from(response.body);
    } else {
      throw Exception(
        'Failed to add data. Response: ${response.statusCode} ${response.request?.url}',
      );
    }
  }

  Future<void> delete(String id) async {
    final url = '${await endpoint}/$id';
    final response = await http.delete(
      Uri.parse(url),
      headers: await buildRequestHeaders(),
    );
    if (response.statusCode != 200) {
      throw Exception(
        'Failed to delete data. Response: ${response.statusCode} ${response.request?.url}',
      );
    }
  }

  Future<T> update(String id, T t) async {
    final url = '${await endpoint}/$id';
    final response = await http.put(
      Uri.parse(url),
      headers: await buildRequestHeaders(),
      body: t.toRequestJson(),
    );
    if (response.statusCode == 200) {
      return from(response.body);
    } else {
      throw Exception(
        'Failed to update data. Response: ${response.request?.url} - ${response.statusCode} ${response.body}',
      );
    }
  }
}
