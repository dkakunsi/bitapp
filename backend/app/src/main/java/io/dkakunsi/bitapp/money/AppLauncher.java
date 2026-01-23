package io.dkakunsi.bitapp.money;

import java.util.function.Function;

import io.dkakunsi.bitapp.account.endpoint.CreateAccountEndpoint;
import io.dkakunsi.bitapp.account.endpoint.GetAccountEndpoint;
import io.dkakunsi.bitapp.account.endpoint.GetUserAccountsEndpoint;
import io.dkakunsi.bitapp.account.endpoint.UpdateAccountEndpoint;
import io.dkakunsi.bitapp.account.repository.MongoAccountRepository;
import io.dkakunsi.bitapp.account.usecase.CreateAccount;
import io.dkakunsi.bitapp.account.usecase.GetAccount;
import io.dkakunsi.bitapp.account.usecase.GetUserAccounts;
import io.dkakunsi.bitapp.account.usecase.UpdateAccount;
import io.dkakunsi.bitapp.common.EnvironmentConfiguration;
import io.dkakunsi.bitapp.common.Launcher;
import io.dkakunsi.bitapp.javalin.JavalinServer;
import io.dkakunsi.bitapp.jwt.JWTAuthorizer;
import io.dkakunsi.bitapp.loan.endpoint.CreateLoanEndpoint;
import io.dkakunsi.bitapp.loan.endpoint.GetLoanEndpoint;
import io.dkakunsi.bitapp.loan.endpoint.GetUserLoansEndpoint;
import io.dkakunsi.bitapp.loan.endpoint.UpdateLoanEndpoint;
import io.dkakunsi.bitapp.loan.repository.MongoLoanRepository;
import io.dkakunsi.bitapp.loan.usecase.CreateLoan;
import io.dkakunsi.bitapp.loan.usecase.GetLoan;
import io.dkakunsi.bitapp.loan.usecase.GetUserLoans;
import io.dkakunsi.bitapp.loan.usecase.UpdateLoan;
import io.dkakunsi.bitapp.mongo.MongoConfiguration;
import io.dkakunsi.bitapp.transaction.endpoint.CreateTransactionEndpoint;
import io.dkakunsi.bitapp.transaction.endpoint.GetTransactionEndpoint;
import io.dkakunsi.bitapp.transaction.endpoint.GetUserTransactionsEndpoint;
import io.dkakunsi.bitapp.transaction.repository.MongoTransactionRepository;
import io.dkakunsi.bitapp.transaction.usecase.CreateTransaction;
import io.dkakunsi.bitapp.transaction.usecase.GetTransaction;
import io.dkakunsi.bitapp.transaction.usecase.GetUserTransactions;
import io.dkakunsi.bitapp.user.endpoint.GetUserEndpoint;
import io.dkakunsi.bitapp.user.endpoint.RegisterUserEndpoint;
import io.dkakunsi.bitapp.user.endpoint.UpdateUserEndpoint;
import io.dkakunsi.bitapp.user.repository.MongoUserRepository;
import io.dkakunsi.bitapp.user.usecase.GetUser;
import io.dkakunsi.bitapp.user.usecase.RegisterUser;
import io.dkakunsi.bitapp.user.usecase.UpdateUser;

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
    var updateUser = new UpdateUser(userRepository);
    var createAccount = new CreateAccount(accountRepository);
    var getAccount = new GetAccount(accountRepository);
    var getUserAccounts = new GetUserAccounts(accountRepository);
    var updateAccount = new UpdateAccount(accountRepository);
    var createLoan = new CreateLoan(loanRepository);
    var getLoan = new GetLoan(loanRepository);
    var getUserLoans = new GetUserLoans(loanRepository);
    var updateLoan = new UpdateLoan(loanRepository);
    var createTransaction = new CreateTransaction(transactionRepository, accountRepository, loanRepository);
    var getTransaction = new GetTransaction(transactionRepository);
    var getUserTransactions = new GetUserTransactions(transactionRepository);

    // endpoints
    var authorizer = JWTAuthorizer.of(configuration);
    var registerUserEndpoint = new RegisterUserEndpoint(registerUser);
    var getUserEndpoint = new GetUserEndpoint(getUser)
        .setAuthorizer(authorizer);
    var updateUserEndpoint = new UpdateUserEndpoint(updateUser)
        .setAuthorizer(authorizer);
    var createAccountEndpoint = new CreateAccountEndpoint(createAccount)
        .setAuthorizer(authorizer);
    var getAccountEndpoint = new GetAccountEndpoint(getAccount)
        .setAuthorizer(authorizer);
    var getUserAccountsEndpoint = new GetUserAccountsEndpoint(getUserAccounts)
        .setAuthorizer(authorizer);
    var updateAccountEndpoint = new UpdateAccountEndpoint(updateAccount)
        .setAuthorizer(authorizer);
    var createLoanEndpoint = new CreateLoanEndpoint(createLoan)
        .setAuthorizer(authorizer);
    var getLoanEndpoint = new GetLoanEndpoint(getLoan)
        .setAuthorizer(authorizer);
    var getUserLoansEndpoint = new GetUserLoansEndpoint(getUserLoans)
        .setAuthorizer(authorizer);
    var updateLoanEndpoint = new UpdateLoanEndpoint(updateLoan)
        .setAuthorizer(authorizer);
    var createTransactionEndpoint = new CreateTransactionEndpoint(createTransaction)
        .setAuthorizer(authorizer);
    var getTransactionEndpoint = new GetTransactionEndpoint(getTransaction)
        .setAuthorizer(authorizer);
    var getUserTransactionsEndpoint = new GetUserTransactionsEndpoint(getUserTransactions)
        .setAuthorizer(authorizer);

    var appPort = configuration.get(APP_PORT).orElse("8080");
    server = JavalinServer.of(Integer.parseInt(appPort))
        .addEndpoint(registerUserEndpoint)
        .addEndpoint(getUserEndpoint)
        .addEndpoint(updateUserEndpoint)
        .addEndpoint(createAccountEndpoint)
        .addEndpoint(getAccountEndpoint)
        .addEndpoint(getUserAccountsEndpoint)
        .addEndpoint(updateAccountEndpoint)
        .addEndpoint(createLoanEndpoint)
        .addEndpoint(getLoanEndpoint)
        .addEndpoint(getUserLoansEndpoint)
        .addEndpoint(updateLoanEndpoint)
        .addEndpoint(createTransactionEndpoint)
        .addEndpoint(getTransactionEndpoint)
        .addEndpoint(getUserTransactionsEndpoint)
        .start();
  }

  @Override
  public void stop() {
    if (server != null) {
      server.stop();
    }
  }
}
