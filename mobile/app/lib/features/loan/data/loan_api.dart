import 'package:bitapp/common/data/app_api.dart';
import 'package:bitapp/features/loan/data/loan_model.dart';

class LoanApi extends AppApi<LoanModel> {
  LoanApi({required super.configurationStore});

  @override
  String get dataName => 'loan';

  @override
  List<LoanModel> fromList(String data) {
    return LoanModel.fromListResponsePayload(data);
  }

  @override
  LoanModel from(String data) {
    return LoanModel.fromResponsePayload(data);
  }
}
