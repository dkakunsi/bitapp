import 'package:app_common/app_common.dart';
import 'package:flutter/material.dart';

/// An abstract base class for creating forms in the application.
///
/// This class extends [StatefulWidget] and is designed to work with a
/// generic type [VM] that extends [ViewModel]. It provides a structure
/// for forms that may or may not be in editing mode, depending on
/// whether a [viewModel] is provided.
///
/// - [VM]: The type of the [ViewModel] associated with the form.
///
/// ## Properties:
///
/// - `viewModel`: An optional instance of [VM] that represents the
///   data model for the form. If this is `null`, the form is considered
///   to be in creation mode. If not `null`, the form is in editing mode.
///
/// - `isEditing`: A computed property that returns `true` if a
///   [viewModel] is provided, indicating that the form is in editing mode.
///
/// - `formTitle`: An abstract getter that must be implemented by
///   subclasses to provide the title of the form, represented as a
///   [String].
///
/// ## Usage:
///
/// This class is intended to be subclassed to create specific forms
/// for different types of [ViewModel]. Subclasses must implement the
/// `formTitle` getter to define the title of the form.
abstract class AppForm<VM extends ViewModel> extends StatefulWidget {
  final VM? viewModel;

  const AppForm({super.key, this.viewModel});

  String get formTitle;

  bool get isEditing => viewModel != null;

  bool showDeleteButton() => isEditing;
  bool showSaveButton() => true;
}

/// An abstract class that represents the state of a form widget in the application.
///
/// This class is designed to be extended by specific form states and provides
/// common functionality and styling for form widgets.
///
/// Type Parameters:
/// - `AF`: The type of the form widget that extends [AppForm].
/// - `VM`: The type of the [ViewModel] associated with the form.
///
/// Properties:
/// - `borderSettings`: Defines the border styling for input fields in the form,
///   including border radius, color, and width.
/// - `textStyle`: Specifies the text styling for input fields, including color,
///   font size, and font family.
///
/// Getters:
/// - `viewModel`: Retrieves the [ViewModel] associated with the form widget.
///
/// Methods:
/// - `onSave()`: Abstract method that must be implemented to define the save
///   behavior of the form.
/// - `onDelete()`: A method to handle delete functionality. By default, it
///   will do nothing.
abstract class AppFormState<AF extends AppForm<VM>, VM extends ViewModel>
    extends State<AF> {
  final borderSettings = OutlineInputBorder(
    borderRadius: BorderRadius.circular(10),
    borderSide: const BorderSide(color: AppColor.mainDark, width: 1),
  );

  final labelStyle = TextStyles.appDetail(
    fontColor: AppColor.disabledDark,
    fontSize: AppFontSize.small,
  );

  VM? get viewModel => widget.viewModel;

  bool get isEditing => widget.isEditing;

  String? onSave(BuildContext context);
  String? onDelete(BuildContext context) => null;
}
