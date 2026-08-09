import 'package:bitapp/common/data/api.dart';
import 'package:bitapp/features/draft/data/draft_model.dart';
import 'package:bitapp/features/draft/domain/chat.dart';
import 'package:http/http.dart' as http;

class DraftApi extends Api {
  DraftApi({required super.configurationStore});

  @override
  String get endpoint => 'v1/chats';

  Future<DraftModel> createDraft(Chat chat) async {
    final response = await http.post(
      Uri.parse(await url),
      headers: await buildRequestHeaders(),
      body: chat.toRequestJson(),
    );

    if (response.statusCode == 200) {
      return DraftModel.fromResponsePayload(response.body);
    }

    throw Exception(
      response.body.isNotEmpty
          ? response.body
          : 'Failed to create draft. Response: ${response.statusCode} ${response.request?.url}',
    );
  }

  Future<void> confirmDraft(String draftId) async {
    final response = await http.post(
      Uri.parse('${await url}/$draftId/confirm'),
      headers: await buildRequestHeaders(),
    );

    if (response.statusCode != 200) {
      throw Exception(
        response.body.isNotEmpty
            ? response.body
            : 'Failed to confirm draft. Response: ${response.statusCode} ${response.request?.url}',
      );
    }
  }
}
