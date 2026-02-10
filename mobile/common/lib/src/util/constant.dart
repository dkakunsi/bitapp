enum Language {
  en(value: "EN"),
  id(value: "ID");

  static const defaultLanguage = Language.en;

  final String value;

  const Language({required this.value});

  static Language valueOf(String s) {
    switch (s) {
      case "ID":
        return Language.id;
      default:
        return Language.en;
    }
  }
}
