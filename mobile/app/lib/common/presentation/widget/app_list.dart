import 'package:bitapp/common/presentation/app_style.dart';
import 'package:bitapp/common/presentation/viewmodel/viewmodel.dart';
import 'package:bitapp/common/presentation/widget/container.dart';
import 'package:collection/collection.dart';
import 'package:flutter/material.dart';

class AppList extends StatelessWidget {
  final List<ListViewModel> items;
  final void Function(ListViewModel)? onItemTap;
  final String Function(ListViewModel)? groupByFunction;
  final IconData Function(ListViewModel)? getIcon;
  final bool showCategory;
  final bool showSubtitle;
  final bool wrapWithBoxContainer;
  final List<Widget> Function(ListViewModel)? hightlight;

  const AppList({
    super.key,
    required this.items,
    required this.onItemTap,
    this.groupByFunction,
    this.getIcon,
    this.showCategory = false,
    this.showSubtitle = false,
    required this.hightlight,
    this.wrapWithBoxContainer = true,
  });

  List<AppListView> get views {
    final sortedItems = items.sortedByCompare(
      (ListViewModel i) => i,
      (a, b) => a.compareTo(b),
    );

    if (groupByFunction != null) {
      return groupBy(sortedItems, groupByFunction!).entries
          .map(
            (entry) => AppListGroupView(
              title: entry.key,
              items: entry.value,
              onItemTap: onItemTap,
              getIcon: getIcon,
              showCategory: showCategory,
              showSubtitle: showSubtitle,
              hightlight: hightlight,
              wrapWithBoxContainer: wrapWithBoxContainer,
            ),
          )
          .toList();
    } else {
      return sortedItems
          .map(
            (item) => AppListObjectView(
              item: item,
              onItemTap: onItemTap,
              getIcon: getIcon,
              showCategory: showCategory,
              showSubtitle: showSubtitle,
              hightlight: hightlight,
              wrapWithBoxContainer: wrapWithBoxContainer,
            ),
          )
          .toList();
    }
  }

  @override
  Widget build(BuildContext context) => Column(children: views);
}

abstract class AppListView extends StatelessWidget {
  final void Function(ListViewModel)? onItemTap;
  final IconData Function(ListViewModel)? getIcon;
  final Color Function(ListViewModel)? getAmountColor;
  final bool hasAmount;
  final bool showCategory;
  final bool showSubtitle;
  final bool wrapWithBoxContainer;

  const AppListView({
    super.key,
    this.onItemTap,
    this.getIcon,
    this.getAmountColor,
    this.hasAmount = false,
    this.showCategory = false,
    this.showSubtitle = false,
    this.wrapWithBoxContainer = true,
  });
}

class AppListObjectView extends AppListView {
  final ListViewModel item;
  final bool isGrouped;
  final List<Widget> Function(ListViewModel)? hightlight;

  const AppListObjectView({
    super.key,
    super.onItemTap,
    super.getIcon,
    super.showCategory,
    super.wrapWithBoxContainer,
    required this.item,
    this.isGrouped = false,
    super.showSubtitle,
    this.hightlight,
  });

  @override
  Widget build(BuildContext context) {
    final bodyWidget =
        isGrouped || !wrapWithBoxContainer
            ? _buildBodyWidget(context)
            : BoxContainer(
              padding: EdgeInsets.only(left: 8, right: 8, top: 16, bottom: 16),
              child: _buildBodyWidget(context),
            );

    return Padding(
      padding: EdgeInsets.only(top: 8, bottom: 8),
      child: InkWell(
        onTap: () {
          if (onItemTap != null) {
            onItemTap!(item);
          }
        },
        child: bodyWidget,
      ),
    );
  }

  Widget _buildBodyWidget(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Row(
          children: [
            _icon(context),
            SizedBox(width: 16),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _title(context),
                SizedBox(width: 8),
                _subtitle(context),
              ],
            ),
          ],
        ),
        Row(
          mainAxisAlignment: MainAxisAlignment.end,
          children: hightlight != null ? hightlight!(item) : [],
        ),
      ],
    );
  }

  Widget _icon(BuildContext context) {
    return getIcon != null
        ? Icon(getIcon!(item), color: AppColor.mainDark)
        : Container();
  }

  Widget _title(BuildContext context) {
    return Text(item.title, style: TextStyles.appDetail());
  }

  Widget _subtitle(BuildContext context) {
    return showSubtitle
        ? Text(
          item.subtitle,
          style: TextStyles.appDetail(fontSize: AppFontSize.small),
        )
        : Container();
  }
}

class AppListGroupView extends AppListView {
  final String title;
  final List<ListViewModel> items;
  final List<Widget> Function(ListViewModel)? hightlight;

  const AppListGroupView({
    super.key,
    super.onItemTap,
    super.getIcon,
    super.showCategory,
    super.showSubtitle,
    super.wrapWithBoxContainer,
    required this.title,
    required this.items,
    this.hightlight,
  });

  List<AppListObjectView> get _itemViews =>
      items
          .map(
            (item) => AppListObjectView(
              item: item,
              onItemTap: onItemTap,
              getIcon: getIcon,
              isGrouped: true,
              showCategory: showCategory,
              showSubtitle: showSubtitle,
              hightlight: hightlight,
              wrapWithBoxContainer: wrapWithBoxContainer,
            ),
          )
          .toList();

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(title, style: TextStyles.appDetail(fontWeight: FontWeight.bold)),
        Padding(
          padding: EdgeInsets.only(top: 8, bottom: 8),
          child: BoxContainer(child: Column(children: _itemViews)),
        ),
      ],
    );
  }
}
