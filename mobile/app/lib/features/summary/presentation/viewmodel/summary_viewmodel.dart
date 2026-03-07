import 'package:bitapp/common/presentation/viewmodel/viewmodel.dart';
import 'package:bitapp/features/summary/data/summary.dart';

class SummaryViewModel implements ViewModel {
  final Summary _summary;

  SummaryViewModel(this._summary);

  double get totalAsset => _summary.totalAsset;
  double get totalIncome => _summary.totalIncome;
  double get totalDebt => _summary.totalDebt;
  double get totalExpense => _summary.totalExpense;
}
