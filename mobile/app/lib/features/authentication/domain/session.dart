import 'package:bitapp/features/user/domain/user.dart';

class Session {
  final String token;

  /// Firebase User Unieuq ID
  final String userUID;
  final String name;
  final String email;
  final String? phone;
  final String? photoUrl;

  Session({
    required this.token,
    required this.userUID,
    required this.name,
    required this.email,
    this.phone,
    this.photoUrl,
  });

  User get user => User(
    id: email,
    name: name,
    email: email,
    phone: phone,
    photoUrl: photoUrl,
  );
}
