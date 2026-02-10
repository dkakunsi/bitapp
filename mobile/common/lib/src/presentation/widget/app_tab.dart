import 'package:app_common/app_common.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_speed_dial/flutter_speed_dial.dart';

class AppTab<
  BLOC extends AppTabBloc<EVENT, STATE>,
  STATE extends AppTabState,
  EVENT extends AppTabEvent
>
    extends StatelessWidget {
  final Map<String, AppTabPage> sections;
  final SelectTabEvent Function(String label)? createEvent;

  const AppTab({super.key, required this.sections, this.createEvent})
    : assert(
        createEvent == null || createEvent is EVENT Function(String),
        'createEvent must be a function that returns an EVENT or null',
      );

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<BLOC, STATE>(
      builder: (context, state) {
        if (state is! AppTabSelected) {
          return Container();
        }
        final selectedState = state as AppTabSelected;
        return Column(
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children:
                  sections.entries.map((s) {
                    return _SelectButton(
                      label: s.key,
                      selected: selectedState.label,
                      onTap: (context) {
                        if (createEvent != null) {
                          context.read<BLOC>().add(
                            createEvent!(s.key) as EVENT,
                          );
                        }
                      },
                    );
                  }).toList(),
            ),
            SizedBox(height: 24),
            sections[selectedState.label] ?? sections.values.first,
          ],
        );
      },
    );
  }
}

class _SelectButton extends AppButton {
  final String selected;

  _SelectButton({
    required super.label,
    required super.onTap,
    required this.selected,
  }) : super(
         height: 30,
         width: 90,
         color: selected == label ? AppColor.mainDark : AppColor.disabledLight,
         textColor: selected == label ? AppColor.white : AppColor.disabledDark,
       );
}

abstract class AppTabPage extends StatelessWidget {
  const AppTabPage({super.key});
  bool get showHeader => false;
  List<SpeedDialChild> buildFloatingActionButtons(BuildContext context) => [];
  void onLoad(BuildContext context) {}
}

class AppTabPageHeader extends StatelessWidget {
  final String headerLabel;
  final String? analyticLabel;
  final String? analyticRoute;

  AppTabPageHeader({
    super.key,
    required this.headerLabel,
    this.analyticLabel,
    this.analyticRoute,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(
          headerLabel,
          style: TextStyles.appDetail(fontSize: AppFontSize.small),
        ),
        Row(
          children: [
            analyticRoute != null ? _buildAnalyticButton(context) : Container(),
          ],
        ),
      ],
    );
  }

  Widget _buildAnalyticButton(BuildContext context) {
    return Padding(
      padding: EdgeInsets.only(left: 8),
      child: AppButton(
        label: analyticLabel!,
        color: AppColor.mainDark,
        textColor: AppColor.white,
        onTap: (context) => context.nextRoute(analyticRoute!),
        icon: Icon(Icons.insights_outlined, size: 16, color: AppColor.white),
        sideIcon: true,
      ),
    );
  }
}
