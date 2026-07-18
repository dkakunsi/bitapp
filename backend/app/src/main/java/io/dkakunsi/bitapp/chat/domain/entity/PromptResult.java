package io.dkakunsi.bitapp.chat.domain.entity;

public record PromptResult(
    Boolean success,
    String error,
    String data) {
}
