package io.dkakunsi.bitapp.user.usecase;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.bitapp.user.dto.UpdateUserLanguageInput;
import io.dkakunsi.bitapp.user.dto.UserResult;
import io.dkakunsi.bitapp.user.entity.User;
import io.dkakunsi.bitapp.user.repository.UserRepository;

public final class UpdateUserLanguage implements UseCase<UpdateUserLanguageInput, UserResult> {

  private final UserRepository userRepository;

  public UpdateUserLanguage(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Result<UserResult> execute(Context context, UpdateUserLanguageInput input) {
    // Verify the authenticated user matches the email being updated
    var requester = context.requester();
    if (!requester.equals(input.email())) {
      return Result.failure(Code.BAD_REQUEST, "User can only update their own language preference");
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
