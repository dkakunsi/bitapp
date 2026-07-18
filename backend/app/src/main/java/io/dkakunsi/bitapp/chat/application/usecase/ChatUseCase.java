package io.dkakunsi.bitapp.chat.application.usecase;

import java.util.Map;

import org.json.JSONObject;

import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.UseCase;
import io.dkakunsi.bitapp.chat.domain.entity.Chat;
import io.dkakunsi.bitapp.chat.domain.entity.Draft;
import io.dkakunsi.bitapp.chat.domain.entity.PromptMessage;
import io.dkakunsi.bitapp.chat.domain.repository.DraftRepository;
import io.dkakunsi.bitapp.chat.domain.repository.LanguageModelRepository;

public class ChatUseCase implements UseCase<Chat, Draft> {

  private final LanguageModelRepository languageModelRepository;

  private final DraftRepository draftRepository;

  private final Map<Chat.Type, PromptMessage.PromptMessageBuilder> promptMessageBuilders;

  public ChatUseCase(LanguageModelRepository languageModelRepository, DraftRepository draftRepository,
      Map<Chat.Type, PromptMessage.PromptMessageBuilder> promptMessageBuilders) {
    this.languageModelRepository = languageModelRepository;
    this.draftRepository = draftRepository;
    this.promptMessageBuilders = promptMessageBuilders;
  }

  @Override
  public Result<Draft> execute(Chat input) {
    var requester = getRequester();
    var draft = draftRepository.findOne(input.draftId())
        .orElse(Draft.from(input, requester));

    var promptMessageBuilder = promptMessageBuilders.get(input.type());
    var promptMessage = promptMessageBuilder.build(input, draft);
    var promptResult = languageModelRepository.prompt(promptMessage);

    draft = draft.updateData(new JSONObject(promptResult.data()));
    draftRepository.save(draft);

    return Result.success(draft);
  }
}
