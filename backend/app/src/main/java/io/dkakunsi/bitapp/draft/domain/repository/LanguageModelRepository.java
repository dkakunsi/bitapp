package io.dkakunsi.bitapp.draft.domain.repository;

import io.dkakunsi.bitapp.draft.domain.entity.Draft;
import io.dkakunsi.bitapp.langchain.PromptMessage;
import io.dkakunsi.bitapp.langchain.PromptResult;

public interface LanguageModelRepository {

  PromptResult prompt(PromptMessage<Draft> promptMessage);

}
