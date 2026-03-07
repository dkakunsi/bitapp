import 'package:bitapp/common/data/app_api.dart';
import 'package:bitapp/features/logging/data/log.dart';
import 'package:http/http.dart' as http;

class LogApi extends AppApi<Log> {
  LogApi({required super.configurationStore});

  @override
  String get dataName => 'log';

  @override
  Log from(String data) {
    return Log(timestamp: DateTime.now());
  }

  @override
  List<Log> fromList(String data) {
    return [from(data)];
  }

  @override
  Future<Log> add(Log t) async {
    http.post(
      Uri.parse('${await baseUrl}/v1/$dataName'),
      headers: {'Content-Type': 'application/json'},
      body: t.toRequestJson(),
    );
    return Log(timestamp: DateTime.now());
  }
}
