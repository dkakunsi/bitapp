import 'package:app_common/app_common.dart';
import 'package:app_common/src/data/api/log_api.dart';
import 'package:app_common/src/data/model/log.dart';
import 'package:logging/logging.dart';

abstract class LogConfig {
  static void configure(Level level, LogWriter writer) {
    Logger.root.level = level;
    Logger.root.onRecord.listen((LogRecord record) {
      if (record.level >= level) {
        writer.write(record);
      }
    });
  }
}

abstract class LogWriter {
  void write(LogRecord record);
}

class ConsoleWriter implements LogWriter {
  @override
  void write(LogRecord record) {
    print(
      '[${record.time}] [${record.loggerName}] ${record.level.name} ${record.message}',
    );
  }
}

class WebWriter implements LogWriter {
  final LogApi logApi = LogApi(
    configurationStore: getInstance<ConfigurationStore>(),
  );

  @override
  Future<void> write(LogRecord record) async {
    try {
      final log = Log(
        level: record.level,
        message: record.message,
        timestamp: record.time,
        loggerName: record.loggerName,
      );
      await logApi.add(log);
    } catch (e) {
      print('Error writing log to web API: $e');
    }
  }
}
