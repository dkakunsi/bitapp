package io.dkakunsi.bitapp.chat.infrastructure.javalin;

import java.lang.reflect.Type;

import io.dkakunsi.bitapp.chat.application.usecase.ConfirmDraft;
import io.dkakunsi.bitapp.javalin.JavalinEndpoint;
import io.javalin.http.Context;

public class ConfirmDraftEndpoint extends JavalinEndpoint<String, Void> {

  public ConfirmDraftEndpoint(ConfirmDraft usecase) {
    super(usecase);
  }

  @Override
  public Method getMethod() {
    return Method.POST;
  }

  @Override
  public String getPath() {
    return "/v1/chats/{id}/confirm";
  }

  @Override
  protected String buildInput(Context ctx) {
    return ctx.pathParam("id");
  }

  @Override
  protected Type getOutputClass() {
    return Void.class;
  }
}
