import 'package:bitapp/common/data/app_api.dart';
import 'package:bitapp/features/loan/data/loan.dart';

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
