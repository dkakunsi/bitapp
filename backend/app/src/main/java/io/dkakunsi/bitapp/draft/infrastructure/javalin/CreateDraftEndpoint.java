package io.dkakunsi.bitapp.draft.infrastructure.javalin;

import java.lang.reflect.Type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.dkakunsi.bitapp.draft.application.usecase.CreateDraft;
import io.dkakunsi.bitapp.draft.domain.entity.Chat;
import io.dkakunsi.bitapp.draft.domain.entity.Draft;
import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.javalin.http.Context;

public final class CreateDraftEndpoint extends JavalinEndpoint<Chat, Draft> {

  public CreateDraftEndpoint(CreateDraft usecase) {
    super(usecase);
  }

  @Override
  public Method getMethod() {
    return Method.POST;
  }

  @Override
  public String getPath() {
    return "/v1/chats";
  }

  @Override
  protected Chat buildInput(Context ctx) {
    var body = ctx.bodyAsClass(ChatRequest.class);
    return Chat.builder()
        .type(Chat.Type.valueOf(body.type()))
        .draftId(body.draftId())
        .message(body.message())
        .language(body.language())
        .build();
  }

  @Override
  protected Type getOutputClass() {
    return Draft.class;
  }
}

@JsonIgnoreProperties(ignoreUnknown = true)
final record ChatRequest(
    String type,
    String draftId,
    String message,
    String language) {
}
