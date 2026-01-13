package io.dkakunsi.lab.money;

import java.util.function.Function;

import io.dkakunsi.bitapp.account.usecase.CreateAccount;
import io.dkakunsi.bitapp.account.usecase.GetUserAccounts;
import io.dkakunsi.bitapp.account.usecase.UpdateAccount;
import io.dkakunsi.bitapp.common.EnvironmentConfiguration;
import io.dkakunsi.bitapp.common.Launcher;
import io.dkakunsi.bitapp.mongo.MongoConfiguration;
import io.dkakunsi.bitapp.mongo.repository.MongoAccountRepository;
import io.dkakunsi.bitapp.mongo.repository.MongoUserRepository;
import io.dkakunsi.bitapp.security.jwt.JWTAuthorizer;
import io.dkakunsi.bitapp.user.usecase.GetUser;
import io.dkakunsi.bitapp.user.usecase.RegisterUser;
import io.dkakunsi.bitapp.user.usecase.UpdateUserLanguage;
import io.dkakunsi.lab.javalin.JavalinServer;
import io.dkakunsi.lab.javalin.endpoint.CreateAccountJavalinEndpoint;
import io.dkakunsi.lab.javalin.endpoint.GetUserAccountsJavalinEndpoint;
import io.dkakunsi.lab.javalin.endpoint.GetUserJavalinEndpoint;
import io.dkakunsi.lab.javalin.endpoint.RegisterUserJavalinEndpoint;
import io.dkakunsi.lab.javalin.endpoint.UpdateAccountJavalinEndpoint;
import io.dkakunsi.lab.javalin.endpoint.UpdateUserLanguageJavalinEndpoint;

public final class AppLauncher implements Launcher {

  private static final String APP_PORT = "app.port";

  private JavalinServer server;

  @Override
  public void launch(Function<String, String> envProvider) {
    var configuration = EnvironmentConfiguration.of(envProvider);
    var mongoConfig = new MongoConfiguration(configuration);

    // Repositories
    var datastore = mongoConfig.getDatastore();
    var userRepository = new MongoUserRepository(datastore);
    var accountRepository = new MongoAccountRepository(datastore);

    // UseCases
    var registerUser = new RegisterUser(userRepository);
    var getUser = new GetUser(userRepository);
    var updateUserLanguage = new UpdateUserLanguage(userRepository);
    var createAccount = new CreateAccount(accountRepository);
    var getUserAccounts = new GetUserAccounts(accountRepository);
    var updateAccount = new UpdateAccount(accountRepository);

    // endpoints
    var authorizer = JWTAuthorizer.of(configuration);
    var registerUserEndpoint = new RegisterUserJavalinEndpoint(registerUser, null);
    var getUserEndpoint = new GetUserJavalinEndpoint(getUser, null);
    var updateUserLanguageEndpoint = new UpdateUserLanguageJavalinEndpoint(updateUserLanguage, authorizer);
    var createAccountEndpoint = new CreateAccountJavalinEndpoint(createAccount, authorizer);
    var getUserAccountsEndpoint = new GetUserAccountsJavalinEndpoint(getUserAccounts, authorizer);
    var updateAccountEndpoint = new UpdateAccountJavalinEndpoint(updateAccount, authorizer);

    var appPort = configuration.get(APP_PORT).orElse("8080");
    server = JavalinServer.of(Integer.parseInt(appPort))
        .addEndpoint(registerUserEndpoint)
        .addEndpoint(getUserEndpoint)
        .addEndpoint(updateUserLanguageEndpoint)
        .addEndpoint(createAccountEndpoint)
        .addEndpoint(getUserAccountsEndpoint)
        .addEndpoint(updateAccountEndpoint)
        .start();
  }

  @Override
  public void stop() {
    if (server != null) {
      server.stop();
    }
  }
}
