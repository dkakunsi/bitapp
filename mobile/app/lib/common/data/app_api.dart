import 'package:bitapp/common/data/api.dart';
import 'package:bitapp/common/data/model/api_data.dart';
import 'package:http/http.dart' as http;

abstract class AppApi<T extends ApiData> extends Api {
  AppApi({required super.configurationStore});

  String get dataName;

  @override
  String get endpoint => 'v1/$dataName';

  List<T> fromList(String data);

  T from(String data);

  Future<List<T>> fetchByUser(String userId) async {
    final url = '${await baseUrl}/v1/users/$userId/$dataName';
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
    final response = await http.post(
      Uri.parse(await url),
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
    final response = await http.delete(
      Uri.parse('${await url}/$id'),
      headers: await buildRequestHeaders(),
    );
    if (response.statusCode != 200) {
      throw Exception(
        'Failed to delete data. Response: ${response.statusCode} ${response.request?.url}',
      );
    }
  }

  Future<T> update(String id, T t) async {
    final response = await http.put(
      Uri.parse('${await url}/$id'),
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
