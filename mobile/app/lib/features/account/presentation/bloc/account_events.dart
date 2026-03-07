part of 'account_bloc.dart';

abstract class AccountEvent extends Equatable {
  const AccountEvent();

  @override
  List<Object> get props => [];
}

class FetchAccounts extends AccountEvent {
  final String userId;

  const FetchAccounts({required this.userId});
}

class GetAccounts extends AccountEvent {
  final String userId;

  const GetAccounts({required this.userId});

  @override
  List<Object> get props => [userId];
}

class GetAccount extends AccountEvent {
  final String id;

  const GetAccount({required this.id});
}

class AddAccount extends AccountEvent {
  final String name;
  final String type;
  final String themeColor;
  final String userId;

  const AddAccount({
    required this.userId,
    required this.name,
    required this.type,
    required this.themeColor,
  });

  @override
  List<Object> get props => [userId, name, type, themeColor];
}

class UpdateAccount extends AccountEvent {
  final String id;
  final String name;
  final String type;
  final String themeColor;
  final String userId;

  const UpdateAccount({
    required this.id,
    required this.userId,
    required this.name,
    required this.type,
    required this.themeColor,
  });

  @override
  List<Object> get props => [id, userId, name, type, themeColor];
}

class DeleteAccount extends AccountEvent {
  final String id;

  const DeleteAccount({required this.id});

  @override
  List<Object> get props => [id];
}
