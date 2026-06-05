import 'package:bitapp/common/data/app_store.dart';
import 'package:bitapp/features/user/data/user_model.dart';
import 'package:sembast/sembast_io.dart';

class UserStore extends AppStore<UserModel> {
  UserStore(Database database) : super(database, 'user');

  @override
  UserModel from(Map<String, dynamic> data) {
    return UserModel.from(data);
  }
}
