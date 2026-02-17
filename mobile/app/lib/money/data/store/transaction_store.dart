import 'package:bitapp/common/common.dart';
import 'package:bitapp/money/data/model/transaction.dart' as app_transaction;
import 'package:sembast/sembast_io.dart';

class TransactionStore extends AppStore<app_transaction.Transaction> {
  TransactionStore(Database database) : super(database, 'transaction');

  @override
  app_transaction.Transaction from(Map<String, dynamic> data) {
    return app_transaction.Transaction.from(data);
  }

  Future<List<app_transaction.Transaction>> getListByAccount(
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

  Future<List<app_transaction.Transaction>> getListByLoan(String loanId) async {
    var finder = Finder(filter: Filter.equals('loanId', loanId));
    final records = await store.find(database, finder: finder);
    return records.map((record) => from(record.value)).toList();
  }

  Future<List<app_transaction.Transaction>> getListByTypeAndDateRange(
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
