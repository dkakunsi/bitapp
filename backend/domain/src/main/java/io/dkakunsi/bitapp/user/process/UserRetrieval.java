package io.dkakunsi.bitapp.user.process;

import io.dkakunsi.bitapp.common.Input;
import io.dkakunsi.bitapp.common.Result;
import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.user.dto.UserRetrievalInput;
import io.dkakunsi.bitapp.user.model.User;
import io.dkakunsi.bitapp.user.repository.UserRepository;

public final class UserRetrieval {

  private UserRepository userRepository;

  public UserRetrieval(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public Result<User> process(Input<UserRetrievalInput> input) {
    try {
      return userRepository.findByEmail(input.data().email())
          .map(user -> Result.success(user))
          .orElse(Result.success());
    } catch (Exception e) {
      return Result.failure(Code.SERVER_ERROR, e.getMessage());
    }
  }
}
