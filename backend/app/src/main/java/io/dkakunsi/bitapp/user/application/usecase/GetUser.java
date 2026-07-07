package io.dkakunsi.bitapp.user.application.usecase;

import io.dkakunsi.bitapp.AppError.Code;
import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.UseCase;
import io.dkakunsi.bitapp.user.application.dto.UserResult;
import io.dkakunsi.bitapp.user.domain.repository.UserRepository;

public final class GetUser implements UseCase<String, UserResult> {

  private UserRepository userRepository;

  public GetUser(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Result<UserResult> execute(String email) {
    return userRepository.findByEmail(email)
        .map(user -> Result.success(UserResult.from(user)))
        .orElse(Result.failure(Code.NOT_FOUND, "User not found"));
  }
}
