package io.dkakunsi.bitapp.user.usecase;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.domain.usecase.Result;
import io.dkakunsi.bitapp.domain.usecase.UseCase;
import io.dkakunsi.bitapp.user.dto.UpdateUserInput;
import io.dkakunsi.bitapp.user.dto.UserResult;
import io.dkakunsi.bitapp.user.entity.User;
import io.dkakunsi.bitapp.user.repository.UserRepository;

public final class UpdateUser implements UseCase<UpdateUserInput, UserResult> {

  private final UserRepository userRepository;

  public UpdateUser(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Result<UserResult> execute(UpdateUserInput input) {
    var requester = getRequester();
    if (!requester.equals(input.email())) {
      return Result.failure(Code.BAD_REQUEST, "User can only update their own data");
    }

    return userRepository.findByEmail(input.email())
        .map(user -> {
          var updatedUser = user.updateLanguage(User.Language.valueOf(input.language()), requester);
          var savedUser = userRepository.save(updatedUser);
          return Result.success(savedUser.toResult());
        })
        .orElse(Result.failure(Code.NOT_FOUND, "User not found"));
  }
}
