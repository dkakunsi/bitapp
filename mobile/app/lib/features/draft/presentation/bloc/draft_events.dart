part of 'draft_bloc.dart';

abstract class DraftEvent {}

class CreateDraft extends DraftEvent {
  final String draftId;
  final ChatType type;
  final String message;
  final String language;

  CreateDraft({
    required this.draftId,
    required this.type,
    required this.message,
    required this.language,
  });

  Chat getChat() {
    return Chat(
      draftId: draftId,
      type: type,
      message: message,
      language: language,
    );
  }
}

class ConfirmDraft extends DraftEvent {
  final String draftId;

  ConfirmDraft({required this.draftId});
}
