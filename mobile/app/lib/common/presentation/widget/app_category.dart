import 'package:flutter/material.dart';

class ColoredCategory extends StatelessWidget {
  final Color color;
  final BoxShape shape;

  const ColoredCategory({
    super.key,
    required this.color,
    this.shape = BoxShape.circle,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 12,
      height: 12,
      decoration: BoxDecoration(color: color, shape: shape),
    );
  }
}
