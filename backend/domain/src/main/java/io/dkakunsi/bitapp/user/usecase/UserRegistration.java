package io.dkakunsi.bitapp.user.usecase;

import io.dkakunsi.bitapp.common.Input;
import io.dkakunsi.bitapp.common.Result;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.user.dto.UserRegistrationInput;
import io.dkakunsi.bitapp.user.model.User;
import io.dkakunsi.bitapp.user.repository.UserRepository;

public final class UserRegistration {

  private UserRepository userRepository;

  public UserRegistration(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public Result<User> process(Input<UserRegistrationInput> input) {
    try {
      User user = userRepository.findByEmail(input.data().email())
          .map(existing -> update(existing, input.data()))
          .orElseGet(() -> create(input.data()));
      return Result.success(user);
    } catch (IllegalArgumentException e) {
      return Result.failure(Code.BAD_REQUEST, e.getMessage());
    } catch (Exception e) {
      return Result.failure(Code.SERVER_ERROR, e.getMessage());
    }
  }

  private User update(User existingUser, UserRegistrationInput userInput) {
    return existingUser.needUpdate(userInput) ? userRepository.save(existingUser.update(userInput)) : existingUser;
  }

  private User create(UserRegistrationInput userInput) {
    return userRepository.save(User.from(userInput));
  }
}
