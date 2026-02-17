import 'package:bitapp/common/common.dart';
import 'package:bitapp/money/data/model/summary.dart';

class SummaryViewModel implements ViewModel {
  final Summary _summary;

  SummaryViewModel(this._summary);

  double get totalAsset => _summary.totalAsset;
  double get totalIncome => _summary.totalIncome;
  double get totalDebt => _summary.totalDebt;
  double get totalExpense => _summary.totalExpense;
}
