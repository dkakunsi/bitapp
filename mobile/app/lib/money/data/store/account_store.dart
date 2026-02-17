import 'package:bitapp/common/common.dart';
import 'package:bitapp/money/data/model/account.dart';
import 'package:sembast/sembast_io.dart';

class AccountStore extends AppStore<Account> {
  AccountStore(Database database) : super(database, 'account');

  @override
  Account from(Map<String, dynamic> data) {
    return Account.from(data);
  }
}
