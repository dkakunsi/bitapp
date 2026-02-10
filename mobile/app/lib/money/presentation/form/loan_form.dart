import 'package:app_common/app_common.dart';
import 'package:bitapp/l10n/localization_extension.dart';
import 'package:bitapp/money/bloc/loan/loan_bloc.dart';
import 'package:bitapp/money/data/model/loan.dart';
import 'package:bitapp/money/presentation/viewmodel/loan_viewmodel.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class LoanForm extends AppForm<LoanViewModel> {
  final LoanType type;
  final String title;

  const LoanForm({
    super.key,
    LoanViewModel? loanViewModel,
    required this.type,
    required this.title,
  }) : super(viewModel: loanViewModel);

  @override
  State<StatefulWidget> createState() => LoanFormState();

  @override
  String get formTitle => title;
}

class LoanFormState extends AppFormState<LoanForm, LoanViewModel> {
  final TextEditingController _titleController = TextEditingController();
  final TextEditingController _descriptionController = TextEditingController();
  final TextEditingController _amountController = TextEditingController();
  final TextEditingController _partyNameController = TextEditingController();
  final TextEditingController _dateController = TextEditingController();
  final TextEditingController _timeController = TextEditingController();

  DateTime? _loanDate;
  TimeOfDay? _loanTime;
  String _amount = '';

  LoanType get _type => widget.type;

  @override
  void initState() {
    super.initState();

    _loanDate = DateTime.now();
    _loanTime = TimeOfDay.now();

    if (viewModel != null) {
      _titleController.text = viewModel!.title;
      _descriptionController.text = viewModel!.description;
      _amountController.text = viewModel!.principalAmount.toCurrencyFormat(
        context,
      );
      _partyNameController.text = viewModel!.partyName;
      _loanDate = viewModel!.date;
      _loanTime = viewModel!.time;
    }

    _amount = _amountController.text;
    _amountController.addListener(_onAmountChanged);

    _dateController.text = _loanDate!.toDateFormat(context);
    _timeController.text = _loanTime!.toTimeFormat();
  }

  void _onAmountChanged() {
    final amount = _amountController.text;
    if (amount.isEmpty) return;

    final parsingAmount = _parsingAmount;
    if (parsingAmount == _amount) return;

    final parsedAmount = double.tryParse(parsingAmount);
    if (parsedAmount == null) return;

    _amount = parsingAmount;
    _amountController.text = parsedAmount.toCurrencyFormat(context);
  }

  String get _parsingAmount {
    final amount = _amountController.text;
    return amount.replaceAll(',', '').replaceAll('.', '');
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Row(
          children: [
            Expanded(
              child: AppModalText(
                label: context.locale.selectDate,
                controller: _dateController,
                onTap: () async {
                  final selectedDate = await showDatePicker(
                    context: context,
                    initialDate: _loanDate ?? DateTime.now(),
                    firstDate: DateTime(2025),
                    lastDate: DateTime(2030),
                  );
                  if (selectedDate != null) {
                    _dateController.text =
                        await selectedDate.toDateFormatAsync();
                    setState(() {
                      _loanDate = selectedDate;
                    });
                  }
                },
              ),
            ),
            const SizedBox(width: 8),
            Expanded(
              child: AppModalText(
                label: context.locale.selectTime,
                controller: _timeController,
                onTap: () async {
                  final selectedTime = await showTimePicker(
                    context: context,
                    initialTime: _loanTime ?? TimeOfDay.now(),
                  );
                  if (selectedTime != null) {
                    _timeController.text = selectedTime.toTimeFormat();
                    setState(() {
                      _loanTime = selectedTime;
                    });
                  }
                },
              ),
            ),
          ],
        ),
        AppModalText(
          label: context.locale.inputTitle,
          controller: _titleController,
        ),
        AppModalText(
          label: context.locale.inputDescription,
          controller: _descriptionController,
        ),
        AppModalText(
          label:
              _type == LoanType.debt
                  ? context.locale.inputLender
                  : context.locale.inputBorrower,
          controller: _partyNameController,
        ),
        AppModalText(
          label: context.locale.inputAmount,
          controller: _amountController,
        ),
      ],
    );
  }

  @override
  String? onSave(BuildContext context) {
    if (_parsingAmount.isEmpty || _parsingAmount == '0') {
      return context.locale.amountShouldNotBeZeroOrEmpty;
    }

    if (isEditing) {
      _updateLoan(context);
    } else {
      _createLoan(context);
    }

    return null;
  }

  void _createLoan(BuildContext context) {
    context.read<LoanBloc>().add(
      AddLoan(
        userId: context.userId,
        title: _titleController.text,
        description: _descriptionController.text,
        amount: double.parse(_parsingAmount),
        partyName: _partyNameController.text,
        date: _loanDate ?? DateTime.now(),
        time: _loanTime ?? TimeOfDay.now(),
        type: _type,
      ),
    );
  }

  void _updateLoan(BuildContext context) {
    context.read<LoanBloc>().add(
      UpdateLoan(
        id: viewModel!.id!,
        userId: context.userId,
        title: _titleController.text,
        description: _descriptionController.text,
        amount: double.parse(_parsingAmount),
        partyName: _partyNameController.text,
        date: _loanDate ?? DateTime.now(),
        time: _loanTime ?? TimeOfDay.now(),
        type: _type,
      ),
    );
  }

  @override
  String? onDelete(BuildContext context) {
    context.read<LoanBloc>().add(DeleteLoan(id: viewModel!.id!));
    return null;
  }
}
