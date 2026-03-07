import 'package:bitapp/common/presentation/viewmodel/viewmodel.dart';

abstract class ListState {
  List<ListViewModel> get items;
}

abstract class ObjectState {
  ViewModel get object;
}
