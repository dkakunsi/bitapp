import 'package:app_common/app_common.dart';
import 'package:bitapp/l10n/localization_extension.dart';
import 'package:bitapp/money/bloc/transaction_analytics/transaction_analytics_bloc.dart';
import 'package:bitapp/money/extension/transaction_category_extension.dart';
import 'package:bitapp/money/presentation/screen/money_screen.dart';
import 'package:bitapp/money/presentation/viewmodel/transaction_analytics_viewmodel.dart';
import 'package:bitapp/money/presentation/viewmodel/transaction_viewmodel.dart';
import 'package:expandable/expandable.dart';
import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class TransactionAnalyticsScreen extends AppScreen {
  static final String routeName = '/transaction-analytics';
  final String title;

  const TransactionAnalyticsScreen({super.key, required this.title});

  @override
  String get moduleName => title;

  @override
  String get backRouteName => MoneyScreen.routeName;

  @override
  AppScreenContent buildContent(BuildContext context) =>
      const TransactionAnalyticsScreenContent();
}

class TransactionAnalyticsScreenContent extends AppScreenContent {
  const TransactionAnalyticsScreenContent({super.key});

  @override
  Future<void> reload(BuildContext context) async {}

  @override
  Widget build(BuildContext context) {
    return BlocConsumer<TransactionAnalyticsBloc, TransactionAnalyticsState>(
      listener: (context, state) {},
      builder: (context, state) {
        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                IconButton(
                  onPressed: () {
                    final currentPeriod = state.object.anlaysisDate;
                    context.read<TransactionAnalyticsBloc>().add(
                      AnalyzeTransactions(
                        userId: context.userId,
                        date: DateTime(
                          currentPeriod.year,
                          currentPeriod.month - 1,
                          1,
                        ),
                      ),
                    );
                  },
                  icon: Icon(
                    Icons.arrow_left_sharp,
                    size: 40,
                    color: AppColor.mainDark,
                  ),
                ),
                Text(
                  state.object.getPeriod(context),
                  style: TextStyles.appMain(
                    fontSize: AppFontSize.large,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                IconButton(
                  onPressed: () {
                    final currentPeriod = state.object.anlaysisDate;
                    context.read<TransactionAnalyticsBloc>().add(
                      AnalyzeTransactions(
                        userId: context.userId,
                        date: DateTime(
                          currentPeriod.year,
                          currentPeriod.month + 1,
                          1,
                        ),
                      ),
                    );
                  },
                  icon: Icon(
                    Icons.arrow_right_sharp,
                    size: 40,
                    color: AppColor.mainDark,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            BoxContainer(
              borderColor: AppColor.transparent,
              color: AppColor.mainDark,
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Column(
                    children: [
                      Text(
                        context.locale.income,
                        style: TextStyles.appDetail(
                          fontColor: AppColor.green,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 8),
                      CurrencyAmount(
                        amount: state.object.income,
                        currency: context.locale.idr,
                        color: AppColor.green,
                        fontWeight: FontWeight.bold,
                      ),
                    ],
                  ),
                  Column(
                    children: [
                      Text(
                        context.locale.expense,
                        style: TextStyles.appDetail(
                          fontColor: AppColor.red,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 8),
                      CurrencyAmount(
                        amount: state.object.expense,
                        currency: context.locale.idr,
                        color: AppColor.red,
                        fontWeight: FontWeight.bold,
                      ),
                    ],
                  ),
                  Column(
                    children: [
                      Text(
                        context.locale.accumulation,
                        style: TextStyles.appDetail(
                          fontColor: AppColor.white,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 8),
                      CurrencyAmount(
                        amount: state.object.accumulation,
                        currency: context.locale.idr,
                        color: AppColor.white,
                        fontWeight: FontWeight.bold,
                      ),
                    ],
                  ),
                ],
              ),
            ),
            const SizedBox(height: 8),
            Text('Persentase pengeluaran kamu', style: TextStyles.appDetail()),
            SizedBox(
              height: 300,
              child: PieChart(
                PieChartData(
                  pieTouchData: PieTouchData(
                    touchCallback: (event, response) {},
                  ),
                  borderData: FlBorderData(show: false),
                  sectionsSpace: 0,
                  centerSpaceRadius: 40,
                  sections:
                      state.object.expenseGroups
                          .map(
                            (eg) => PieChartSectionData(
                              color: eg.category.color,
                              value: eg.total,
                              title:
                                  '${context.availableCategories[eg.category]!}\n${eg.percentage.toStringAsFixed(0)}%',
                              radius: 90,
                              titleStyle: TextStyles.appDetail(),
                            ),
                          )
                          .toList(),
                ),
              ),
            ),
            const SizedBox(height: 8),
            Text('Detail pengeluaran kamu', style: TextStyles.appDetail()),
            const SizedBox(height: 8),
            ExpenseGroupList(items: state.object.expenseGroups),
          ],
        );
      },
    );
  }
}

class ExpenseGroupList extends StatelessWidget {
  final List<ExpenseGroup> items;

  const ExpenseGroupList({super.key, required this.items});

  @override
  Widget build(BuildContext context) {
    return Column(
      children:
          items.map((e) {
            return Column(
              children: [
                BoxContainer(
                  padding: const EdgeInsets.all(8),
                  color: AppColor.white,
                  child: ExpandablePanel(
                    theme: const ExpandableThemeData(
                      headerAlignment: ExpandablePanelHeaderAlignment.center,
                      iconColor: AppColor.mainDark,
                      iconSize: 24,
                      tapBodyToCollapse: true,
                    ),
                    header: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Row(
                          children: [
                            Icon(
                              e.category.icon,
                              size: 24,
                              color: AppColor.mainDark,
                            ),
                            const SizedBox(width: 16),
                            Text(
                              context.availableCategories[e.category]!,
                              style: TextStyles.appDetail(
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ],
                        ),
                        CurrencyAmount(
                          amount: e.total,
                          currency: context.locale.idr,
                          fontWeight: FontWeight.bold,
                        ),
                      ],
                    ),
                    collapsed: Container(),
                    expanded: AppList(
                      wrapWithBoxContainer: false,
                      items: e.transactions,
                      onItemTap: (item) {},
                      hightlight: (item) {
                        item as TransactionViewModel;
                        return [
                          CurrencyAmount(
                            amount: item.amount,
                            currency: context.locale.idr,
                          ),
                        ];
                      },
                    ),
                  ),
                ),
              ],
            );
          }).toList(),
    );
    // return AppList(items: items, onItemTap: onItemTap, hightlight: hightlight)
  }
}
