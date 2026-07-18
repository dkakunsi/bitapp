package io.dkakunsi.bitapp.chat.domain.repository;

import io.dkakunsi.bitapp.chat.domain.entity.PromptMessage;
import io.dkakunsi.bitapp.chat.domain.entity.PromptResult;

public interface LanguageModelRepository {

  PromptResult prompt(PromptMessage promptMessage);

}
