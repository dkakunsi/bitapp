import 'package:app_common/app_common.dart';
import 'package:flutter/material.dart';

class ModuleName extends StatelessWidget {
  final String moduleName;
  final String appLogo;

  const ModuleName({
    super.key,
    required this.moduleName,
    required this.appLogo,
  });

  @override
  Widget build(BuildContext context) => Row(
    mainAxisAlignment: MainAxisAlignment.start,
    children: [
      Image.asset(appLogo, height: 40, width: 40),
      SizedBox(width: 8),
      Text(moduleName, style: TextStyles.appMain(fontSize: 28)),
    ],
  );
}
