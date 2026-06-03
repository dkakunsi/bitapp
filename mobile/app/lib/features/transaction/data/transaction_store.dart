import 'package:bitapp/common/data/app_store.dart';
import 'package:bitapp/features/transaction/data/transaction_model.dart';
import 'package:bitapp/features/transaction/domain/transaction_type.dart' as app_transaction;
import 'package:sembast/sembast_io.dart';

class TransactionStore extends AppStore<TransactionModel> {
  TransactionStore(Database database) : super(database, 'transaction');

  @override
  TransactionModel from(Map<String, dynamic> data) {
    return TransactionModel.from(data);
  }

  Future<List<TransactionModel>> getListByAccount(
    String accountId,
  ) async {
    var finder = Finder(
      filter:
          Filter.equals('sourceId', accountId) |
          Filter.equals('destinationId', accountId),
    );
    final records = await store.find(database, finder: finder);
    return records.map((record) => from(record.value)).toList();
  }

  Future<List<TransactionModel>> getListByLoan(String loanId) async {
    var finder = Finder(filter: Filter.equals('loanId', loanId));
    final records = await store.find(database, finder: finder);
    return records.map((record) => from(record.value)).toList();
  }

  Future<List<TransactionModel>> getListByTypeAndDateRange(
    String user,
    app_transaction.TransactionType type,
    int start,
    int end,
  ) async {
    var finder = Finder(
      filter: Filter.and([
        Filter.equals('user', user),
        Filter.equals('type', type.value),
        Filter.greaterThanOrEquals('date', start),
        Filter.lessThanOrEquals('date', end),
      ]),
    );
    final records = await store.find(database, finder: finder);
    return records.map((record) => from(record.value)).toList();
  }

  Future<void> deleteByAccountId(String id) async {
    var finder = Finder(
      filter:
          Filter.equals('sourceId', id) | Filter.equals('destinationId', id),
    );
    await store.delete(database, finder: finder);
  }

  Future<void> deleteByLoanId(String id) async {
    var finder = Finder(filter: Filter.equals('loanId', id));
    await store.delete(database, finder: finder);
  }
}
