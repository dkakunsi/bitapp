package io.dkakunsi.bitapp;

import java.util.UUID;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

import lombok.Builder;

@Builder
public record Context(String requester, String requestId, String authorizationToken) {

  public static final ScopedValue<Context> CONTEXT = ScopedValue.newInstance();

  public static Context current() {
    return CONTEXT.get();
  }

  public static <T> T executeInContext(Context context, Supplier<T> runnable) {
    return ScopedValue.where(CONTEXT, context).call(() -> runnable.get());
  }

  public String requester() {
    return StringUtils.isNotEmpty(requester) ? requester : "N/A";
  }

  public String requestId() {
    return StringUtils.isNotEmpty(requestId) ? requestId : UUID.randomUUID().toString();
  }
}
