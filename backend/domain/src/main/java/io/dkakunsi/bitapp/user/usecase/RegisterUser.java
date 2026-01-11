package io.dkakunsi.bitapp.user.usecase;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.bitapp.user.dto.RegisterUserInput;
import io.dkakunsi.bitapp.user.dto.RegisterUserResult;
import io.dkakunsi.bitapp.user.model.User;
import io.dkakunsi.bitapp.user.repository.UserRepository;

public final class RegisterUser implements UseCase<RegisterUserInput, RegisterUserResult> {

  private UserRepository userRepository;

  public RegisterUser(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public Result<RegisterUserResult> process(Context context, RegisterUserInput input) {
    try {
      User user = userRepository.findByEmail(input.email())
          .map(existing -> update(existing, input))
          .orElseGet(() -> create(input));
      return Result.success(RegisterUserResult.from(user));
    } catch (IllegalArgumentException e) {
      return Result.failure(Code.BAD_REQUEST, e.getMessage());
    } catch (Exception e) {
      return Result.failure(Code.SERVER_ERROR, e.getMessage());
    }
  }

  private User update(User existingUser, RegisterUserInput userInput) {
    return existingUser.needUpdate(userInput) ? userRepository.save(existingUser.update(userInput)) : existingUser;
  }

  private User create(RegisterUserInput userInput) {
    return userRepository.save(userInput.toUser());
  }
}
