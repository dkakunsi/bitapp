import 'package:bitapp/common/presentation/viewmodel/viewmodel.dart';
import 'package:bitapp/features/user/domain/user.dart';

class UserViewModel implements ViewModel {
  final User _user;

  UserViewModel(this._user);

  String get name => _user.name;

  String get email => _user.email;

  String get phone => _user.phone ?? '-';

  User get user => _user;
}
