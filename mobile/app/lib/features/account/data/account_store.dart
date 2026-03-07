import 'package:bitapp/common/data/app_store.dart';
import 'package:bitapp/features/account/data/account.dart';
import 'package:sembast/sembast_io.dart';

class AccountStore extends AppStore<Account> {
  AccountStore(Database database) : super(database, 'account');

  @override
  Account from(Map<String, dynamic> data) {
    return Account.from(data);
  }
}
