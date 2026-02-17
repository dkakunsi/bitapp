import 'package:flutter_bloc/flutter_bloc.dart';

part 'app_tab_event.dart';
part 'app_tab_state.dart';

abstract class AppTabBloc<EVENT extends AppTabEvent, STATE extends AppTabState>
    extends Bloc<EVENT, STATE> {
  AppTabBloc(super.initialState);

  @override
  void onTransition(Transition<EVENT, STATE> transition) {
    super.onTransition(transition);
  }
}
