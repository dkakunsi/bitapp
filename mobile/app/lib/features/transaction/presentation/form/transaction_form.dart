import 'package:bitapp/common/presentation/app_style.dart';
import 'package:bitapp/common/presentation/widget/app_form.dart';
import 'package:bitapp/common/presentation/widget/app_modal.dart';
import 'package:bitapp/common/presentation/widget/app_select_dialog.dart';
import 'package:bitapp/common/util/formatter.dart';
import 'package:bitapp/features/account/domain/account.dart';
import 'package:bitapp/features/authentication/extension/session_extension.dart';
import 'package:bitapp/features/loan/domain/loan.dart';
import 'package:bitapp/features/loan/domain/loan_type.dart';
import 'package:bitapp/features/transaction/domain/transaction_category.dart';
import 'package:bitapp/features/transaction/domain/transaction_type.dart';
import 'package:bitapp/l10n/localization_extension.dart';
import 'package:bitapp/features/account/presentation/bloc/account_bloc.dart';
import 'package:bitapp/features/loan/presentation/bloc/loan_bloc.dart';
import 'package:bitapp/features/transaction/presentation/bloc/transaction_bloc.dart';
import 'package:bitapp/features/transaction/extension/transaction_category_extension.dart';
import 'package:bitapp/features/account/presentation/viewmodel/account_viewmodel.dart';
import 'package:bitapp/features/loan/presentation/viewmodel/loan_viewmodel.dart';
import 'package:bitapp/features/transaction/presentation/viewmodel/transaction_viewmodel.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class TransactionForm extends AppForm<TransactionViewModel> {
  final TransactionType transactionType;

  // If set, hide the loan field
  final LoanViewModel? loan;
  // If set, hide the source account field
  final AccountViewModel? sourceAccount;
  // If set, hide the destination account field
  final AccountViewModel? destinationAccount;

  final String title;

  const TransactionForm({
    super.key,
    required this.transactionType,
    TransactionViewModel? transaction,
    this.loan,
    this.sourceAccount,
    this.destinationAccount,
    required this.title,
  }) : super(viewModel: transaction);

  @override
  State<StatefulWidget> createState() => TransactionFormState();

  @override
  String get formTitle => title;

  @override
  bool showSaveButton() => !isEditing;
}

class TransactionFormState
    extends AppFormState<TransactionForm, TransactionViewModel> {
  late final List<TransactionCategoryOption> _availableCategories;

  final TextEditingController _titleController = TextEditingController();
  final TextEditingController _descriptionController = TextEditingController();
  final TextEditingController _amountController = TextEditingController();
  final TextEditingController _dateController = TextEditingController();
  final TextEditingController _timeController = TextEditingController();
  final TextEditingController _sourceController = TextEditingController();
  final TextEditingController _destinationController = TextEditingController();
  final TextEditingController _loanController = TextEditingController();

  Account? _sourceAccount;
  Account? _destinationAccount;
  Loan? _loan;
  LoanType? _loanType;
  DateTime? _transactionDate;
  TimeOfDay? _transactionTime;
  String _amount = '';
  TransactionCategory? _category;

  TransactionType get _transactionType => widget.transactionType;
  LoanViewModel? get _loanViewModel => widget.loan;
  AccountViewModel? get _sourceAccountViewModel => widget.sourceAccount;
  AccountViewModel? get _destinationAccountViewModel => widget.destinationAccount;

  @override
  void initState() {
    super.initState();

    _availableCategories =
        context.availableCategories.entries
            .map((e) => TransactionCategoryOption(name: e.key, label: e.value))
            .toList();
    _availableCategories.sort((a, b) => a.label.compareTo(b.label));

    _transactionDate = DateTime.now();
    _transactionTime = TimeOfDay.now();

    if (_loanViewModel != null) {
      _loan = _loanViewModel!.loan;
      _loanType = _loanViewModel!.type;
      _loanController.text = _loanViewModel!.title;
      _amountController.text = _loanViewModel!.amount.toCurrencyFormat(context);
    }

    if (_sourceAccountViewModel != null) {
      _sourceAccount = _sourceAccountViewModel!.account;
      _sourceController.text = _sourceAccountViewModel!.name;
    }

    if (_destinationAccountViewModel != null) {
      _destinationAccount = _destinationAccountViewModel!.account;
      _destinationController.text = _destinationAccountViewModel!.name;
    }

    if (viewModel != null) {
      _titleController.text = viewModel!.title;
      _descriptionController.text = viewModel!.description ?? '';
      _amountController.text = viewModel!.amount.toCurrencyFormat(context);
      _transactionDate = viewModel!.date;
      _transactionTime = viewModel!.time;
      _sourceAccount = viewModel!.sourceAccount;
      _sourceController.text = viewModel!.sourceAccountName;
      _destinationAccount = viewModel!.destinationAccount;
      _destinationController.text = viewModel!.destinationAccountName;
      _loan = viewModel!.loan;
      _loanType = viewModel!.loanType;
      _loanController.text = viewModel!.loanTitle;
      _category = viewModel!.transactionCategory;
    }

    _amount = _amountController.text;
    _amountController.addListener(_onAmountChanged);

    _dateController.text = _transactionDate!.toDateFormat(context);
    _timeController.text = _transactionTime!.toTimeFormat();
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
  String? onSave(BuildContext context) {
    if (_transactionType == TransactionType.credit &&
        _destinationAccount == null) {
      return context.locale.destinationAccountNotProvided;
    }
    if (_transactionType == TransactionType.debit && _sourceAccount == null) {
      return context.locale.sourceAccountNotProvided;
    }
    if (_transactionType == TransactionType.transfer &&
        (_sourceAccount == null || _destinationAccount == null)) {
      return context.locale.accountsNotProvided;
    }
    if (_loanType == LoanType.debt &&
        _transactionType != TransactionType.debit) {
      return context.locale.debitShouldRelateToDebtLoan;
    }
    if (_loanType == LoanType.receivable &&
        _transactionType != TransactionType.credit) {
      return context.locale.creditShouldRelateToReceivableLoan;
    }
    if (_sourceAccount == _destinationAccount) {
      return context.locale.accountsShouldBeDifferent;
    }

    context.read<TransactionBloc>().add(
      AddTransaction(
        user: context.user!,
        title: _titleController.text,
        description: _descriptionController.text,
        amount: _parsingAmount.isEmpty ? 0 : double.parse(_parsingAmount),
        date: _transactionDate ?? DateTime.now(),
        time: _transactionTime ?? TimeOfDay.now(),
        transactionType: _transactionType,
        category: _category ?? TransactionCategory.other,
        sourceAccount: _sourceAccount,
        destinationAccount: _destinationAccount,
        loan: _loan,
      ),
    );

    return null;
  }

  @override
  String? onDelete(BuildContext context) {
    context.read<TransactionBloc>().add(DeleteTransaction(id: viewModel!.id!));
    return null;
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
                  final pickedDate = await showDatePicker(
                    context: context,
                    initialDate: DateTime.now(),
                    firstDate: DateTime(2025),
                    lastDate: DateTime(2030),
                  );
                  if (pickedDate != null) {
                    _dateController.text = await pickedDate.toDateFormatAsync();
                    setState(() {
                      _transactionDate = pickedDate;
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
                  final pickedTime = await showTimePicker(
                    initialTime: TimeOfDay.now(),
                    context: context,
                  );
                  if (pickedTime != null) {
                    _timeController.text = pickedTime.toTimeFormat();
                    setState(() {
                      _transactionTime = pickedTime;
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
        SizedBox(
          height: 50,
          child: DropdownButtonFormField<TransactionCategory>(
            style: TextStyles.appDetail(),
            decoration: InputDecoration(
              labelText: context.locale.transactionCategory,
              labelStyle: labelStyle,
              enabledBorder: borderSettings,
              border: borderSettings,
            ),
            initialValue: _category,
            items:
                _availableCategories
                    .where((entry) => entry.name.canShow(_transactionType))
                    .map(
                      (entry) => DropdownMenuItem<TransactionCategory>(
                        value: entry.name,
                        child: Text(entry.label),
                      ),
                    )
                    .toList(),
            onChanged: (TransactionCategory? newValue) {
              setState(() {
                _category = newValue!;
              });
            },
          ),
        ),

        AppModalText(
          label: context.locale.selectSourceAccount,
          controller: _sourceController,
          visible:
              (_transactionType == TransactionType.debit ||
                  _transactionType == TransactionType.transfer) &&
              _sourceAccountViewModel == null,
          onTap: () async {
            final pickedAccount =
                await showDialog(
                      context: context,
                      builder: _buildAccountPickerDialog,
                    )
                    as AccountViewModel?;
            if (pickedAccount != null) {
              _sourceController.text = pickedAccount.name;
              setState(() {
                _sourceAccount = pickedAccount.account;
              });
            }
          },
        ),
        AppModalText(
          label: context.locale.selectDestinationAccount,
          controller: _destinationController,
          visible:
              (_transactionType == TransactionType.credit ||
                  _transactionType == TransactionType.transfer) &&
              _destinationAccountViewModel == null,
          onTap: () async {
            final pickedAccount =
                await showDialog(
                      context: context,
                      builder: _buildAccountPickerDialog,
                    )
                    as AccountViewModel?;
            if (pickedAccount != null) {
              _destinationController.text = pickedAccount.name;
              setState(() {
                _destinationAccount = pickedAccount.account;
              });
            }
          },
        ),
        AppModalText(
          label:
              _transactionType == TransactionType.credit
                  ? context.locale.selectReceivable
                  : context.locale.selectDebt,
          controller: _loanController,
          visible:
              (_transactionType == TransactionType.credit ||
                  _transactionType == TransactionType.debit) &&
              _loanViewModel == null,
          onTap: () async {
            final pickedLoan =
                await showDialog(
                      context: context,
                      builder: _buildLoanPickerDialog,
                    )
                    as LoanViewModel?;
            if (pickedLoan != null) {
              _loanController.text = pickedLoan.title;
              _amountController.text =
                  await pickedLoan.amount.toCurrencyFormatAsync();
              _amount = pickedLoan.amount.toString();
              setState(() {
                _loan = pickedLoan.loan;
              });
            }
          },
        ),
        AppModalText(
          label: context.locale.inputAmount,
          controller: _amountController,
        ),
      ],
    );
  }

  Dialog _buildAccountPickerDialog(BuildContext context) {
    return Dialog(
      child: AppSelectDialog<
        AccountBloc,
        AccountEvent,
        AccountState,
        AccountsRetrieved
      >(
        loadData:
            (context) => context.read<AccountBloc>().add(
              GetAccounts(user: context.user!),
            ),
      ),
    );
  }

  Dialog _buildLoanPickerDialog(BuildContext context) {
    return Dialog(
      child: AppSelectDialog<LoanBloc, LoanEvent, LoanState, LoansRetrieved>(
        loadData:
            (context) =>
                context.read<LoanBloc>().add(GetLoans(userId: context.user!.id)),
        filter: (l) {
          l as LoanViewModel;
          return (l.type == LoanType.debt &&
                  widget.transactionType == TransactionType.debit) ||
              (l.type == LoanType.receivable &&
                  widget.transactionType == TransactionType.credit);
        },
      ),
    );
  }
}

class TransactionCategoryOption {
  final TransactionCategory name;
  final String label;

  const TransactionCategoryOption({required this.name, required this.label});
}
