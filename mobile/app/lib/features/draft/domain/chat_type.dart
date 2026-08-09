enum ChatType {
  account('ACCOUNT'),
  loan('LOAN'),
  transaction('TRANSACTION');

  final String apiValue;

  const ChatType(this.apiValue);

  static ChatType fromApi(String value) {
    switch (value.toUpperCase()) {
      case 'ACCOUNT':
        return ChatType.account;
      case 'LOAN':
        return ChatType.loan;
      case 'TRANSACTION':
      default:
        return ChatType.transaction;
    }
  }
}
