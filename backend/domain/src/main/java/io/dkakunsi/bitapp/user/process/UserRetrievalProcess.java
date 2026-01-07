package io.dkakunsi.money.user.process;

import io.dkakunsi.lab.common.process.Process;
import io.dkakunsi.lab.common.process.ProcessError;
import io.dkakunsi.lab.common.process.ProcessInput;
import io.dkakunsi.lab.common.process.ProcessResult;
import io.dkakunsi.money.user.model.User;
import io.dkakunsi.money.user.port.UserPort;

public final class UserRetrievalProcess {

  private UserRepository userPort;

  public UserRetrievalProcess(UserRepository userPort) {
    this.userPort = userPort;
  }

  @Override
  public Result<User> process(ProcessInput<UserRetrievalInput> input) {
    try {
      return userPort.findByEmail(input.data().email())
          .map(user -> Result.success(user))
          .orElse(Result.success());
    } catch (Exception e) {
      return Result.failure(ProcessError.Code.SERVER_ERROR, e.getMessage());
    }
  }
}
