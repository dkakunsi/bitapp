part of 'app_tab_bloc.dart';

abstract class AppTabEvent {}

abstract class SelectTabEvent {
  String get tabName;
}
