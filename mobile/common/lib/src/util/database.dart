import 'package:path/path.dart';
import 'package:path_provider/path_provider.dart';
import 'package:sembast/sembast_io.dart';

Future<Database> getDatabase(String dbName) async {
  var dir = await getApplicationDocumentsDirectory();
  await dir.create(recursive: true);
  var dbPath = join(dir.path, dbName);
  return await databaseFactoryIo.openDatabase(dbPath);
}
