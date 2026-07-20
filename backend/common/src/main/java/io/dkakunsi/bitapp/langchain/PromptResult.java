package io.dkakunsi.bitapp.langchain;

import java.util.List;

import org.json.JSONObject;

import io.dkakunsi.bitapp.CrossDomainReference;

public record PromptResult(
    String error,
    String data,
    List<CrossDomainReference> crossDomainReferences) {

  public static PromptResult of(String modelResult, List<CrossDomainReference> crossDomainReferences) {
    var json = new JSONObject(modelResult);
    var error = json.optString("error", null);
    var data = json.optString("data", null);
    return new PromptResult(error, data, crossDomainReferences);
  }
}
