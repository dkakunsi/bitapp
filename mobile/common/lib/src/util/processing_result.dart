class ProcessingResult<T> {
  T? _data;

  final Exception? exception;

  ProcessingResult({T? data, this.exception}) {
    _data = data;
  }

  bool get isSuccess => exception == null;

  bool get isFailure => exception != null;

  bool get isEmpty => _data == null;

  T get data {
    if (isFailure) {
      throw exception!;
    }
    if (isEmpty) {
      throw Exception('Data is empty');
    }
    return _data!;
  }
}
