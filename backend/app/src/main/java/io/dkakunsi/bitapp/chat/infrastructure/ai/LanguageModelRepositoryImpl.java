package io.dkakunsi.bitapp.chat.infrastructure.ai;

import io.dkakunsi.bitapp.chat.domain.entity.PromptMessage;
import io.dkakunsi.bitapp.chat.domain.entity.PromptResult;
import io.dkakunsi.bitapp.chat.domain.repository.LanguageModelRepository;

public class LanguageModelRepositoryImpl implements LanguageModelRepository {

  @Override
  public PromptResult prompt(PromptMessage promptMessage) {
    // TODO: Implement the prompt logic
    throw new UnsupportedOperationException("Unimplemented method 'prompt'");
  }
}
