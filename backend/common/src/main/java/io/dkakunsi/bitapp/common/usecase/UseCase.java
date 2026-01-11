package io.dkakunsi.bitapp.common.usecase;

import io.dkakunsi.bitapp.common.Context;

public interface UseCase<IN, OUT> {
  Result<OUT> process(Context context, IN input);
}
