import 'dart:convert';

class Summary {
  final double totalAsset;
  final double totalIncome;
  final double totalDebt;
  final double totalExpense;

  Summary({
    this.totalAsset = 0,
    this.totalIncome = 0,
    this.totalDebt = 0,
    this.totalExpense = 0,
  });

  static Summary fromResponsePayload(String s) {
    final data = jsonDecode(s);
    return Summary(
      totalAsset: (data['totalAsset'] as num).toDouble(),
      totalIncome: (data['totalIncome'] as num).toDouble(),
      totalDebt: (data['totalDebt'] as num).toDouble(),
      totalExpense: (data['totalExpense'] as num).toDouble(),
    );
  }

  static Summary empty() {
    return Summary();
  }
}
