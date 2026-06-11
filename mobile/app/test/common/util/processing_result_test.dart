import 'package:bitapp/common/util/processing_result.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  group('ProcessingResult', () {
    test('isSuccess true when exception is null', () {
      final result = ProcessingResult<void>();

      expect(result.isSuccess, isTrue);
      expect(result.isFailure, isFalse);
    });

    test('isFailure true when exception exists', () {
      final result = ProcessingResult<void>(exception: Exception('failed'));

      expect(result.isFailure, isTrue);
      expect(result.isSuccess, isFalse);
    });

    test('isEmpty true when data is null', () {
      final result = ProcessingResult<int>();

      expect(result.isEmpty, isTrue);
    });

    test('data returns value when success and non-empty', () {
      final result = ProcessingResult<int>(data: 42);

      expect(result.data, 42);
    });

    test('data throws original exception when failure', () {
      final exception = Exception('boom');
      final result = ProcessingResult<int>(data: 42, exception: exception);

      expect(() => result.data, throwsA(same(exception)));
    });

    test('data throws when empty', () {
      final result = ProcessingResult<int>();

      expect(() => result.data, throwsA(isA<Exception>()));
    });
  });
}
