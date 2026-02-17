import 'package:bitapp/common/common.dart';

abstract class ListState {
  List<ListViewModel> get items;
}

abstract class ObjectState {
  ViewModel get object;
}
