import 'package:bitapp/features/draft/domain/chat.dart';
import 'package:bitapp/features/draft/domain/chat_type.dart';
import 'package:bitapp/features/draft/domain/draft.dart';
import 'package:bitapp/features/draft/domain/draft_usecase.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

part 'draft_events.dart';
part 'draft_states.dart';

class DraftBloc extends Bloc<DraftEvent, DraftState> {
  final DraftUseCase _draftUseCase;

  DraftBloc(this._draftUseCase) : super(DraftInitial()) {
    on<CreateDraft>(_createDraft);
    on<ConfirmDraft>(_confirmDraft);
  }

  Future<void> _createDraft(CreateDraft event, Emitter<DraftState> emit) async {
    emit(DraftProcessing());
    final chat = event.getChat();
    final result = await _draftUseCase.createDraft(chat);
    if (result.isFailure) {
      emit(DraftFailed(exception: result.exception));
    } else {
      emit(DraftCreated(draft: result.data));
    }
  }

  Future<void> _confirmDraft(
    ConfirmDraft event,
    Emitter<DraftState> emit,
  ) async {
    emit(DraftProcessing());
    final result = await _draftUseCase.confirmDraft(event.draftId);
    if (result.isFailure) {
      emit(DraftFailed(exception: result.exception));
    } else {
      emit(DraftConfirmed(draftId: event.draftId));
    }
  }
}
