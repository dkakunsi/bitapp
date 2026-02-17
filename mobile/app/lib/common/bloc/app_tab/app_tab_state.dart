part of 'app_tab_bloc.dart';

abstract class AppTabState {}

abstract class AppTabSelected extends AppTabState {
  String get label;
}
