package io.dkakunsi.bitapp.common.usecase;

public interface UseCase<IN, OUT> {
  Result<OUT> process(Input<IN> input);
}
