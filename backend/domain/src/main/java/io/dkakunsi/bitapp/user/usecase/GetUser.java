package io.dkakunsi.bitapp.user.usecase;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.bitapp.user.dto.GetUserInput;
import io.dkakunsi.bitapp.user.dto.UserResult;
import io.dkakunsi.bitapp.user.repository.UserRepository;

public final class GetUser implements UseCase<GetUserInput, UserResult> {

  private UserRepository userRepository;

  public GetUser(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Result<UserResult> execute(Context context, GetUserInput input) {
    return userRepository.findByEmail(input.email())
        .map(user -> Result.success(user.toResult()))
        .orElse(Result.failure(Code.NOT_FOUND, "User not found"));
  }
}
