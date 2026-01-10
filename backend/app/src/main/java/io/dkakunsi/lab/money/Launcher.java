package io.dkakunsi.lab.money;

import java.util.function.Function;

import io.dkakunsi.bitapp.account.usecase.CreateAccount;
import io.dkakunsi.bitapp.common.EnvironmentConfiguration;
import io.dkakunsi.bitapp.mongo.MongoConfiguration;
import io.dkakunsi.bitapp.mongo.repository.MongoAccountRepository;
import io.dkakunsi.bitapp.mongo.repository.MongoUserRepository;
import io.dkakunsi.bitapp.user.usecase.GetUser;
import io.dkakunsi.bitapp.user.usecase.RegisterUser;
import io.dkakunsi.lab.javalin.JavalinServer;
import io.dkakunsi.lab.javalin.endpoint.CreateAccountJavalinEndpoint;
import io.dkakunsi.lab.javalin.endpoint.GetUserJavalinEndpoint;
import io.dkakunsi.lab.javalin.endpoint.RegisterUserJavalinEndpoint;

public final class Launcher {

  private static final String APP_PORT = "app.port";

  private JavalinServer server;

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
    var createAccount = new CreateAccount(accountRepository);

    // endpoints
    var registerUserEndpoint = new RegisterUserJavalinEndpoint(registerUser, null);
    var getUserEndpoint = new GetUserJavalinEndpoint(getUser, null);
    var createAccountEndpoint = new CreateAccountJavalinEndpoint(createAccount, null);

    var appPort = configuration.get(APP_PORT).orElse("8080");
    server = JavalinServer.of(Integer.parseInt(appPort))
        .addEndpoint(registerUserEndpoint)
        .addEndpoint(getUserEndpoint)
        .addEndpoint(createAccountEndpoint)
        .start();
  }

  public void stop() {
    if (server != null) {
      server.stop();
    }
  }
}
