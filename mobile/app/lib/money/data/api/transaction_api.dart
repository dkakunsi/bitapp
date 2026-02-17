import 'package:bitapp/common/common.dart';
import 'package:bitapp/money/data/model/transaction.dart';
import 'package:http/http.dart' as http;

class TransactionApi extends AppApi<Transaction> {
  TransactionApi({required super.configurationStore});

  @override
  String get dataName => 'transaction';

  @override
  List<Transaction> fromList(String data) {
    return Transaction.fromListResponsePayload(data);
  }

  @override
  Transaction from(String data) {
    return Transaction.fromResponsePayload(data);
  }

  Future<List<Transaction>> fetchTransactionsByAccount(String accountId) async {
    final response = await http.get(
      Uri.parse('${await baseUrl}/v1/account/$accountId/transactions'),
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ${await token}',
      },
    );

    if (response.statusCode == 200) {
      return Transaction.fromListResponsePayload(response.body);
    } else {
      throw Exception(
        'Failed to load transactions. Response: ${response.statusCode} ${response.request?.url}',
      );
    }
  }

  Future<List<Transaction>> fetchTransactionsByLoan(String loanId) async {
    final response = await http.get(
      Uri.parse('${await baseUrl}/v1/loan/$loanId/transactions'),
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ${await token}',
      },
    );

    if (response.statusCode == 200) {
      return Transaction.fromListResponsePayload(response.body);
    } else {
      throw Exception(
        'Failed to load transactions. Response: ${response.statusCode} ${response.request?.url}',
      );
    }
  }
}
