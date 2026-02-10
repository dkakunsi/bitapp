import 'dart:async';

class PeriodicAction {
  static final int defaultIntervalInMinutes = 60;
  final Future<void> Function() action;
  final int? durationInMinutes;
  Timer? _timer;

  PeriodicAction({required this.action, this.durationInMinutes});

  void start() {
    stop();

    try {
      final duration = durationInMinutes ?? defaultIntervalInMinutes;
      _timer = Timer.periodic(Duration(minutes: duration), (_) async {
        if (_timer != null && _timer!.isActive) {
          try {
            await action();
          } catch (e) {
            stop();
          }
        } else {
          stop();
        }
      });
    } catch (e) {
      stop();
    }
  }

  void stop() {
    if (isRunning()) {
      _timer?.cancel();
      _timer = null;
    }
  }

  bool isRunning() {
    return _timer?.isActive ?? false;
  }
}
