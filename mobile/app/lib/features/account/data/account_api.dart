import 'package:bitapp/common/data/app_api.dart';
import 'package:bitapp/features/account/data/account.dart';

class AccountApi extends AppApi<Account> {
  AccountApi({required super.configurationStore});

  @override
  String get dataName => 'account';

  @override
  List<Account> fromList(String data) {
    return Account.fromListResponsePayload(data);
  }

  @override
  Account from(String data) {
    return Account.fromResponsePayload(data);
  }
}
