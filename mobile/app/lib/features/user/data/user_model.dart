import 'dart:convert';

import 'package:bitapp/common/data/model/api_data.dart';
import 'package:bitapp/common/data/model/store_data.dart';
import 'package:bitapp/common/util/language.dart';

class UserModel implements ApiData, StoreData {
  @override
  final String id;
  final String name;
  final String email;
  final String? phone;
  final String? photoUrl;
  Language? _language;

  UserModel({
    required this.id,
    required this.name,
    required this.email,
    this.phone,
    this.photoUrl,
    Language? language,
  }) : _language = language;

  Language? get language => _language;

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

  static UserModel fromJson(String user) {
    final Map<String, dynamic> data = jsonDecode(user);
    return from(data);
  }

  static UserModel from(Map<String, dynamic> data) {
    final language =
        data['language'] != null
            ? Language.valueOf(data['language'] as String)
            : Language.defaultLanguage;
    final id = data['id'] ?? data['email'] as String;

    return UserModel(
      id: id,
      name: data['name'] as String,
      email: data['email'] as String,
      phone: data['phone'] as String?,
      photoUrl: data['photoUrl'] as String?,
      language: language,
    );
  }

  UserModel updateLanguage(Language? language) {
    _language = language;
    return this;
  }
}
