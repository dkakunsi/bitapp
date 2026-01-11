package io.dkakunsi.bitapp.user.usecase;

import io.dkakunsi.bitapp.common.AppError.Code;
import io.dkakunsi.bitapp.common.usecase.Input;
import io.dkakunsi.bitapp.common.usecase.Result;
import io.dkakunsi.bitapp.common.usecase.UseCase;
import io.dkakunsi.bitapp.user.dto.UpdateUserLanguageInput;
import io.dkakunsi.bitapp.user.dto.UpdateUserLanguageResult;
import io.dkakunsi.bitapp.user.repository.UserRepository;

public final class UpdateUserLanguage implements UseCase<UpdateUserLanguageInput, UpdateUserLanguageResult> {

  private final UserRepository userRepository;

  public UpdateUserLanguage(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Result<UpdateUserLanguageResult> process(Input<UpdateUserLanguageInput> input) {
    try {
      return userRepository.findByEmail(input.data().email())
          .map(user -> {
            var updatedUser = user.updateLanguage(input.data().language());
            var savedUser = userRepository.save(updatedUser);
            return Result.success(UpdateUserLanguageResult.from(savedUser));
          })
          .orElse(Result.success());
    } catch (IllegalArgumentException e) {
      return Result.failure(Code.BAD_REQUEST, e.getMessage());
    } catch (Exception e) {
      return Result.failure(Code.SERVER_ERROR, e.getMessage());
    }
  }
}
