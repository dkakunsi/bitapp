import 'dart:convert';

import 'package:bitapp/features/draft/domain/chat_type.dart';
import 'package:bitapp/features/draft/domain/draft.dart';

class DraftModel extends Draft {
  DraftModel({
    required super.id,
    required super.type,
    super.modelError,
    super.modelResult,
    required super.success,
    required super.confirmed,
  });

  factory DraftModel.fromResponsePayload(String response) {
    final data = jsonDecode(response);
    return DraftModel.from(Map<String, dynamic>.from(data));
  }

  factory DraftModel.from(Map<String, dynamic> data) {
    return DraftModel(
      id: _readId(data['id']),
      type: ChatType.fromApi(data['type']?.toString() ?? ''),
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
