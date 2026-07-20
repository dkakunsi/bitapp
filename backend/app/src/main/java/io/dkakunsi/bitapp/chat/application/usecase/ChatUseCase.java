package io.dkakunsi.bitapp.chat.application.usecase;

import java.util.Map;

import io.dkakunsi.bitapp.Id;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.UseCase;
import io.dkakunsi.bitapp.chat.domain.entity.Chat;
import io.dkakunsi.bitapp.chat.domain.entity.Draft;
import io.dkakunsi.bitapp.chat.domain.repository.DraftRepository;
import io.dkakunsi.bitapp.chat.domain.repository.LanguageModelRepository;
import io.dkakunsi.bitapp.langchain.PromptMessage.PromptMessageBuilder;

public class ChatUseCase implements UseCase<Chat, Draft> {

  private final LanguageModelRepository languageModelRepository;

  private final DraftRepository draftRepository;

  private final Map<Chat.Type, PromptMessageBuilder<Draft>> promptMessageBuilders;

  public ChatUseCase(LanguageModelRepository languageModelRepository, DraftRepository draftRepository,
      Map<Chat.Type, PromptMessageBuilder<Draft>> promptMessageBuilders) {
    this.languageModelRepository = languageModelRepository;
    this.draftRepository = draftRepository;
    this.promptMessageBuilders = promptMessageBuilders;
  }

  @Override
  public Result<Draft> execute(Chat input) {
    var requester = getRequester();
    var draft = draftRepository.findByIdAndNotConfirmed(Id.of(input.draftId()))
        .map(d -> d.addChat(input))
        .orElseGet(() -> Draft.from(input, requester));

    var promptMessageBuilder = promptMessageBuilders.get(draft.type());
    var promptMessage = promptMessageBuilder.build(draft);
    var promptResult = languageModelRepository.prompt(promptMessage);

    draft = draft.update(promptResult);
    draftRepository.save(draft);

    return Result.success(draft);
  }
}
