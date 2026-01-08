package io.dkakunsi.lab.test;

import io.dkakunsi.bitapp.common.Id;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TestObject {
  private Id id;
  private String code;
  private String name;

  public String toData() {
    return String.format("{\"id\":\"%s\",\"code\":\"%s\",\"name\":\"%s\"}",
        id.value(), code, name);
  }

  public static TestObject from(String data) {
    // Simple approach using string operations (not recommended for production)
    var idPattern = java.util.regex.Pattern.compile("\"id\": \"([^\"]+)\"");
    var codePattern = java.util.regex.Pattern.compile("\"code\": \"([^\"]+)\"");
    var namePattern = java.util.regex.Pattern.compile("\"name\": \"([^\"]+)\"");

    var idMatcher = idPattern.matcher(data);
    var codeMatcher = codePattern.matcher(data);
    var nameMatcher = namePattern.matcher(data);

    var idValue = idMatcher.find() ? idMatcher.group(1) : "";
    var codeValue = codeMatcher.find() ? codeMatcher.group(1) : "";
    var nameValue = nameMatcher.find() ? nameMatcher.group(1) : "";

    return TestObject.builder()
        .id(Id.of(idValue))
        .code(codeValue)
        .name(nameValue)
        .build();
  }
}
