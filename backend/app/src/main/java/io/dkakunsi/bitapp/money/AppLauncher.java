package io.dkakunsi.bitapp.money;

import java.util.function.Function;

import io.dkakunsi.bitapp.account.usecase.CreateAccount;
import io.dkakunsi.bitapp.account.usecase.GetUserAccounts;
import io.dkakunsi.bitapp.account.usecase.UpdateAccount;
import io.dkakunsi.bitapp.common.EnvironmentConfiguration;
import io.dkakunsi.bitapp.common.Launcher;
import io.dkakunsi.bitapp.javalin.JavalinServer;
import io.dkakunsi.bitapp.javalin.endpoint.CreateAccountJavalinEndpoint;
import io.dkakunsi.bitapp.javalin.endpoint.CreateLoanJavalinEndpoint;
import io.dkakunsi.bitapp.javalin.endpoint.GetUserAccountsJavalinEndpoint;
import io.dkakunsi.bitapp.javalin.endpoint.GetUserJavalinEndpoint;
import io.dkakunsi.bitapp.javalin.endpoint.RegisterUserJavalinEndpoint;
import io.dkakunsi.bitapp.javalin.endpoint.UpdateAccountJavalinEndpoint;
import io.dkakunsi.bitapp.javalin.endpoint.UpdateUserLanguageJavalinEndpoint;
import io.dkakunsi.bitapp.jwt.JWTAuthorizer;
import io.dkakunsi.bitapp.loan.usecase.CreateLoan;
import io.dkakunsi.bitapp.mongo.MongoConfiguration;
import io.dkakunsi.bitapp.mongo.repository.MongoAccountRepository;
import io.dkakunsi.bitapp.mongo.repository.MongoLoanRepository;
import io.dkakunsi.bitapp.mongo.repository.MongoUserRepository;
import io.dkakunsi.bitapp.user.usecase.GetUser;
import io.dkakunsi.bitapp.user.usecase.RegisterUser;
import io.dkakunsi.bitapp.user.usecase.UpdateUserLanguage;

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
    var loanRepository = new MongoLoanRepository(datastore);

    // UseCases
    var registerUser = new RegisterUser(userRepository);
    var getUser = new GetUser(userRepository);
    var updateUserLanguage = new UpdateUserLanguage(userRepository);
    var createAccount = new CreateAccount(accountRepository);
    var getUserAccounts = new GetUserAccounts(accountRepository);
    var updateAccount = new UpdateAccount(accountRepository);
    var createLoan = new CreateLoan(loanRepository);

    // endpoints
    var authorizer = JWTAuthorizer.of(configuration);
    var registerUserEndpoint = new RegisterUserJavalinEndpoint(registerUser)
        .withValidator();
    var getUserEndpoint = new GetUserJavalinEndpoint(getUser)
        .setAuthorizer(authorizer)
        .withValidator();
    var updateUserLanguageEndpoint = new UpdateUserLanguageJavalinEndpoint(updateUserLanguage)
        .setAuthorizer(authorizer)
        .withValidator();
    var createAccountEndpoint = new CreateAccountJavalinEndpoint(createAccount)
        .setAuthorizer(authorizer)
        .withValidator();
    var getUserAccountsEndpoint = new GetUserAccountsJavalinEndpoint(getUserAccounts)
        .setAuthorizer(authorizer)
        .withValidator();
    var updateAccountEndpoint = new UpdateAccountJavalinEndpoint(updateAccount)
        .setAuthorizer(authorizer)
        .withValidator();
    var createLoanEndpoint = new CreateLoanJavalinEndpoint(createLoan)
        .setAuthorizer(authorizer)
        .withValidator();

    var appPort = configuration.get(APP_PORT).orElse("8080");
    server = JavalinServer.of(Integer.parseInt(appPort))
        .addEndpoint(registerUserEndpoint)
        .addEndpoint(getUserEndpoint)
        .addEndpoint(updateUserLanguageEndpoint)
        .addEndpoint(createAccountEndpoint)
        .addEndpoint(getUserAccountsEndpoint)
        .addEndpoint(updateAccountEndpoint)
        .addEndpoint(createLoanEndpoint)
        .start();
  }

  @Override
  public void stop() {
    if (server != null) {
      server.stop();
    }
  }
}
