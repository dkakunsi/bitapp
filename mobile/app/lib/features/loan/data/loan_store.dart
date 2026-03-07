import 'package:bitapp/common/data/app_store.dart';
import 'package:bitapp/features/loan/data/loan.dart';
import 'package:sembast/sembast_io.dart';

class LoanStore extends AppStore<Loan> {
  LoanStore(Database database) : super(database, 'loan');

  @override
  Loan from(Map<String, dynamic> data) {
    return Loan.from(data);
  }

  Future<List<Loan>> getListByType(String userId, LoanType type) async {
    var finder = Finder(
      filter: Filter.equals('user', userId) & Filter.equals('type', type.value),
    );
    final records = await store.find(database, finder: finder);
    return records.map((record) => from(record.value)).toList();
  }
}
