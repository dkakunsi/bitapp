import 'package:app_common/app_common.dart';

abstract class ListState {
  List<ListViewModel> get items;
}

abstract class ObjectState {
  ViewModel get object;
}
