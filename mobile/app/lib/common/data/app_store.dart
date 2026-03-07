import 'package:bitapp/common/data/model/store_data.dart';
import 'package:sembast/sembast_io.dart';
import 'package:sembast/utils/database_utils.dart';
import 'package:uuid/uuid.dart';

abstract class AppStore<T extends StoreData> {
  final Database database;
  late final StoreRef<String, Map<String, dynamic>> store;

  AppStore(this.database, String storeName) {
    store = stringMapStoreFactory.store(storeName);
  }

  T from(Map<String, dynamic> data);

  Future<void> save(T t) async {
    await store.record(t.id ?? Uuid().v4()).put(database, t.toStoreJson());
  }

  Future<void> addAll(List<T> listOfT) async {
    for (var t in listOfT) {
      await save(t);
    }
  }

  Future<T?> get(String id) async {
    var data = await store.record(id).get(database);
    return data != null ? from(data) : null;
  }

  Future<List<T>> getList(String userId) async {
    var finder = Finder(filter: Filter.equals('user', userId));
    final records = await store.find(database, finder: finder);
    return records.map((record) => from(record.value)).toList();
  }

  Future<void> delete(String id) async {
    await store.record(id).delete(database);
  }

  Future<void> clear() async {
    await store.delete(database);
  }
}
