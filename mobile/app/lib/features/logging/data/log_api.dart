import 'package:bitapp/common/data/api.dart';
import 'package:bitapp/features/logging/data/log.dart';
import 'package:http/http.dart' as http;

class LogApi extends Api {
  LogApi({required super.configurationStore});

  @override
  String get endpoint => 'v1/logs';

  Future<Log> log(Log t) async {
    http.post(
      Uri.parse(await url),
      headers: await buildRequestHeaders(),
      body: t.toRequestJson(),
    );
    return Log(timestamp: DateTime.now());
  }
}
