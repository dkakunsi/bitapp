import 'package:bitapp/common/presentation/app_style.dart';
import 'package:bitapp/common/presentation/widget/app_button.dart';
import 'package:bitapp/common/presentation/widget/app_form.dart';
import 'package:bitapp/common/presentation/widget/app_modal.dart';
import 'package:bitapp/features/authentication/extension/session_extension.dart';
import 'package:bitapp/l10n/localization_extension.dart';
import 'package:bitapp/features/account/presentation/bloc/account_bloc.dart';
import 'package:bitapp/features/account/data/account.dart';
import 'package:bitapp/features/account/presentation/viewmodel/account_viewmodel.dart';
import 'package:flex_color_picker/flex_color_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class AccountForm extends AppForm<AccountViewModel> {
  final String title;
  const AccountForm({
    super.key,
    AccountViewModel? accountViewModel,
    required this.title,
  }) : super(viewModel: accountViewModel);

  @override
  State<StatefulWidget> createState() => AccountFormState();

  @override
  String get formTitle => title;
}

class AccountFormState extends AppFormState<AccountForm, AccountViewModel> {
  final TextEditingController _nameController = TextEditingController();
  String _accountType = '';
  Color _selectedColor = Colors.blue;

  @override
  void initState() {
    super.initState();
    _nameController.text = viewModel?.title ?? '';
    _accountType = viewModel?.objectType ?? '';
    _selectedColor = viewModel != null ? viewModel!.color : AppColor.mainLight;
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        AppModalText(
          label: context.locale.accountName,
          controller: _nameController,
        ),
        SizedBox(height: 5),
        SizedBox(
          height: 50,
          child: DropdownButtonFormField<String>(
            style: TextStyles.appDetail(),
            decoration: InputDecoration(
              labelText: context.locale.accountType,
              labelStyle: labelStyle,
              enabledBorder: borderSettings,
              border: borderSettings,
            ),
            initialValue: viewModel?.type.value,
            items:
                AccountType.types().map((String type) {
                  return DropdownMenuItem<String>(
                    value: type,
                    child: Text(type),
                  );
                }).toList(),
            onChanged: (String? newValue) {
              setState(() {
                _accountType = newValue!;
              });
            },
          ),
        ),
        SizedBox(height: 10),
        Row(
          children: [
            AppButton(
              label: context.locale.selectColor,
              height: 50,
              width: 100,
              borderWidth: 1,
              borderColor: AppColor.mainDark,
              onTap: (context) {
                showDialog(
                  context: context,
                  builder: (BuildContext context) {
                    return AlertDialog(
                      title: Text(context.locale.selectColor),
                      content: SingleChildScrollView(
                        child: ColorPicker(
                          color: _selectedColor,
                          onColorChanged: (Color color) {
                            setState(() {
                              _selectedColor = color;
                            });
                            Navigator.of(context).pop();
                          },
                          heading: Text(context.locale.selectColor),
                          subheading: Text(context.locale.selectColor),
                        ),
                      ),
                      actions: <Widget>[
                        TextButton(
                          child: Text(context.locale.cancel),
                          onPressed: () {
                            Navigator.of(context).pop();
                          },
                        ),
                      ],
                    );
                  },
                );
              },
            ),
            SizedBox(width: 20),
            Container(
              width: 40,
              height: 40,
              decoration: BoxDecoration(
                color: _selectedColor,
                shape: BoxShape.circle,
                border: Border.all(color: AppColor.mainDark, width: 1),
              ),
            ),
          ],
        ),
      ],
    );
  }

  @override
  String? onSave(BuildContext context) {
    if (isEditing) {
      _updateAccount(context);
    } else {
      _createAccount(context);
    }
    return null;
  }

  void _createAccount(BuildContext context) {
    context.read<AccountBloc>().add(
      AddAccount(
        userId: context.userId,
        name: _nameController.text,
        type: _accountType,
        themeColor: _selectedColor.value.toRadixString(16),
      ),
    );
  }

  void _updateAccount(BuildContext context) {
    context.read<AccountBloc>().add(
      UpdateAccount(
        id: viewModel!.id!,
        userId: context.userId,
        name: _nameController.text,
        type: _accountType,
        themeColor: _selectedColor.value.toRadixString(16),
      ),
    );
  }

  @override
  String? onDelete(BuildContext context) {
    context.read<AccountBloc>().add(DeleteAccount(id: viewModel!.id!));
    return null;
  }
}
