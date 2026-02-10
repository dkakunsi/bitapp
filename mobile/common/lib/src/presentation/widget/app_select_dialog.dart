import 'package:app_common/app_common.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class AppSelectDialog<
  BLOC extends Bloc<EVENT, STATE>,
  EVENT,
  STATE,
  LOADED_STATE extends ListState
>
    extends StatefulWidget {
  final void Function(BuildContext) loadData;

  final bool Function(ListViewModel)? filter;

  const AppSelectDialog({super.key, required this.loadData, this.filter});

  @override
  State<StatefulWidget> createState() =>
      AppSelectDialogState<BLOC, EVENT, STATE, LOADED_STATE>();
}

class AppSelectDialogState<
  BLOC extends Bloc<EVENT, STATE>,
  EVENT,
  STATE,
  LOADED_STATE extends ListState
>
    extends State<AppSelectDialog> {
  @override
  Widget build(BuildContext context) {
    return BlocBuilder<BLOC, STATE>(
      builder: (context, state) {
        if (state is LOADED_STATE) {
          final items =
              (state as LOADED_STATE).items
                  .where(widget.filter ?? _defaultFilter)
                  .toList();
          return buildView(items, (item) {
            Navigator.of(context, rootNavigator: true).pop(item);
          });
        } else {
          widget.loadData(context);
          return LoadingIndicator();
        }
      },
    );
  }

  Padding buildView(
    List<ListViewModel> items,
    Function(ListViewModel) onSelect,
  ) {
    return Padding(
      padding: const EdgeInsets.all(8.0),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        mainAxisAlignment: MainAxisAlignment.start,
        children: [
          SingleChildScrollView(
            child: Column(
              children:
                  items
                      .map(
                        (i) => ListTile(
                          title: Text(i.title),
                          onTap: () {
                            onSelect(i);
                          },
                        ),
                      )
                      .toList(),
            ),
          ),
        ],
      ),
    );
  }

  bool _defaultFilter(ListViewModel l) => true;
}
