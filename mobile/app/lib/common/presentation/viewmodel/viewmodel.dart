import 'package:flutter/material.dart';

abstract class ViewModel {}

abstract class ListViewModel extends ViewModel
    implements Comparable<ListViewModel> {
  String get title;
  String get subtitle => '';
  String? get objectType;

  IconData get icon;

  double get listAmount;
  Color get listAmountColor;

  String? get category;
  Color? get categoryColor;

  DateTime? get date;
}
