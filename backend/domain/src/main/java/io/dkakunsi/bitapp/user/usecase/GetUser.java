package io.dkakunsi.bitapp.user.usecase;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.bitapp.user.dto.GetUserInput;
import io.dkakunsi.bitapp.user.dto.GetUserResult;
import io.dkakunsi.bitapp.user.repository.UserRepository;

public final class GetUser implements UseCase<GetUserInput, GetUserResult> {

  private UserRepository userRepository;

  public GetUser(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public Result<GetUserResult> process(Context context, GetUserInput input) {
    try {
      return userRepository.findByEmail(input.email())
          .map(user -> Result.success(GetUserResult.from(user)))
          .orElse(Result.failure(Code.NOT_FOUND, "User not found"));
    } catch (Exception e) {
      return Result.failure(Code.SERVER_ERROR, e.getMessage());
    }
  }
}
