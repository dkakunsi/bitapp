package io.dkakunsi.bitapp.chat.domain.repository;

import io.dkakunsi.bitapp.chat.domain.entity.Draft;
import io.dkakunsi.bitapp.langchain.PromptMessage;
import io.dkakunsi.bitapp.langchain.PromptResult;

public interface LanguageModelRepository {

  PromptResult prompt(PromptMessage<Draft> promptMessage);

}
