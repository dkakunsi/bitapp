import 'package:bitapp/common/data/app_store.dart';
import 'package:bitapp/features/loan/data/loan_model.dart';
import 'package:bitapp/features/loan/domain/loan_type.dart';
import 'package:sembast/sembast_io.dart';

class LoanStore extends AppStore<LoanModel> {
  LoanStore(Database database) : super(database, 'loan');

  @override
  LoanModel from(Map<String, dynamic> data) {
    return LoanModel.from(data);
  }

  Future<List<LoanModel>> getListByType(String userId, LoanType type) async {
    var finder = Finder(
      filter: Filter.equals('user', userId) & Filter.equals('type', type.value),
    );
    final records = await store.find(database, finder: finder);
    return records.map((record) => from(record.value)).toList();
  }
}
