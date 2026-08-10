import 'package:bitapp/features/draft/domain/chat_type.dart';

class Draft {
  final String id;
  final ChatType type;
  final String? modelError;
  final Map<String, dynamic>? modelResult;
  final bool success;
  final bool confirmed;

  Draft({
    required this.id,
    required this.type,
    this.modelError,
    this.modelResult,
    required this.success,
    required this.confirmed,
  });

  bool? get canConfirm => success == true && confirmed == false;
}
