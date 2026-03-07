import 'package:bitapp/features/user/data/user.dart';
import 'package:bitapp/common/data/app_store.dart';
import 'package:sembast/sembast_io.dart';

class UserStore extends AppStore<User> {
  UserStore(Database database) : super(database, 'user');

  @override
  User from(Map<String, dynamic> data) {
    return User.from(data)!;
  }
}
