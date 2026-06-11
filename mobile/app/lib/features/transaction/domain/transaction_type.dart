const _debit = 'DEBIT';
const _credit = 'CREDIT';
const _transfer = 'TRANSFER';

enum TransactionType {
  debit(_debit),
  credit(_credit),
  transfer(_transfer);

  final String value;

  const TransactionType(this.value);

  static TransactionType valueOf(String s) {
    switch (s) {
      case _debit:
        return TransactionType.debit;
      case _credit:
        return TransactionType.credit;
      case _transfer:
        return TransactionType.transfer;
      default:
        throw Exception('TransactionType not found');
    }
  }

  static List<TransactionType> types() {
    return [debit, credit, transfer];
  }
}
