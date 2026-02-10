import 'dart:convert';

import 'package:app_common/src/data/model/api_data.dart';
import 'package:logging/logging.dart';

class Log implements ApiData {
  final Level level;
  final String message;
  final DateTime timestamp;
  final String loggerName;

  Log({
    required this.timestamp,
    this.level = Level.INFO,
    this.message = '',
    this.loggerName = '',
  });

  @override
  String toRequestJson() {
    final record = {
      'level': level.name,
      'name': loggerName,
      'times': timestamp.toIso8601String(),
      'message': message,
    };
    return jsonEncode(record);
  }
}
