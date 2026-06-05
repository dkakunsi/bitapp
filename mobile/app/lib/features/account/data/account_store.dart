import 'package:bitapp/common/data/app_store.dart';
import 'package:bitapp/features/account/data/account_model.dart';
import 'package:sembast/sembast_io.dart';

class AccountStore extends AppStore<AccountModel> {
  AccountStore(Database database) : super(database, 'account');

  @override
  AccountModel from(Map<String, dynamic> data) {
    return AccountModel.from(data);
  }
}
