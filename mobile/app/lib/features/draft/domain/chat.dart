import 'dart:convert';

import 'package:bitapp/features/draft/domain/chat_type.dart';

class Chat {
  final String draftId;
  final ChatType type;
  final String message;
  final String language;

  Chat({
    required this.draftId,
    required this.type,
    required this.message,
    required this.language,
  });

  String toRequestJson() => jsonEncode({
    'type': type.apiValue,
    'draftId': draftId,
    'message': message,
    'language': language,
  });
}
