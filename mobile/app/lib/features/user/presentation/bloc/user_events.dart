part of 'user_bloc.dart';

abstract class UserEvent extends Equatable {
  const UserEvent();

  @override
  List<Object?> get props => [];
}

class RegisterUser extends UserEvent {
  final String userId;
  final String name;
  final String email;
  final String? phone;
  final String? photoUrl;

  const RegisterUser({
    required this.userId,
    required this.name,
    required this.email,
    this.phone,
    this.photoUrl,
  });

  @override
  List<Object?> get props => [userId, name, email, phone, photoUrl];
}

class UpdateUserLanguage extends UserEvent {
  final String userId;
  final Language language;

  const UpdateUserLanguage({required this.userId, required this.language});

  @override
  List<Object?> get props => [userId, language];
}
