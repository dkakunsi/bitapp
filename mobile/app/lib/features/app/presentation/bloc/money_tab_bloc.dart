import 'package:bitapp/common/presentation/bloc/app_tab/app_tab_bloc.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

part 'money_tab_event.dart';
part 'money_tab_state.dart';

class MoneyTabBloc extends AppTabBloc<MoneyTabEvent, MoneyTabState> {
  MoneyTabBloc() : super(InitialMoneyTabState()) {
    on<SelectMoneyTab>(_selectSection);
  }

  Future<void> _selectSection(
    SelectMoneyTab event,
    Emitter<MoneyTabState> emit,
  ) async {
    emit(MoneyTabSelected(label: event.tabName));
  }
}
