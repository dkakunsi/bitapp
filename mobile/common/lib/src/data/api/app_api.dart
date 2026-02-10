import 'package:app_common/app_common.dart';
import 'package:http/http.dart' as http;

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

  String get dataName;

  List<T> fromList(String data);

  T from(String data);

  Future<List<T>> fetchByUser(String userId) async {
    final response = await http.get(
      Uri.parse('${await baseUrl}/v1/user/$userId/${dataName}s'),
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ${await token}',
      },
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
    final response = await http.post(
      Uri.parse('${await baseUrl}/v1/$dataName'),
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ${await token}',
      },
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
    final response = await http.delete(
      Uri.parse('${await baseUrl}/v1/$dataName/$id'),
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ${await token}',
      },
    );
    if (response.statusCode != 200) {
      throw Exception(
        'Failed to delete data. Response: ${response.statusCode} ${response.request?.url}',
      );
    }
  }

  Future<T> update(String id, T t) async {
    final response = await http.put(
      Uri.parse('${await baseUrl}/v1/$dataName/$id'),
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ${await token}',
      },
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
