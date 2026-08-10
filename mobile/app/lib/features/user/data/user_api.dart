import 'package:bitapp/common/data/app_api.dart';
import 'package:bitapp/features/user/data/user_model.dart';
import 'package:http/http.dart' as http;
import 'package:bitapp/common/util/language.dart';

class UserApi extends AppApi<UserModel> {
  UserApi({required super.configurationStore});

  @override
  String get dataName => 'users';

  @override
  UserModel from(String data) {
    return UserModel.fromJson(data);
  }

  @override
  List<UserModel> fromList(String data) => throw UnimplementedError();

  Future<UserModel> updateUserLanguage(String userId, Language language) async {
    final response = await http.put(
      Uri.parse('${await baseUrl}/v1/users/$userId'),
      headers: await buildRequestHeaders(),
      body: '{"language": "${language.value}"}',
    );

    if (response.statusCode == 200) {
      return UserModel.fromJson(response.body);
    } else {
      throw Exception(
        'Failed to update user preferred language. Response: ${response.statusCode} ${response.request?.url}',
      );
    }
  }
}
