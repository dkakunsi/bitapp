import 'dart:convert';
import 'dart:io';

import 'package:bitapp/common/presentation/app_style.dart';
import 'package:bitapp/common/presentation/widget/app_button.dart';
import 'package:bitapp/common/presentation/widget/container.dart';
import 'package:bitapp/common/util/container.dart';
import 'package:bitapp/features/account/presentation/bloc/account_bloc.dart';
import 'package:bitapp/features/app/extension/navigation_extension.dart';
import 'package:bitapp/features/app/presentation/screen/app_screen.dart';
import 'package:bitapp/features/app/presentation/screen/money_screen.dart';
import 'package:bitapp/features/draft/data/draft_model.dart';
import 'package:bitapp/features/draft/domain/draft_type.dart';
import 'package:bitapp/features/draft/domain/draft_usecase.dart';
import 'package:bitapp/features/authentication/extension/session_extension.dart';
import 'package:bitapp/features/loan/presentation/bloc/loan_bloc.dart';
import 'package:bitapp/features/summary/presentation/bloc/summary_bloc.dart';
import 'package:bitapp/features/transaction/presentation/bloc/transaction_bloc.dart';
import 'package:bitapp/l10n/localization_extension.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:google_mlkit_text_recognition/google_mlkit_text_recognition.dart';
import 'package:image_picker/image_picker.dart';
import 'package:path/path.dart' as path;
import 'package:uuid/uuid.dart';

class DraftScreen extends AppScreen {
  static const String routeName = '/draft';
  final String title;

  const DraftScreen({super.key, required this.title});

  @override
  String get moduleName => title;

  @override
  String get backRouteName => MoneyScreen.routeName;

  @override
  AppScreenContent buildContent(BuildContext context) =>
      const DraftScreenContent();
}

class DraftScreenContent extends AppScreenContent {
  const DraftScreenContent({super.key});

  @override
  Widget build(BuildContext context) => const _DraftFeature();
}

class _DraftFeature extends StatefulWidget {
  const _DraftFeature();

  @override
  State<_DraftFeature> createState() => _DraftFeatureState();
}

class _DraftFeatureState extends State<_DraftFeature> {
  final _draftUseCase = getInstance<DraftUseCase>();
  final _promptController = TextEditingController();
  final _feedbackController = TextEditingController();
  final _imagePicker = ImagePicker();
  final _textRecognizer = TextRecognizer();

  DraftType _selectedType = DraftType.transaction;
  DraftModel? _draft;
  XFile? _selectedImage;
  String _extractedText = '';
  String _draftId = Uuid().v4();
  bool _isBusy = false;
  bool _isExtractingText = false;

  @override
  void dispose() {
    _promptController.dispose();
    _feedbackController.dispose();
    _textRecognizer.close();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Create a draft with AI',
          style: TextStyles.appMain(fontSize: AppFontSize.large),
        ),
        const SizedBox(height: 8),
        Text(
          'Describe the account, loan, or transaction you want to create.',
          style: TextStyles.appDetail(),
        ),
        const SizedBox(height: 16),
        _buildTypeDropdown(context),
        const SizedBox(height: 12),
        _buildTextField(
          label: 'Request',
          controller: _promptController,
          hintText: 'Type what you want to create',
          maxLines: 5,
          enabled: _draft == null,
        ),
        const SizedBox(height: 12),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            AppButton(
              label: _selectedImage == null ? 'Select image' : 'Change image',
              color: AppColor.mainDark,
              textColor: AppColor.white,
              width: 150,
              height: 42,
              visible: _draft == null,
              onTap: (_) => _pickImage(context),
            ),
            AppButton(
              label: _draft == null ? 'Generate draft' : 'Start new draft',
              color: AppColor.mainLight,
              textColor: AppColor.white,
              width: 150,
              height: 42,
              onTap:
                  (_) => _draft == null
                      ? _submitDraft(context)
                      : _resetDraft(keepInput: true),
            ),
          ],
        ),
        if (_selectedImage != null) ...[
          const SizedBox(height: 12),
          _buildImagePreview(),
        ],
        if (_isExtractingText) ...[
          const SizedBox(height: 12),
          const Center(child: CircularProgressIndicator()),
        ],
        if (_extractedText.isNotEmpty) ...[
          const SizedBox(height: 12),
          _buildSection(
            title: 'Extracted text',
            child: SelectableText(
              _extractedText,
              style: TextStyles.appDetail(),
            ),
          ),
        ],
        if (_draft != null) ...[
          const SizedBox(height: 16),
          _buildDraftPreview(context),
        ],
        if (_draft?.canRefine == true) ...[
          const SizedBox(height: 16),
          _buildTextField(
            label: 'Update request',
            controller: _feedbackController,
            hintText: 'Add more details if the draft is not correct yet',
            maxLines: 4,
          ),
          const SizedBox(height: 12),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              AppButton(
                label: 'Update draft',
                color: AppColor.mainDark,
                textColor: AppColor.white,
                width: 150,
                height: 42,
                onTap: (_) => _submitDraft(context, refinement: true),
              ),
              AppButton(
                label: 'Confirm',
                color: AppColor.green,
                textColor: AppColor.white,
                width: 150,
                height: 42,
                onTap: (_) => _confirmDraft(context),
              ),
            ],
          ),
        ],
        if (_draft != null && _draft!.canRefine == false) ...[
          const SizedBox(height: 12),
          Text(
            _draft!.modelError != null
                ? 'The draft could not be completed. Start a new draft with clearer instructions.'
                : 'This draft is no longer editable. Start a new draft to continue.',
            style: TextStyles.appDetail(fontColor: AppColor.red),
          ),
        ],
        if (_isBusy) ...[
          const SizedBox(height: 16),
          const Center(child: CircularProgressIndicator()),
        ],
      ],
    );
  }

  Widget _buildTypeDropdown(BuildContext context) {
    return SizedBox(
      height: 58,
      child: DropdownButtonFormField<DraftType>(
        initialValue: _selectedType,
        style: TextStyles.appDetail(),
        decoration: _inputDecoration('Draft type'),
        items:
            DraftType.values.map((type) {
              return DropdownMenuItem<DraftType>(
                value: type,
                child: Text(_typeLabel(context, type)),
              );
            }).toList(),
        onChanged:
            _draft == null
                ? (value) {
                  if (value == null) {
                    return;
                  }
                  setState(() {
                    _selectedType = value;
                  });
                }
                : null,
      ),
    );
  }

  Widget _buildTextField({
    required String label,
    required TextEditingController controller,
    required String hintText,
    int maxLines = 1,
    bool enabled = true,
  }) {
    return TextField(
      controller: controller,
      enabled: enabled && !_isBusy,
      maxLines: maxLines,
      style: TextStyles.appDetail(),
      decoration: _inputDecoration(label).copyWith(hintText: hintText),
    );
  }

  Widget _buildImagePreview() {
    return _buildSection(
      title: 'Selected image',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            path.basename(_selectedImage!.path),
            style: TextStyles.appDetail(fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 8),
          ClipRRect(
            borderRadius: BorderRadius.circular(10),
            child: Image.file(
              File(_selectedImage!.path),
              height: 180,
              width: double.infinity,
              fit: BoxFit.cover,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildDraftPreview(BuildContext context) {
    final draft = _draft!;

    return _buildSection(
      title: 'Draft preview',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            _typeLabel(context, draft.type),
            style: TextStyles.appDetail(fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 8),
          if (draft.modelResult.isEmpty)
            Text('No response data available.', style: TextStyles.appDetail()),
          ...draft.modelResult.entries.map(
            (entry) => Padding(
              padding: const EdgeInsets.only(bottom: 8),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    _formatKey(entry.key),
                    style: TextStyles.appDetail(fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 2),
                  SelectableText(
                    _formatValue(entry.value),
                    style: TextStyles.appDetail(),
                  ),
                ],
              ),
            ),
          ),
          if (draft.modelError != null) ...[
            const SizedBox(height: 8),
            Text(
              draft.modelError!,
              style: TextStyles.appDetail(fontColor: AppColor.red),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildSection({required String title, required Widget child}) {
    return BoxContainer(
      width: double.infinity,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: TextStyles.appDetail(fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 8),
          child,
        ],
      ),
    );
  }

  InputDecoration _inputDecoration(String label) {
    return InputDecoration(
      labelText: label,
      labelStyle: TextStyles.appDetail(
        fontColor: AppColor.disabledDark,
        fontSize: AppFontSize.small,
      ),
      enabledBorder: _border,
      focusedBorder: _border,
      border: _border,
    );
  }

  OutlineInputBorder get _border => OutlineInputBorder(
    borderRadius: BorderRadius.circular(10),
    borderSide: const BorderSide(color: AppColor.mainDark, width: 1),
  );

  Future<void> _pickImage(BuildContext context) async {
    if (_isBusy || _draft != null) {
      return;
    }

    final image = await _imagePicker.pickImage(source: ImageSource.gallery);
    if (image == null) {
      return;
    }

    setState(() {
      _selectedImage = image;
      _extractedText = '';
      _isExtractingText = true;
    });

    try {
      final recognizedText = await _textRecognizer.processImage(
        InputImage.fromFilePath(image.path),
      );
      if (!mounted) {
        return;
      }

      setState(() {
        _extractedText = recognizedText.text.trim();
      });

      if (_extractedText.isEmpty) {
        context.infoMessage('No text was found in the selected image.');
      }
    } catch (e) {
      if (!mounted) {
        return;
      }
      context.errorMessage(_errorMessage(e));
    } finally {
      if (mounted) {
        setState(() {
          _isExtractingText = false;
        });
      }
    }
  }

  Future<void> _submitDraft(
    BuildContext context, {
    bool refinement = false,
  }) async {
    if (_isBusy || _isExtractingText) {
      return;
    }

    final message = _buildMessage(refinement: refinement);
    if (message == null) {
      context.errorMessage(
        refinement
            ? 'Add more free text before updating the draft.'
            : 'Enter a request or choose an image first.',
      );
      return;
    }

    setState(() {
      _isBusy = true;
    });

    final result = await _draftUseCase.createDraft(
      type: _selectedType,
      draftId: _draft?.id ?? _draftId,
      message: message,
      language: context.language.name,
    );

    if (!mounted) {
      return;
    }

    setState(() {
      _isBusy = false;
    });

    if (result.isFailure) {
      context.errorMessage(_errorMessage(result.exception));
      return;
    }

    setState(() {
      _draft = result.data;
      _draftId = result.data.id;
      _feedbackController.clear();
    });
  }

  Future<void> _confirmDraft(BuildContext context) async {
    final draft = _draft;
    if (draft == null || _isBusy) {
      return;
    }

    setState(() {
      _isBusy = true;
    });

    final result = await _draftUseCase.confirmDraft(draft.id);

    if (!mounted) {
      return;
    }

    setState(() {
      _isBusy = false;
    });

    if (result.isFailure) {
      context.errorMessage(_errorMessage(result.exception));
      return;
    }

    context.read<AccountBloc>().add(FetchAccounts(user: context.user!));
    context.read<LoanBloc>().add(FetchLoans(userId: context.userId));
    context.read<TransactionBloc>().add(FetchTransactions(userId: context.userId));
    context.read<SummaryBloc>().add(CalculateSummary(userId: context.userId));
    context.successMessage('Draft confirmed');
    context.nextRoute(MoneyScreen.routeName);
  }

  void _resetDraft({bool keepInput = false}) {
    setState(() {
      _draft = null;
      _draftId = Uuid().v4();
      _feedbackController.clear();
      if (!keepInput) {
        _promptController.clear();
        _selectedImage = null;
        _extractedText = '';
      }
    });
  }

  String? _buildMessage({required bool refinement}) {
    if (refinement) {
      final feedback = _feedbackController.text.trim();
      return feedback.isEmpty ? null : feedback;
    }

    final parts = <String>[
      _promptController.text.trim(),
      if (_extractedText.isNotEmpty) 'Extracted text from image:\n$_extractedText',
    ]..removeWhere((part) => part.isEmpty);

    if (parts.isEmpty) {
      return null;
    }

    return parts.join('\n\n');
  }

  String _typeLabel(BuildContext context, DraftType type) {
    switch (type) {
      case DraftType.account:
        return context.locale.account;
      case DraftType.loan:
        return context.locale.loan;
      case DraftType.transaction:
        return context.locale.transaction;
    }
  }

  String _formatKey(String value) {
    return value
        .replaceAllMapped(
          RegExp(r'([A-Z])'),
          (match) => ' ${match.group(1)}',
        )
        .replaceAll('_', ' ')
        .trim()
        .split(' ')
        .where((part) => part.isNotEmpty)
        .map(
          (part) =>
              '${part.substring(0, 1).toUpperCase()}${part.substring(1)}',
        )
        .join(' ');
  }

  String _formatValue(dynamic value) {
    if (value == null) {
      return '-';
    }
    if (value is Map || value is List) {
      return const JsonEncoder.withIndent('  ').convert(value);
    }
    return value.toString();
  }

  String _errorMessage(Object? error) {
    final message = error?.toString() ?? context.locale.unknownError;
    return message.startsWith('Exception: ') ? message.substring(11) : message;
  }
}
