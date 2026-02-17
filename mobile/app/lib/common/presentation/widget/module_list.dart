import 'package:bitapp/common/common.dart';
import 'package:flutter/material.dart';

class ModuleList extends StatelessWidget {
  final List<ModuleConfig> modules;

  const ModuleList({super.key, required this.modules});

  @override
  Widget build(BuildContext context) {
    if (modules.isEmpty) {
      return Container();
    }

    return BoxContainer(
      child: Row(
        children: [
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children:
                modules
                    .map(
                      (e) => ModuleNavigationButton(
                        title: e.title,
                        routeName: e.routeName,
                        onClick: e.onOpening,
                      ),
                    )
                    .toList(),
          ),
        ],
      ),
    );
  }
}

class ModuleConfig {
  final String title;
  final String routeName;
  final Function? onOpening;

  ModuleConfig({required this.title, required this.routeName, this.onOpening});
}
