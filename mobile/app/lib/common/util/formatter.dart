import 'package:bitapp/common/common.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

extension CurrencyFormatter on double {
  static final NumberFormat _idFormat = NumberFormat('#,###', 'id_ID');
  static final NumberFormat _enFormat = NumberFormat('#,###', 'en_US');

  String toCurrencyFormat(BuildContext context) =>
      context.language == Language.id
          ? _idFormat.format(this)
          : _enFormat.format(this);

  Future<String> toCurrencyFormatAsync() async {
    final configurationStore = getInstance<ConfigurationStore>();
    final configuration = await configurationStore.get(Configuration.storeId);
    final language = configuration?.language ?? Language.defaultLanguage;
    return language == Language.id
        ? _idFormat.format(this)
        : _enFormat.format(this);
  }
}

extension DateFormatter on DateTime {
  static final DateFormat _enFormat = DateFormat('dd MMMM yyyy', 'en_US');
  static final DateFormat _idFormat = DateFormat('dd MMMM yyyy', 'id_ID');
  static final DateFormat _enPeriodFormat = DateFormat('MMMM yyyy', 'en_US');
  static final DateFormat _idPeriodFormat = DateFormat('MMMM yyyy', 'id_ID');

  String toDateFormat(BuildContext context) =>
      context.language == Language.id
          ? _idFormat.format(this)
          : _enFormat.format(this);

  Future<String> toDateFormatAsync() async {
    final configurationStore = getInstance<ConfigurationStore>();
    final configuration = await configurationStore.get(Configuration.storeId);
    final language = configuration?.language ?? Language.defaultLanguage;
    return language == Language.id
        ? _idFormat.format(this)
        : _enFormat.format(this);
  }

  String toPeriodFormat(BuildContext context) =>
      context.language == Language.id
          ? _idPeriodFormat.format(this)
          : _enPeriodFormat.format(this);
}

extension TimeFormatter on TimeOfDay {
  static final DateFormat _enTimeFormat = DateFormat('HH:mm', 'en_US');
  static final DateFormat _idTimeFormat = DateFormat('HH:mm', 'id_ID');

  String toTimeFormat({Language language = Language.defaultLanguage}) =>
      language == Language.id
          ? _idTimeFormat.format(DateTime(0, 0, 0, hour, minute))
          : _enTimeFormat.format(DateTime(0, 0, 0, hour, minute));

  int toInt() => hour * 60 + minute;

  static TimeOfDay fromInt(int value) =>
      TimeOfDay(hour: value ~/ 60, minute: value % 60);
}

@Deprecated('Convert to int instead')
Color colorFromString(String s) => Color(int.parse(s, radix: 16));
