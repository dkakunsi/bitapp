import 'package:bitapp/common/util/processing_result.dart';
import 'package:bitapp/features/draft/data/draft_api.dart';
import 'package:bitapp/features/draft/domain/chat.dart';
import 'package:bitapp/features/draft/domain/draft.dart';
import 'package:logging/logging.dart';

class DraftUseCase {
  final _logger = Logger('DraftUseCase');
  final DraftApi _draftApi;

  DraftUseCase({required DraftApi draftApi}) : _draftApi = draftApi;

  Exception _toException(Object error) {
    return error is Exception ? error : Exception(error.toString());
  }

  Future<ProcessingResult<Draft>> createDraft(Chat chat) async {
    try {
      final draft = await _draftApi.createDraft(chat);
      return ProcessingResult(data: draft);
    } catch (e, stackTrace) {
      _logger.warning('Error creating draft', e, stackTrace);
      return ProcessingResult(exception: _toException(e));
    }
  }

  Future<ProcessingResult<void>> confirmDraft(String draftId) async {
    try {
      await _draftApi.confirmDraft(draftId);
      return ProcessingResult();
    } catch (e, stackTrace) {
      _logger.warning('Error confirming draft', e, stackTrace);
      return ProcessingResult(exception: _toException(e));
    }
  }
}
