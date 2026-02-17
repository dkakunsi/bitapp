import 'dart:convert';

import 'package:app_common/app_common.dart';

class User implements ApiData, StoreData {
  @override
  final String? id;
  final String name;
  final String email;
  final String? phone;
  final String? photoUrl;
  final Language? language;

  User({
    this.id,
    required this.name,
    required this.email,
    this.phone,
    this.photoUrl,
    this.language,
  });

  User copyWith({
    String? id,
    String? name,
    String? email,
    String? phone,
    String? photoUrl,
    Language? language,
  }) {
    return User(
      id: id ?? this.id,
      name: name ?? this.name,
      email: email ?? this.email,
      phone: phone ?? this.phone,
      photoUrl: photoUrl ?? this.photoUrl,
      language: language ?? this.language,
    );
  }

  @override
  Map<String, dynamic> toStoreJson() {
    return {
      'id': id,
      'name': name,
      'email': email,
      'photoUrl': photoUrl,
      'phone': phone,
      'language': language?.value,
    };
  }

  @override
  String toRequestJson() {
    return jsonEncode(toStoreJson());
  }

  static User? fromJson(String user) {
    if (user.isEmpty) {
      return null;
    }
    final Map<String, dynamic> data = jsonDecode(user);
    return from(data);
  }

  static User? from(Map<String, dynamic> data) {
    if (data.isEmpty) {
      return null;
    }
    return User(
      id: data['id'] as String?,
      name: data['name'] as String,
      email: data['email'] as String,
      phone: data['phone'] as String?,
      photoUrl: data['photoUrl'] as String?,
      language:
          data['language'] != null
              ? Language.valueOf(data['language'] as String)
              : Language.defaultLanguage,
    );
  }
}
