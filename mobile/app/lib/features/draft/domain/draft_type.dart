enum DraftType {
  account('ACCOUNT'),
  loan('LOAN'),
  transaction('TRANSACTION');

  final String apiValue;

  const DraftType(this.apiValue);

  static DraftType fromApi(String value) {
    switch (value.toUpperCase()) {
      case 'ACCOUNT':
        return DraftType.account;
      case 'LOAN':
        return DraftType.loan;
      case 'TRANSACTION':
      default:
        return DraftType.transaction;
    }
  }
}
