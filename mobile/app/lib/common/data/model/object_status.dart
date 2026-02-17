enum ObjectStatus {
  active('ACTIVE'),
  inactive('INACTIVE');

  final String value;

  const ObjectStatus(this.value);

  static ObjectStatus valueOf(String s) {
    switch (s) {
      case 'ACTIVE':
        return ObjectStatus.active;
      case 'INACTIVE':
        return ObjectStatus.inactive;
      default:
        throw Exception('Unknown status: $s');
    }
  }
}
