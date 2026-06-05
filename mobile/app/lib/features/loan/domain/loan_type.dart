const _borrow = 'BORROW';
const _lend = 'LEND';

enum LoanType {
  debt(_borrow),
  receivable(_lend);

  final String value;

  const LoanType(this.value);

  static LoanType valueOf(String s) {
    switch (s) {
      case _borrow:
        return LoanType.debt;
      case _lend:
        return LoanType.receivable;
      default:
        throw Exception('LoanType not found');
    }
  }

  static List<LoanType> types() => [debt, receivable];
}
