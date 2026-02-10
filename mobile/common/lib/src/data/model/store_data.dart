abstract class StoreData {
  final String? id;

  StoreData({required this.id});

  Map<String, dynamic> toStoreJson();
}
