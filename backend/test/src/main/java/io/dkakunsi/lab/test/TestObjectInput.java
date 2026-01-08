package io.dkakunsi.lab.test;

import lombok.Builder;

@Builder
public record TestObjectInput(String code, String name) {
}
