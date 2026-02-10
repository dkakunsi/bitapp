import 'package:app_common/app_common.dart';
import 'package:bitapp/money/data/model/loan.dart';

class LoanApi extends AppApi<Loan> {
  LoanApi({required super.configurationStore});

  @override
  String get dataName => 'loan';

  @override
  List<Loan> fromList(String data) {
    return Loan.fromListResponsePayload(data);
  }

  @override
  Loan from(String data) {
    return Loan.fromResponsePayload(data);
  }
}
