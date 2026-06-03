import 'package:bitapp/features/transaction/domain/transaction_category.dart';
import 'package:bitapp/l10n/localization_extension.dart';
import 'package:flutter/material.dart';

extension TransactionCategoryExtension on BuildContext {
  Map<TransactionCategory, String> get availableCategories => {
    TransactionCategory.bonus: locale.bonus,
    TransactionCategory.bills: locale.bills,
    TransactionCategory.charity: locale.charity,
    TransactionCategory.education: locale.education,
    TransactionCategory.entertainment: locale.entertainment,
    TransactionCategory.food: locale.food,
    TransactionCategory.gift: locale.gift,
    TransactionCategory.health: locale.health,
    TransactionCategory.interest: locale.interest,
    TransactionCategory.investment: locale.investment,
    TransactionCategory.loan: locale.loan,
    TransactionCategory.loanPayment: locale.loanPayment,
    TransactionCategory.other: locale.other,
    TransactionCategory.rent: locale.rent,
    TransactionCategory.salary: locale.salary,
    TransactionCategory.savings: locale.savings,
    TransactionCategory.shopping: locale.shopping,
    TransactionCategory.subscription: locale.subscription,
    TransactionCategory.tax: locale.tax,
    TransactionCategory.transport: locale.transport,
    TransactionCategory.travel: locale.travel,
    TransactionCategory.utilities: locale.utilities,
  };
}
