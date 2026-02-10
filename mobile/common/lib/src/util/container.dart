import 'package:kiwi/kiwi.dart';

KiwiContainer container = KiwiContainer();

void addInstance<T>(T instance) {
  container.registerInstance<T>(instance);
}

T getInstance<T>() {
  return container.resolve<T>();
}
