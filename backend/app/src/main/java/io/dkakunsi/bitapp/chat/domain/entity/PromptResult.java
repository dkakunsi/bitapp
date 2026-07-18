package io.dkakunsi.bitapp.chat.domain.entity;

public record PromptResult(
    Boolean success,
    String message,
    String data) {
}
