package io.dkakunsi.bitapp.user.application.usecase;

import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.UseCase;
import io.dkakunsi.bitapp.user.application.dto.RegisterUserInput;
import io.dkakunsi.bitapp.user.application.dto.UserResult;
import io.dkakunsi.bitapp.user.domain.entity.User;
import io.dkakunsi.bitapp.user.domain.repository.UserRepository;

public final class RegisterUser implements UseCase<RegisterUserInput, UserResult> {

  private UserRepository userRepository;

  public RegisterUser(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Result<UserResult> execute(RegisterUserInput input) {
    User user = userRepository.findByEmail(input.email())
        .map(existing -> update(existing, input))
        .orElseGet(() -> create(input));
    return Result.success(user.toResult());
  }

  private User update(User existingUser, RegisterUserInput userInput) {
    return existingUser.needUpdate(userInput) ? userRepository.save(existingUser.update(userInput)) : existingUser;
  }

  private User create(RegisterUserInput userInput) {
    return userRepository.save(User.from(userInput));
  }
}
