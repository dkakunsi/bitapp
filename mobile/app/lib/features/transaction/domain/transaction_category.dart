import 'package:bitapp/features/transaction/domain/transaction_type.dart';
import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';

enum TransactionCategory {
  bonus(
    value: 'BONUS',
    isCredit: true,
    icon: FontAwesomeIcons.moneyBillTrendUp,
    color: Colors.amber,
  ),
  bills(value: 'BILLS', isDebit: true, icon: FontAwesomeIcons.moneyBill1, color: Colors.blue),
  charity(
    value: 'CHARITY',
    isDebit: true,
    icon: Icons.volunteer_activism,
    color: Colors.blueGrey,
  ),
  education(value: 'EDUCATION', isDebit: true, icon: Icons.school, color: Colors.brown),
  entertainment(value: 'ENTERTAINMENT', isDebit: true, icon: Icons.movie, color: Colors.cyan),
  food(value: 'FOOD', isDebit: true, icon: Icons.restaurant, color: Colors.deepOrange),
  gift(value: 'GIFT', isDebit: true, icon: FontAwesomeIcons.gift, color: Colors.deepPurple),
  health(value: 'HEALTH', isDebit: true, icon: Icons.local_hospital, color: Colors.green),
  hobbies(value: 'HOBBIES', isDebit: true, icon: Icons.sports_esports, color: Colors.grey),
  interest(value: 'INTEREST', isCredit: true, icon: Icons.receipt, color: Colors.indigo),
  investment(value: 'INVESTMENT', isDebit: true, icon: Icons.attach_money, color: Colors.lightBlue),
  loan(value: 'LOAN', isDebit: true, icon: Icons.real_estate_agent, color: Colors.lightGreen),
  loanDisbursement(
    value: 'LOAN_DISBURSEMENT',
    isCredit: true,
    icon: Icons.real_estate_agent,
    color: Colors.lightGreenAccent,
  ),
  loanPayment(
    value: 'LOAN_PAYMENT',
    isCredit: true,
    isDebit: true,
    icon: Icons.payment,
    color: Colors.lime,
  ),
  other(
    value: 'OTHER',
    isCredit: true,
    isDebit: true,
    isTransfer: true,
    icon: Icons.question_mark,
    color: Colors.orange,
  ),
  rent(value: 'RENT', isDebit: true, icon: Icons.apartment, color: Colors.pink),
  salary(
    value: 'SALARY',
    isCredit: true,
    icon: FontAwesomeIcons.sackDollar,
    color: Colors.purple,
  ),
  savings(
    value: 'SAVINGS',
    isCredit: true,
    isDebit: true,
    isTransfer: true,
    icon: Icons.savings,
    color: Colors.red,
  ),
  shopping(value: 'SHOPPING', isDebit: true, icon: Icons.shopping_cart, color: Colors.teal),
  subscription(value: 'SUBSCRIPTION', isDebit: true, icon: Icons.subscriptions, color: Colors.white),
  tax(value: 'TAX', isDebit: true, icon: Icons.receipt, color: Colors.yellow),
  transport(
    value: 'TRANSPORTATION',
    isDebit: true,
    icon: Icons.directions_car,
    color: Colors.amberAccent,
  ),
  travel(value: 'TRAVEL', isDebit: true, icon: Icons.flight, color: Colors.blueAccent),
  utilities(
    value: 'UTILITIES',
    isDebit: true,
    icon: Icons.electrical_services,
    color: Colors.cyanAccent,
  );

  final String value;
  final bool isCredit;
  final bool isDebit;
  final bool isTransfer;
  final IconData icon;
  final Color color;

  const TransactionCategory({
    this.isCredit = false,
    this.isDebit = false,
    this.isTransfer = false,
    required this.icon,
    required this.color,
    required this.value,
  });

  bool canShow(TransactionType type) {
    return (isCredit && type == TransactionType.credit) ||
        (isDebit && type == TransactionType.debit) ||
        (isTransfer && type == TransactionType.transfer);
  }

  static TransactionCategory valueOf(String s) {
    return TransactionCategory.values.firstWhere(
      (e) => e.name == s,
      orElse: () => TransactionCategory.other,
    );
  }
}
