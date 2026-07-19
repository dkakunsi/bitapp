package io.dkakunsi.bitapp.chat.infrastructure.ai;

import org.json.JSONObject;

import io.dkakunsi.bitapp.LanguageModel;
import io.dkakunsi.bitapp.chat.domain.entity.PromptMessage;
import io.dkakunsi.bitapp.chat.domain.entity.PromptResult;
import io.dkakunsi.bitapp.chat.domain.repository.LanguageModelRepository;

public class LanguageModelRepositoryImpl implements LanguageModelRepository {

  private final LanguageModel languageModel;

  public LanguageModelRepositoryImpl(LanguageModel languageModel) {
    this.languageModel = languageModel;
  }

  @Override
  public PromptResult prompt(PromptMessage promptMessage) {
    var result = languageModel.prompt(promptMessage.getPrompt());
    var json = new JSONObject(result);
    var error = json.optString("error", null);
    var data = json.optString("data", null);
    return new PromptResult(error, data);
  }
}
