import 'package:bitapp/common/data/app_api.dart';
import 'package:http/http.dart' as http;
import 'package:bitapp/features/user/data/user.dart';
import 'package:bitapp/common/util/language.dart';

class UserApi extends AppApi<User> {
  UserApi({required super.configurationStore});

  @override
  String get dataName => 'user';

  @override
  User from(String data) {
    return User.fromJson(data)!;
  }

  @override
  List<User> fromList(String data) => throw UnimplementedError();

  Future<User> updateUserLanguage(String userId, Language language) async {
    final response = await http.put(
      Uri.parse('${await baseUrl}/v1/user/$userId/language/${language.value}'),
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ${await token}',
      },
    );

    if (response.statusCode == 200) {
      return User.fromJson(response.body)!;
    } else {
      throw Exception(
        'Failed to update user preferred language. Response: ${response.statusCode} ${response.request?.url}',
      );
    }
  }
}
