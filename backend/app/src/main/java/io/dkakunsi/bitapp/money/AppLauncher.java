package io.dkakunsi.bitapp.money;

import java.util.function.Function;

import io.dkakunsi.bitapp.account.endpoint.CreateAccountJavalinEndpoint;
import io.dkakunsi.bitapp.account.endpoint.GetAccountJavalinEndpoint;
import io.dkakunsi.bitapp.account.endpoint.GetUserAccountsJavalinEndpoint;
import io.dkakunsi.bitapp.account.endpoint.UpdateAccountJavalinEndpoint;
import io.dkakunsi.bitapp.account.repository.MongoAccountRepository;
import io.dkakunsi.bitapp.account.usecase.CreateAccount;
import io.dkakunsi.bitapp.account.usecase.GetAccount;
import io.dkakunsi.bitapp.account.usecase.GetUserAccounts;
import io.dkakunsi.bitapp.account.usecase.UpdateAccount;
import io.dkakunsi.bitapp.common.EnvironmentConfiguration;
import io.dkakunsi.bitapp.common.Launcher;
import io.dkakunsi.bitapp.javalin.JavalinServer;
import io.dkakunsi.bitapp.jwt.JWTAuthorizer;
import io.dkakunsi.bitapp.loan.endpoint.CreateLoanJavalinEndpoint;
import io.dkakunsi.bitapp.loan.endpoint.GetLoanJavalinEndpoint;
import io.dkakunsi.bitapp.loan.endpoint.GetUserLoansJavalinEndpoint;
import io.dkakunsi.bitapp.loan.endpoint.UpdateLoanJavalinEndpoint;
import io.dkakunsi.bitapp.loan.repository.MongoLoanRepository;
import io.dkakunsi.bitapp.loan.usecase.CreateLoan;
import io.dkakunsi.bitapp.loan.usecase.GetLoan;
import io.dkakunsi.bitapp.loan.usecase.GetUserLoans;
import io.dkakunsi.bitapp.loan.usecase.UpdateLoan;
import io.dkakunsi.bitapp.mongo.MongoConfiguration;
import io.dkakunsi.bitapp.transaction.endpoint.CreateTransactionJavalinEndpoint;
import io.dkakunsi.bitapp.transaction.repository.MongoTransactionRepository;
import io.dkakunsi.bitapp.transaction.usecase.CreateTransaction;
import io.dkakunsi.bitapp.user.endpoint.GetUserJavalinEndpoint;
import io.dkakunsi.bitapp.user.endpoint.RegisterUserJavalinEndpoint;
import io.dkakunsi.bitapp.user.endpoint.UpdateUserLanguageJavalinEndpoint;
import io.dkakunsi.bitapp.user.repository.MongoUserRepository;
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
    var transactionRepository = new MongoTransactionRepository(datastore);

    // UseCases
    var registerUser = new RegisterUser(userRepository);
    var getUser = new GetUser(userRepository);
    var updateUserLanguage = new UpdateUserLanguage(userRepository);
    var createAccount = new CreateAccount(accountRepository);
    var getAccount = new GetAccount(accountRepository);
    var getUserAccounts = new GetUserAccounts(accountRepository);
    var updateAccount = new UpdateAccount(accountRepository);
    var createLoan = new CreateLoan(loanRepository);
    var getLoan = new GetLoan(loanRepository);
    var getUserLoans = new GetUserLoans(loanRepository);
    var updateLoan = new UpdateLoan(loanRepository);
    var createTransaction = new CreateTransaction(transactionRepository, accountRepository, loanRepository);

    // endpoints
    var authorizer = JWTAuthorizer.of(configuration);
    var registerUserEndpoint = new RegisterUserJavalinEndpoint(registerUser);
    var getUserEndpoint = new GetUserJavalinEndpoint(getUser)
        .setAuthorizer(authorizer);
    var updateUserLanguageEndpoint = new UpdateUserLanguageJavalinEndpoint(updateUserLanguage)
        .setAuthorizer(authorizer);
    var createAccountEndpoint = new CreateAccountJavalinEndpoint(createAccount)
        .setAuthorizer(authorizer);
    var getAccountEndpoint = new GetAccountJavalinEndpoint(getAccount)
        .setAuthorizer(authorizer);
    var getUserAccountsEndpoint = new GetUserAccountsJavalinEndpoint(getUserAccounts)
        .setAuthorizer(authorizer);
    var updateAccountEndpoint = new UpdateAccountJavalinEndpoint(updateAccount)
        .setAuthorizer(authorizer);
    var createLoanEndpoint = new CreateLoanJavalinEndpoint(createLoan)
        .setAuthorizer(authorizer);
    var getLoanEndpoint = new GetLoanJavalinEndpoint(getLoan)
        .setAuthorizer(authorizer);
    var getUserLoansEndpoint = new GetUserLoansJavalinEndpoint(getUserLoans)
        .setAuthorizer(authorizer);
    var updateLoanEndpoint = new UpdateLoanJavalinEndpoint(updateLoan)
        .setAuthorizer(authorizer);
    var createTransactionEndpoint = new CreateTransactionJavalinEndpoint(createTransaction)
        .setAuthorizer(authorizer);

    var appPort = configuration.get(APP_PORT).orElse("8080");
    server = JavalinServer.of(Integer.parseInt(appPort))
        .addEndpoint(registerUserEndpoint)
        .addEndpoint(getUserEndpoint)
        .addEndpoint(updateUserLanguageEndpoint)
        .addEndpoint(createAccountEndpoint)
        .addEndpoint(getAccountEndpoint)
        .addEndpoint(getUserAccountsEndpoint)
        .addEndpoint(updateAccountEndpoint)
        .addEndpoint(createLoanEndpoint)
        .addEndpoint(getLoanEndpoint)
        .addEndpoint(getUserLoansEndpoint)
        .addEndpoint(updateLoanEndpoint)
        .addEndpoint(createTransactionEndpoint)
        .start();
  }

  @Override
  public void stop() {
    if (server != null) {
      server.stop();
    }
  }
}
