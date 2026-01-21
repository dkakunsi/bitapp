package io.dkakunsi.bitapp.money;

import java.util.function.Function;

import io.dkakunsi.bitapp.account.usecase.CreateAccount;
import io.dkakunsi.bitapp.account.usecase.GetUserAccounts;
import io.dkakunsi.bitapp.account.usecase.UpdateAccount;
import io.dkakunsi.bitapp.common.EnvironmentConfiguration;
import io.dkakunsi.bitapp.common.Launcher;
import io.dkakunsi.bitapp.javalin.JavalinServer;
import io.dkakunsi.bitapp.javalin.endpoint.account.CreateAccountJavalinEndpoint;
import io.dkakunsi.bitapp.javalin.endpoint.account.GetUserAccountsJavalinEndpoint;
import io.dkakunsi.bitapp.javalin.endpoint.account.UpdateAccountJavalinEndpoint;
import io.dkakunsi.bitapp.javalin.endpoint.loan.CreateLoanJavalinEndpoint;
import io.dkakunsi.bitapp.javalin.endpoint.loan.GetUserLoansJavalinEndpoint;
import io.dkakunsi.bitapp.javalin.endpoint.loan.UpdateLoanJavalinEndpoint;
import io.dkakunsi.bitapp.javalin.endpoint.user.GetUserJavalinEndpoint;
import io.dkakunsi.bitapp.javalin.endpoint.user.RegisterUserJavalinEndpoint;
import io.dkakunsi.bitapp.javalin.endpoint.user.UpdateUserLanguageJavalinEndpoint;
import io.dkakunsi.bitapp.jwt.JWTAuthorizer;
import io.dkakunsi.bitapp.loan.usecase.CreateLoan;
import io.dkakunsi.bitapp.loan.usecase.GetUserLoans;
import io.dkakunsi.bitapp.loan.usecase.UpdateLoan;
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
        var getUserLoans = new GetUserLoans(loanRepository);
        var updateLoan = new UpdateLoan(loanRepository);

        // endpoints
        var authorizer = JWTAuthorizer.of(configuration);
        var registerUserEndpoint = new RegisterUserJavalinEndpoint(registerUser);
        var getUserEndpoint = new GetUserJavalinEndpoint(getUser)
                .setAuthorizer(authorizer);
        var updateUserLanguageEndpoint = new UpdateUserLanguageJavalinEndpoint(updateUserLanguage)
                .setAuthorizer(authorizer);
        var createAccountEndpoint = new CreateAccountJavalinEndpoint(createAccount)
                .setAuthorizer(authorizer);
        var getUserAccountsEndpoint = new GetUserAccountsJavalinEndpoint(getUserAccounts)
                .setAuthorizer(authorizer);
        var updateAccountEndpoint = new UpdateAccountJavalinEndpoint(updateAccount)
                .setAuthorizer(authorizer);
        var createLoanEndpoint = new CreateLoanJavalinEndpoint(createLoan)
                .setAuthorizer(authorizer);
        var getUserLoansEndpoint = new GetUserLoansJavalinEndpoint(getUserLoans)
                .setAuthorizer(authorizer);
        var updateLoanEndpoint = new UpdateLoanJavalinEndpoint(updateLoan)
                .setAuthorizer(authorizer);

        var appPort = configuration.get(APP_PORT).orElse("8080");
        server = JavalinServer.of(Integer.parseInt(appPort))
                .addEndpoint(registerUserEndpoint)
                .addEndpoint(getUserEndpoint)
                .addEndpoint(updateUserLanguageEndpoint)
                .addEndpoint(createAccountEndpoint)
                .addEndpoint(getUserAccountsEndpoint)
                .addEndpoint(updateAccountEndpoint)
                .addEndpoint(createLoanEndpoint)
                .addEndpoint(getUserLoansEndpoint)
                .addEndpoint(updateLoanEndpoint)
                .start();
    }

    @Override
    public void stop() {
        if (server != null) {
            server.stop();
        }
    }
}
