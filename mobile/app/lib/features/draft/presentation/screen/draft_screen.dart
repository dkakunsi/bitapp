import 'dart:convert';

import 'package:bitapp/common/presentation/app_style.dart';
import 'package:bitapp/common/presentation/widget/app_button.dart';
import 'package:bitapp/common/presentation/widget/container.dart';
import 'package:bitapp/features/account/presentation/bloc/account_bloc.dart';
import 'package:bitapp/features/app/extension/language_extension.dart';
import 'package:bitapp/features/app/extension/navigation_extension.dart';
import 'package:bitapp/features/app/presentation/screen/app_screen.dart';
import 'package:bitapp/features/app/presentation/screen/money_screen.dart';
import 'package:bitapp/features/draft/presentation/bloc/draft_bloc.dart';
import 'package:bitapp/features/draft/domain/chat_type.dart';
import 'package:bitapp/features/draft/domain/draft.dart';
import 'package:bitapp/features/authentication/extension/session_extension.dart';
import 'package:bitapp/features/loan/presentation/bloc/loan_bloc.dart';
import 'package:bitapp/features/summary/presentation/bloc/summary_bloc.dart';
import 'package:bitapp/features/transaction/presentation/bloc/transaction_bloc.dart';
import 'package:bitapp/l10n/localization_extension.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:google_mlkit_text_recognition/google_mlkit_text_recognition.dart';
import 'package:image_picker/image_picker.dart';
import 'package:uuid/uuid.dart';

class DraftScreen extends AppScreen {
  static const String routeName = '/draft';

  final String title;

  const DraftScreen({super.key, super.listener, required this.title});

  @override
  String get moduleName => title;

  @override
  String get backRouteName => MoneyScreen.routeName;

  @override
  AppScreenContent buildContent(BuildContext context) => DraftScreenContent();
}

class DraftScreenContent extends AppScreenContent {
  final _DraftComposerController _composerController = _DraftComposerController();

  DraftScreenContent({super.key});

  @override
  Widget build(BuildContext context) =>
      _DraftFeature(composerController: _composerController);

  @override
  Widget? buildNavigationBar(BuildContext context) {
    return _DraftComposer(composerController: _composerController);
  }
}

class _DraftFeature extends StatefulWidget {
  final _DraftComposerController composerController;

  const _DraftFeature({required this.composerController});

  @override
  State<_DraftFeature> createState() => _DraftFeatureState();
}

enum _ChatRole { user, system }

class _ChatMessage {
  final _ChatRole role;
  final String text;

  const _ChatMessage({required this.role, required this.text});
}

class _DraftComposerController extends ChangeNotifier {
  final TextEditingController promptController = TextEditingController();

  bool isBusy = false;
  bool isExtractingText = false;
  bool canConfirm = false;
  bool hasDraft = false;

  VoidCallback? onSend;
  VoidCallback? onPickImage;

  void update({
    required bool busy,
    required bool extractingText,
    required bool confirmable,
    required bool draft,
  }) {
    isBusy = busy;
    isExtractingText = extractingText;
    canConfirm = confirmable;
    hasDraft = draft;
    notifyListeners();
  }

  @override
  void dispose() {
    promptController.dispose();
    super.dispose();
  }
}

class _DraftFeatureState extends State<_DraftFeature> {
  final _imagePicker = ImagePicker();
  final _textRecognizer = TextRecognizer();
  final _uuid = Uuid();

  ChatType _selectedType = ChatType.transaction;
  Draft? _draft;
  final List<_ChatMessage> _messages = [];
  late String _draftId;
  bool _isBusy = false;
  bool _isExtractingText = false;

  @override
  void initState() {
    super.initState();
    _draftId = _uuid.v4();
    widget.composerController.onSend = _onComposerSend;
    widget.composerController.onPickImage = _onComposerPickImage;
    _syncComposerController();
  }

  @override
  void dispose() {
    widget.composerController.onSend = null;
    widget.composerController.onPickImage = null;
    _textRecognizer.close();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return BlocConsumer<DraftBloc, DraftState>(
      listener: _onDraftStateChanged,
      builder: (context, state) {
        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              context.locale.aiDraftTitle,
              style: TextStyles.appMain(fontSize: AppFontSize.large),
            ),
            const SizedBox(height: 8),
            Text(
              context.locale.aiDraftDescription,
              style: TextStyles.appDetail(),
            ),
            const SizedBox(height: 16),
            _buildTypeDropdown(context),
            const SizedBox(height: 12),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: [
                AppButton(
                  label:
                      _draft == null
                          ? context.locale.aiDraftStartNew
                          : context.locale.aiDraftStartNew,
                  color: AppColor.mainLight,
                  textColor: AppColor.white,
                  width: 150,
                  height: 42,
                  onTap: (_) => _resetDraft(),
                ),
                AppButton(
                  label: context.locale.aiDraftConfirm,
                  color: AppColor.green,
                  textColor: AppColor.white,
                  width: 150,
                  height: 42,
                  visible: _draft?.canConfirm == true,
                  onTap: (_) => _confirmDraft(context),
                ),
              ],
            ),
            const SizedBox(height: 12),
            ..._buildMessages(context),
            if (_isExtractingText) ...[
              const SizedBox(height: 12),
              const Center(child: CircularProgressIndicator()),
            ],
            if (_draft != null && _draft!.canConfirm == false) ...[
              const SizedBox(height: 12),
              Text(
                _draft!.modelError != null
                    ? context.locale.aiDraftFailed
                    : context.locale.aiDraftLocked,
                style: TextStyles.appDetail(fontColor: AppColor.red),
              ),
            ],
            if (_isBusy) ...[
              const SizedBox(height: 16),
              const Center(child: CircularProgressIndicator()),
            ],
            const SizedBox(height: 80),
          ],
        );
      },
    );
  }

  void _syncComposerController() {
    widget.composerController.update(
      busy: _isBusy,
      extractingText: _isExtractingText,
      confirmable: _draft?.canConfirm == true,
      draft: _draft != null,
    );
  }

  void _onComposerSend() {
    _submitDraft(context, refinement: _draft?.canConfirm == true);
  }

  void _onComposerPickImage() {
    _pickImage(context);
  }

  void _onDraftStateChanged(BuildContext context, DraftState state) {
    if (state is DraftProcessing) {
      setState(() {
        _isBusy = true;
      });
      _syncComposerController();
      return;
    }

    if (state is DraftFailed) {
      setState(() {
        _isBusy = false;
      });
      _syncComposerController();
      context.errorMessage(_errorMessage(state.exception));
      return;
    }

    if (state is DraftCreated) {
      setState(() {
        _isBusy = false;
        _draft = state.draft;
        _draftId = state.draft.id;
        _selectedType = state.draft.type;
        _messages.add(_ChatMessage(role: _ChatRole.system, text: _draftToText(state.draft, context)));
      });
      _syncComposerController();
      return;
    }

    if (state is DraftConfirmed) {
      setState(() {
        _isBusy = false;
      });
      _syncComposerController();
      context.read<AccountBloc>().add(FetchAccounts(user: context.user!));
      context.read<LoanBloc>().add(FetchLoans(userId: context.userId));
      context.read<TransactionBloc>().add(
        FetchTransactions(userId: context.userId),
      );
      context.read<SummaryBloc>().add(CalculateSummary(userId: context.userId));
      context.successMessage(context.locale.aiDraftConfirmed);
      context.nextRoute(MoneyScreen.routeName);
    }
  }

  Widget _buildTypeDropdown(BuildContext context) {
    return SizedBox(
      height: 58,
      child: DropdownButtonFormField<ChatType>(
        initialValue: _selectedType,
        style: TextStyles.appDetail(),
        decoration: _inputDecoration(context.locale.aiDraftType),
        items:
            ChatType.values.map((type) {
              return DropdownMenuItem<ChatType>(
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

  List<Widget> _buildMessages(BuildContext context) {
    if (_messages.isEmpty) {
      return [
        BoxContainer(
          width: double.infinity,
          child: Text(
            context.locale.aiDraftRequestHint,
            style: TextStyles.appDetail(fontColor: AppColor.disabledDark),
          ),
        ),
      ];
    }

    return _messages
        .map(
          (message) => Align(
            alignment:
                message.role == _ChatRole.user
                    ? Alignment.centerRight
                    : Alignment.centerLeft,
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 320),
              child: Container(
                margin: const EdgeInsets.only(bottom: 8),
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                decoration: BoxDecoration(
                  color:
                      message.role == _ChatRole.user
                          ? AppColor.mainDark
                          : AppColor.white,
                  borderRadius: BorderRadius.circular(12),
                  border:
                      message.role == _ChatRole.user
                          ? null
                          : Border.all(color: AppColor.mainDark, width: 1),
                ),
                child: SelectableText(
                  message.text,
                  style: TextStyles.appDetail(
                    fontColor:
                        message.role == _ChatRole.user
                            ? AppColor.white
                            : AppColor.mainDark,
                  ),
                ),
              ),
            ),
          ),
        )
        .toList();
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
      _isExtractingText = true;
    });
    _syncComposerController();

    try {
      final recognizedText = await _textRecognizer.processImage(
        InputImage.fromFilePath(image.path),
      );
      if (!mounted) {
        return;
      }

      setState(() {
        final extractedText = recognizedText.text.trim();
        if (extractedText.isNotEmpty) {
          final currentText =
              widget.composerController.promptController.text.trim();
          widget.composerController.promptController.text =
              currentText.isEmpty
                  ? extractedText
                  : '$currentText\n\n$extractedText';
          widget.composerController.promptController.selection =
              TextSelection.fromPosition(
                TextPosition(
                  offset:
                      widget.composerController.promptController.text.length,
                ),
              );
        }
      });

      if (recognizedText.text.trim().isEmpty) {
        context.infoMessage(context.locale.aiDraftNoTextFound);
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
        _syncComposerController();
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
            ? context.locale.aiDraftUpdateEmpty
            : context.locale.aiDraftRequestEmpty,
      );
      return;
    }

    setState(() {
      _isBusy = true;
      _messages.add(_ChatMessage(role: _ChatRole.user, text: message));
    });
    _syncComposerController();

    context.read<DraftBloc>().add(
      CreateDraft(
        draftId: _draft?.id ?? _draftId,
        type: _selectedType,
        message: message,
        language: context.language.name,
      ),
    );

    widget.composerController.promptController.clear();
  }

  Future<void> _confirmDraft(BuildContext context) async {
    final draft = _draft;
    if (draft == null || _isBusy) {
      return;
    }

    setState(() {
      _isBusy = true;
    });
    _syncComposerController();

    context.read<DraftBloc>().add(ConfirmDraft(draftId: draft.id));
  }

  void _resetDraft({bool keepInput = false}) {
    setState(() {
      _draft = null;
      _draftId = _uuid.v4();
      _messages.clear();
      if (!keepInput) {
        widget.composerController.promptController.clear();
      }
    });
    _syncComposerController();
  }

  String? _buildMessage({required bool refinement}) {
    final message = widget.composerController.promptController.text.trim();
    return message.isEmpty ? null : message;
  }

  String _typeLabel(BuildContext context, ChatType type) {
    switch (type) {
      case ChatType.account:
        return context.locale.account;
      case ChatType.loan:
        return context.locale.loan;
      case ChatType.transaction:
        return context.locale.transaction;
    }
  }

  String _formatKey(String value) {
    return value
        .replaceAllMapped(RegExp(r'([A-Z])'), (match) => ' ${match.group(1)}')
        .replaceAll('_', ' ')
        .trim()
        .split(' ')
        .where((part) => part.isNotEmpty)
        .map(
          (part) => '${part.substring(0, 1).toUpperCase()}${part.substring(1)}',
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

  String _draftToText(Draft draft, BuildContext context) {
    if (draft.modelError?.isNotEmpty ?? false) {
      return draft.modelError!;
    }

    if (draft.modelResult?.isEmpty ?? true) {
      return context.locale.aiDraftNoResponseData;
    }

    return draft.modelResult!.entries
        .map((entry) => '${_formatKey(entry.key)}: ${_formatValue(entry.value)}')
        .join('\n');
  }

  String _errorMessage(Object? error) {
    final message = error?.toString() ?? context.locale.unknownError;
    return message.startsWith('Exception: ') ? message.substring(11) : message;
  }
}

class _DraftComposer extends StatelessWidget {
  final _DraftComposerController composerController;

  const _DraftComposer({required this.composerController});

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      top: false,
      child: AnimatedBuilder(
        animation: composerController,
        builder: (context, child) {
          final isDisabled =
              composerController.isBusy || composerController.isExtractingText;
          return Container(
            color: AppColor.white,
            padding: const EdgeInsets.fromLTRB(8, 8, 8, 12),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                IconButton(
                  onPressed:
                      isDisabled ? null : () => composerController.onPickImage?.call(),
                  icon: const Icon(Icons.photo_library_outlined),
                  color: AppColor.mainDark,
                ),
                Expanded(
                  child: TextField(
                    controller: composerController.promptController,
                    enabled: !isDisabled,
                    minLines: 1,
                    maxLines: 5,
                    style: TextStyles.appDetail(),
                    decoration: InputDecoration(
                      hintText: context.locale.aiDraftRequestHint,
                      hintStyle: TextStyles.appDetail(
                        fontColor: AppColor.disabledDark,
                      ),
                      contentPadding: const EdgeInsets.symmetric(
                        horizontal: 12,
                        vertical: 10,
                      ),
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(12),
                        borderSide: const BorderSide(
                          color: AppColor.mainDark,
                          width: 1,
                        ),
                      ),
                      focusedBorder: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(12),
                        borderSide: const BorderSide(
                          color: AppColor.mainDark,
                          width: 1,
                        ),
                      ),
                      enabledBorder: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(12),
                        borderSide: const BorderSide(
                          color: AppColor.mainDark,
                          width: 1,
                        ),
                      ),
                    ),
                  ),
                ),
                IconButton(
                  onPressed: isDisabled ? null : () => composerController.onSend?.call(),
                  icon: const Icon(Icons.send),
                  color: AppColor.mainDark,
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}
