const _cash = 'CASH';
const _ewallet = 'EWALLET';
const _bank = 'BANK';

enum AccountType {
  cash(value: _cash),
  bank(value: _bank),
  ewallet(value: _ewallet);

  final String value;

  const AccountType({required this.value});

  static AccountType valueOf(String s) {
    switch (s) {
      case _bank:
        return AccountType.bank;
      case _ewallet:
        return AccountType.ewallet;
      case _cash:
        return AccountType.cash;
      default:
        throw Exception('AccountType not found');
    }
  }

  static List<String> types() {
    return [cash.value, bank.value, ewallet.value];
  }
}
