import 'package:bitapp/common/util/formatter.dart';
import 'package:bitapp/common/util/language.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:intl/date_symbol_data_local.dart';

void main() {
  setUpAll(() async {
    await initializeDateFormatting('en_US');
    await initializeDateFormatting('id_ID');
  });

  group('TimeFormatter', () {
    test('toInt converts hour and minute to minutes since midnight', () {
      const time = TimeOfDay(hour: 14, minute: 30);

      expect(time.toInt(), 870);
    });

    test('fromInt converts minutes since midnight back to TimeOfDay', () {
      final time = TimeFormatter.fromInt(870);

      expect(time.hour, 14);
      expect(time.minute, 30);
    });

    test('toInt and fromInt are inverse operations', () {
      const original = TimeOfDay(hour: 23, minute: 59);

      final converted = TimeFormatter.fromInt(original.toInt());

      expect(converted.hour, original.hour);
      expect(converted.minute, original.minute);
    });

    test('toTimeFormat returns HH:mm for English locale', () {
      const time = TimeOfDay(hour: 6, minute: 5);

      final formatted = time.toTimeFormat(language: Language.en);

      expect(formatted, '06:05');
    });

    test('toTimeFormat returns HH:mm for Indonesian locale', () {
      const time = TimeOfDay(hour: 17, minute: 45);

      final formatted = time.toTimeFormat(language: Language.id);

      expect(formatted, '17:45');
    });
  });
}
