import 'package:bitapp/common/util/language.dart';
import 'package:bitapp/features/user/data/user_model.dart';

class User {
  final String id;
  final String name;
  final String email;
  final String? phone;
  final String? photoUrl;
  final Language? language;

  User({
    required this.id,
    required this.name,
    required this.email,
    this.phone,
    this.photoUrl,
    this.language,
  });

  UserModel toModel() => UserModel(
    id: id,
    name: name,
    email: email,
    phone: phone,
    photoUrl: photoUrl,
    language: language,
  );

  static User fromModel(UserModel userModel) => User(
    id: userModel.id,
    name: userModel.name,
    email: userModel.email,
    phone: userModel.phone,
    photoUrl: userModel.photoUrl,
    language: userModel.language,
  );
}
