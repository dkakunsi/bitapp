import 'dart:convert';

import 'package:bitapp/common/data/app_api.dart';
import 'package:bitapp/features/draft/data/draft_model.dart';
import 'package:bitapp/features/draft/domain/draft_type.dart';
import 'package:http/http.dart' as http;

class DraftApi extends AppApi<DraftModel> {
  DraftApi({required super.configurationStore});

  @override
  String get dataName => 'chat';

  @override
  List<DraftModel> fromList(String data) => [];

  @override
  DraftModel from(String data) => DraftModel.fromResponsePayload(data);

  Future<DraftModel> createDraft({
    required DraftType type,
    required String draftId,
    required String message,
    required String language,
  }) async {
    final response = await http.post(
      Uri.parse(await endpoint),
      headers: await buildRequestHeaders(),
      body: jsonEncode({
        'type': type.apiValue,
        'draftId': draftId,
        'message': message,
        'language': language,
      }),
    );

    if (response.statusCode == 200) {
      return from(response.body);
    }

    throw Exception(
      response.body.isNotEmpty
          ? response.body
          : 'Failed to create draft. Response: ${response.statusCode} ${response.request?.url}',
    );
  }

  Future<void> confirmDraft(String draftId) async {
    final response = await http.post(
      Uri.parse('${await endpoint}/$draftId/confirm'),
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
