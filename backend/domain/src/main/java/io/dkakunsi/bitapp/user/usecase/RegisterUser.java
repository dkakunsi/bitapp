package io.dkakunsi.bitapp.user.usecase;

import io.dkakunsi.bitapp.domain.usecase.Result;
import io.dkakunsi.bitapp.domain.usecase.UseCase;
import io.dkakunsi.bitapp.user.dto.RegisterUserInput;
import io.dkakunsi.bitapp.user.dto.UserResult;
import io.dkakunsi.bitapp.user.entity.User;
import io.dkakunsi.bitapp.user.repository.UserRepository;

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
