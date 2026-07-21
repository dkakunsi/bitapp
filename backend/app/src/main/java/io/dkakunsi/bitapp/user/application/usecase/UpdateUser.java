package io.dkakunsi.bitapp.user.application.usecase;

import io.dkakunsi.bitapp.Result;
import io.dkakunsi.bitapp.UseCase;
import io.dkakunsi.bitapp.user.application.dto.UpdateUserInput;
import io.dkakunsi.bitapp.user.application.dto.UserResult;
import io.dkakunsi.bitapp.user.domain.entity.User;
import io.dkakunsi.bitapp.user.domain.repository.UserRepository;

public final class UpdateUser implements UseCase<UpdateUserInput, UserResult> {

  private final UserRepository userRepository;

  public UpdateUser(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Result<UserResult> execute(UpdateUserInput input) {
    var requester = getRequester();
    if (!requester.equals(input.email())) {
      return Result.badRequest("User can only update their own data");
    }

    return userRepository.findByEmail(input.email())
        .map(user -> {
          var updatedUser = user.updateLanguage(User.Language.valueOf(input.language()), requester);
          var savedUser = userRepository.save(updatedUser);
          return Result.success(UserResult.from(savedUser));
        })
        .orElse(Result.notFound("User not found"));
  }
}
