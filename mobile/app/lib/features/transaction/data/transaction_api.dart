import 'package:bitapp/common/data/app_api.dart';
import 'package:bitapp/features/transaction/data/transaction_model.dart';
import 'package:http/http.dart' as http;

class TransactionApi extends AppApi<TransactionModel> {
  TransactionApi({required super.configurationStore});

  @override
  String get dataName => 'transaction';

  @override
  List<TransactionModel> fromList(String data) {
    return TransactionModel.fromListResponsePayload(data);
  }

  @override
  TransactionModel from(String data) {
    return TransactionModel.fromResponsePayload(data);
  }

  Future<List<TransactionModel>> fetchTransactionsByAccount(String accountId) async {
    final response = await http.get(
      Uri.parse('${await baseUrl}/v1/accounts/$accountId/transactions'),
      headers: await buildRequestHeaders(),
    );

    if (response.statusCode == 200) {
      return TransactionModel.fromListResponsePayload(response.body);
    } else {
      throw Exception(
        'Failed to load transactions. Response: ${response.statusCode} ${response.request?.url}',
      );
    }
  }

  Future<List<TransactionModel>> fetchTransactionsByLoan(String loanId) async {
    final response = await http.get(
      Uri.parse('${await baseUrl}/v1/loans/$loanId/transactions'),
      headers: await buildRequestHeaders(),
    );

    if (response.statusCode == 200) {
      return TransactionModel.fromListResponsePayload(response.body);
    } else {
      throw Exception(
        'Failed to load transactions. Response: ${response.statusCode} ${response.request?.url}',
      );
    }
  }
}
