package io.dkakunsi.bitapp.draft.infrastructure.ai;

import io.dkakunsi.bitapp.LanguageModel;
import io.dkakunsi.bitapp.Logger;
import io.dkakunsi.bitapp.Logger.SystemLogger;
import io.dkakunsi.bitapp.draft.domain.entity.Draft;
import io.dkakunsi.bitapp.draft.domain.repository.LanguageModelRepository;
import io.dkakunsi.bitapp.langchain.PromptMessage;
import io.dkakunsi.bitapp.langchain.PromptResult;

public class LanguageModelRepositoryImpl implements LanguageModelRepository {

  private static final Logger LOGGER = SystemLogger.getLogger(LanguageModelRepositoryImpl.class);

  private final LanguageModel languageModel;

  public LanguageModelRepositoryImpl(LanguageModel languageModel) {
    this.languageModel = languageModel;
  }

  @Override
  public PromptResult prompt(PromptMessage<Draft> promptMessage) {
    var result = languageModel.prompt(promptMessage.getPrompt());
    LOGGER.info("Prompt result: {}", result);
    return PromptResult.of(result, promptMessage.getCrossDomainReferences());
  }
}
