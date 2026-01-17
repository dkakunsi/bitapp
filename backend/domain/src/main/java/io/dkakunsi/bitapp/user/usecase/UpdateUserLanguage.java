package io.dkakunsi.bitapp.user.usecase;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.Context;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.bitapp.user.dto.UpdateUserLanguageInput;
import io.dkakunsi.bitapp.user.dto.UpdateUserLanguageResult;
import io.dkakunsi.bitapp.user.model.User;
import io.dkakunsi.bitapp.user.repository.UserRepository;

public final class UpdateUserLanguage implements UseCase<UpdateUserLanguageInput, UpdateUserLanguageResult> {

  private final UserRepository userRepository;

  public UpdateUserLanguage(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Result<UpdateUserLanguageResult> process(Context context, UpdateUserLanguageInput input) {
    try {
      // Verify the authenticated user matches the email being updated
      var requester = context.requester();
      if (!requester.equals(input.email())) {
        return Result.failure(Code.BAD_REQUEST, "User can only update their own language preference");
      }

      return userRepository.findByEmail(input.email())
          .map(user -> {
            var updatedUser = user.updateLanguage(User.Language.from(input.language()));
            var savedUser = userRepository.save(updatedUser);
            return Result.success(UpdateUserLanguageResult.from(savedUser));
          })
          .orElse(Result.failure(Code.NOT_FOUND, "User not found"));
    } catch (IllegalArgumentException e) {
      return Result.failure(Code.BAD_REQUEST, e.getMessage());
    } catch (Exception e) {
      return Result.failure(Code.SERVER_ERROR, e.getMessage());
    }
  }
}
