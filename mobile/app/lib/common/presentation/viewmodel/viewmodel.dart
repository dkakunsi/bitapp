import 'package:flutter/material.dart';

abstract class ViewModel {}

abstract class AmountViewModel {
  double get amount;
  Color get amountColor;
  bool get showPaid => false;
}

abstract class CategoryViewModel extends ViewModel {
  String? get category;
  Color? get categoryColor;
}

abstract class IconViewModel extends ViewModel {
  IconData get icon;
}

abstract class ColorViewModel {
  Color get color;
}

abstract class DateViewModel extends ViewModel {
  DateTime? get date;
}

@Deprecated('Use context etension instead')
abstract class ImageViewModel extends ViewModel {
  ImageProvider get image;

  static ImageProvider defaultImage = const AssetImage(
    'assets/images/default.png',
  );
}

abstract class ListViewModel extends ViewModel
    implements Comparable<ListViewModel> {
  String get title;
  String get subtitle => '';
  String? get objectType;
}
