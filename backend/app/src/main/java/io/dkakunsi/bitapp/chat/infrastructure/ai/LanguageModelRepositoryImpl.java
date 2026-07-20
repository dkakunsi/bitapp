package io.dkakunsi.bitapp.chat.infrastructure.ai;

import io.dkakunsi.bitapp.LanguageModel;
import io.dkakunsi.bitapp.chat.domain.entity.Draft;
import io.dkakunsi.bitapp.chat.domain.repository.LanguageModelRepository;
import io.dkakunsi.bitapp.langchain.PromptMessage;
import io.dkakunsi.bitapp.langchain.PromptResult;

public class LanguageModelRepositoryImpl implements LanguageModelRepository {

  private final LanguageModel languageModel;

  public LanguageModelRepositoryImpl(LanguageModel languageModel) {
    this.languageModel = languageModel;
  }

  @Override
  public PromptResult prompt(PromptMessage<Draft> promptMessage) {
    var result = languageModel.prompt(promptMessage.getPrompt());
    return PromptResult.of(result, promptMessage.getCrossDomainReferences());
  }
}
