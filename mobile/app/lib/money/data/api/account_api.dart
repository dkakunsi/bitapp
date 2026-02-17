import 'package:bitapp/common/common.dart';
import 'package:bitapp/money/data/model/account.dart';

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
