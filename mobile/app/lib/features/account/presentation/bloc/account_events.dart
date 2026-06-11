part of 'account_bloc.dart';

abstract class AccountEvent extends Equatable {
  const AccountEvent();

  @override
  List<Object> get props => [];
}

class FetchAccounts extends AccountEvent {
  final User user;

  const FetchAccounts({required this.user});
}

class GetAccounts extends AccountEvent {
  final User user;

  const GetAccounts({required this.user});

  @override
  List<Object> get props => [user];
}

class GetAccount extends AccountEvent {
  final String id;

  const GetAccount({required this.id});
}

class AddAccount extends AccountEvent {
  final String name;
  final String type;
  final String themeColor;
  final User user;

  const AddAccount({
    required this.user,
    required this.name,
    required this.type,
    required this.themeColor,
  });

  @override
  List<Object> get props => [user, name, type, themeColor];
}

class UpdateAccount extends AccountEvent {
  final String id;
  final String name;
  final String type;
  final String themeColor;
  final User user;

  const UpdateAccount({
    required this.id,
    required this.user,
    required this.name,
    required this.type,
    required this.themeColor,
  });

  @override
  List<Object> get props => [id, user, name, type, themeColor];
}

class DeleteAccount extends AccountEvent {
  final String id;

  const DeleteAccount({required this.id});

  @override
  List<Object> get props => [id];
}
