package io.dkakunsi.bitapp.user.usecase;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.bitapp.user.dto.UserResult;
import io.dkakunsi.bitapp.user.repository.UserRepository;

public final class GetUser implements UseCase<String, UserResult> {

  private UserRepository userRepository;

  public GetUser(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Result<UserResult> execute(Context context, String email) {
    return userRepository.findByEmail(email)
        .map(user -> Result.success(user.toResult()))
        .orElse(Result.failure(Code.NOT_FOUND, "User not found"));
  }
}
