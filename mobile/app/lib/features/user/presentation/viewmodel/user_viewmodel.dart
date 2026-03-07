import 'package:bitapp/common/presentation/viewmodel/viewmodel.dart';
import 'package:bitapp/features/user/data/user.dart';

class UserViewModel extends ViewModel {
  final User _user;

  UserViewModel(this._user);

  String get name => _user.name;

  String get email => _user.email;

  String get phone => _user.phone ?? '-';

  String? get id => _user.id;
}
