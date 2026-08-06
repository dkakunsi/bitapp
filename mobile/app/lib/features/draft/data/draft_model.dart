import 'dart:convert';

import 'package:bitapp/features/draft/domain/draft_type.dart';

class DraftModel {
  final String id;
  final DraftType type;
  final String? modelError;
  final Map<String, dynamic> modelResult;
  final bool success;
  final bool confirmed;

  DraftModel({
    required this.id,
    required this.type,
    required this.modelResult,
    required this.success,
    required this.confirmed,
    this.modelError,
  });

  bool get canConfirm => success && !confirmed;

  bool get canRefine => success && !confirmed;

  String get prettyResult =>
      const JsonEncoder.withIndent('  ').convert(modelResult);

  static DraftModel fromResponsePayload(String response) {
    final data = jsonDecode(response);
    return from(Map<String, dynamic>.from(data));
  }

  static DraftModel from(Map<String, dynamic> data) {
    return DraftModel(
      id: _readId(data['id']),
      type: DraftType.fromApi(data['type']?.toString() ?? ''),
      modelError: _readNullableString(data['modelError']),
      modelResult: _readMap(data['modelResult']),
      success: data['success'] == true,
      confirmed: data['confirmed'] == true,
    );
  }

  static String _readId(dynamic value) {
    if (value is String) {
      return value;
    }
    if (value is Map) {
      return value['value']?.toString() ?? '';
    }
    return '';
  }

  static String? _readNullableString(dynamic value) {
    final text = value?.toString().trim();
    if (text == null || text.isEmpty || text == 'null') {
      return null;
    }
    return text;
  }

  static Map<String, dynamic> _readMap(dynamic value) {
    if (value is Map<String, dynamic>) {
      return value;
    }
    if (value is Map) {
      return Map<String, dynamic>.from(value);
    }
    if (value is String && value.trim().isNotEmpty) {
      final decoded = jsonDecode(value);
      if (decoded is Map) {
        return Map<String, dynamic>.from(decoded);
      }
    }
    return {};
  }
}
