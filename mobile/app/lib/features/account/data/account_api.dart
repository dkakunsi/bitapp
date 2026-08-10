import 'package:bitapp/common/data/app_api.dart';
import 'package:bitapp/features/account/data/account_model.dart';

class AccountApi extends AppApi<AccountModel> {
  AccountApi({required super.configurationStore});

  @override
  String get dataName => 'accounts';

  @override
  List<AccountModel> fromList(String data) {
    return AccountModel.fromListResponsePayload(data);
  }

  @override
  AccountModel from(String data) {
    return AccountModel.fromResponsePayload(data);
  }
}
