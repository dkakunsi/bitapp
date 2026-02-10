import 'package:app_common/app_common.dart';
import 'package:flutter/material.dart';

class AppModal<AF extends AppForm<VM>, VM extends ViewModel>
    extends StatelessWidget {
  final AppForm<VM> modalContent;
  final GlobalKey<AppFormState<AF, VM>> modalKey;
  final String deleteLabel;
  final String saveLabel;
  final String? routeOnDelete;

  AppModal({
    super.key,
    required this.modalContent,
    required this.modalKey,
    required this.deleteLabel,
    required this.saveLabel,
    this.routeOnDelete,
  });

  @override
  Widget build(BuildContext context) {
    return Dialog.fullscreen(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Padding(
            padding: EdgeInsets.only(top: 10, left: 20, right: 20, bottom: 10),
            child: _buildModalHeader(context),
          ),
          Divider(),
          Padding(
            padding: EdgeInsets.only(top: 10, left: 20, right: 20, bottom: 20),
            child: SingleChildScrollView(child: modalContent),
          ),
        ],
      ),
    );
  }

  Widget _buildModalHeader(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Padding(
          padding: EdgeInsets.only(left: 20),
          child: Text(
            modalContent.formTitle,
            style: TextStyles.appMain(fontWeight: FontWeight.bold),
          ),
        ),
        Row(
          children: [
            AppButton(
              label: deleteLabel,
              color: AppColor.red,
              textColor: AppColor.white,
              height: 30,
              onTap: (context) {
                final errorMessage = modalKey.currentState!.onDelete(context);
                if (errorMessage != null) {
                  context.errorMessage(errorMessage);
                  return;
                }
                Navigator.of(context, rootNavigator: true).pop();
                if (routeOnDelete != null) {
                  context.nextRoute(routeOnDelete!);
                }
              },
              visible: modalContent.showDeleteButton(),
            ),
            AppButton(
              label: saveLabel,
              color: AppColor.mainLight,
              textColor: AppColor.white,
              height: 30,
              onTap: (context) {
                final errorMessage = modalKey.currentState!.onSave(context);
                if (errorMessage != null) {
                  context.errorMessage(errorMessage);
                  return;
                }
                Navigator.of(context, rootNavigator: true).pop();
              },
              visible: modalContent.showSaveButton(),
              padding: EdgeInsets.only(left: 10),
            ),
          ],
        ),
      ],
    );
  }
}

class AppModalText extends StatelessWidget {
  final borderSettings = OutlineInputBorder(
    borderRadius: BorderRadius.circular(10),
    borderSide: const BorderSide(color: AppColor.mainDark, width: 1),
  );

  final labelStyle = TextStyles.appDetail(
    fontColor: AppColor.disabledDark,
    fontSize: AppFontSize.small,
  );

  final String label;
  final TextEditingController controller;
  final Function? onTap;
  final bool visible;

  AppModalText({
    super.key,
    required this.label,
    required this.controller,
    this.onTap,
    this.visible = true,
  });

  @override
  Widget build(BuildContext context) {
    return Visibility(
      visible: visible,
      child: Padding(
        padding: EdgeInsets.only(top: 5, bottom: 5),
        child: SizedBox(
          height: 50,
          child: TextField(
            style: TextStyles.appDetail(),
            decoration: InputDecoration(
              labelText: label,
              labelStyle: labelStyle,
              enabledBorder: borderSettings,
              border: borderSettings,
            ),
            onTap: () {
              if (onTap != null) {
                onTap!();
              }
            },
            controller: controller,
          ),
        ),
      ),
    );
  }
}

class AppOptionModal<
  AFS extends AppFormState<AF, VM>,
  AF extends AppForm<VM>,
  VM extends ViewModel
>
    extends StatelessWidget {
  final Map<String, AppForm<VM> Function(GlobalKey<AFS>)> options;

  final String saveLabel;
  final String deleteLabel;

  AppOptionModal({
    super.key,
    required this.options,
    required this.saveLabel,
    required this.deleteLabel,
  });

  List<Widget> _items(BuildContext context) {
    return options.entries
        .map(
          (entry) => Padding(
            padding: EdgeInsets.only(top: 8, bottom: 8),
            child: InkWell(
              child: Text(entry.key, style: TextStyles.appDetail()),
              onTap: () {
                Navigator.of(context, rootNavigator: true).pop();
                showDialog(
                  context: context,
                  builder: (_) {
                    final key = GlobalKey<AFS>();
                    return AppModal<AF, VM>(
                      modalKey: key,
                      modalContent: entry.value(key),
                      deleteLabel: deleteLabel,
                      saveLabel: saveLabel,
                    );
                  },
                );
              },
            ),
          ),
        )
        .toList();
  }

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Expanded(
          child: Padding(
            padding: EdgeInsets.only(top: 20, left: 20, right: 20, bottom: 50),
            child: SingleChildScrollView(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: _items(context),
              ),
            ),
          ),
        ),
      ],
    );
  }
}
