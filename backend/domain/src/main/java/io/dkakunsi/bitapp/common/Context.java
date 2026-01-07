package io.dkakunsi.bitapp.common;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import lombok.Builder;

@Builder
public record Context(String requester, String requestId, String authorizationToken) {

  private static ThreadLocal<Context> context = new InheritableThreadLocal<>();

  public static Context get() {
    return context.get();
  }

  public static void set(Context ctx) {
    context.set(ctx);
  }

  public String requester() {
    return StringUtils.isNotEmpty(requester) ? requester : "N/A";
  }

  public String requestId() {
    return StringUtils.isNotEmpty(requestId) ? requestId : UUID.randomUUID().toString();
  }
}
