bool isJsonArray(String s) {
  return s.startsWith("[") && s.endsWith("]");
}

bool isJsonObject(String s) {
  return s.startsWith("{") && s.endsWith("}");
}
