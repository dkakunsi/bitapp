package io.dkakunsi.bitapp;

import java.util.function.Function;

import io.dkakunsi.bitapp.account.application.usecase.CreateAccount;
import io.dkakunsi.bitapp.account.application.usecase.GetAccount;
import io.dkakunsi.bitapp.account.application.usecase.GetUserAccounts;
import io.dkakunsi.bitapp.account.application.usecase.RemoveAccount;
import io.dkakunsi.bitapp.account.application.usecase.UpdateAccount;
import io.dkakunsi.bitapp.account.infrastructure.javalin.CreateAccountEndpoint;
import io.dkakunsi.bitapp.account.infrastructure.javalin.GetAccountEndpoint;
import io.dkakunsi.bitapp.account.infrastructure.javalin.GetUserAccountsEndpoint;
import io.dkakunsi.bitapp.account.infrastructure.javalin.RemoveAccountEndpoint;
import io.dkakunsi.bitapp.account.infrastructure.javalin.UpdateAccountEndpoint;
import io.dkakunsi.bitapp.account.infrastructure.mongo.repository.MongoAccountRepository;
import io.dkakunsi.bitapp.javalin.JavalinServer;
import io.dkakunsi.bitapp.jwt.JWKAuthorizer;
import io.dkakunsi.bitapp.jwt.JWTAuthorizer;
import io.dkakunsi.bitapp.loan.application.usecase.CreateLoan;
import io.dkakunsi.bitapp.loan.application.usecase.GetLoan;
import io.dkakunsi.bitapp.loan.application.usecase.GetUserLoans;
import io.dkakunsi.bitapp.loan.application.usecase.RemoveLoan;
import io.dkakunsi.bitapp.loan.application.usecase.UpdateLoan;
import io.dkakunsi.bitapp.loan.infrastructure.javalin.CreateLoanEndpoint;
import io.dkakunsi.bitapp.loan.infrastructure.javalin.GetLoanEndpoint;
import io.dkakunsi.bitapp.loan.infrastructure.javalin.GetUserLoansEndpoint;
import io.dkakunsi.bitapp.loan.infrastructure.javalin.RemoveLoanEndpoint;
import io.dkakunsi.bitapp.loan.infrastructure.javalin.UpdateLoanEndpoint;
import io.dkakunsi.bitapp.loan.infrastructure.mongo.repository.MongoLoanRepository;
import io.dkakunsi.bitapp.mongo.MongoConfiguration;
import io.dkakunsi.bitapp.transaction.application.usecase.CreateTransaction;
import io.dkakunsi.bitapp.transaction.application.usecase.GetAccountTransactions;
import io.dkakunsi.bitapp.transaction.application.usecase.GetLoanTransactions;
import io.dkakunsi.bitapp.transaction.application.usecase.GetTransaction;
import io.dkakunsi.bitapp.transaction.application.usecase.GetUserTransactions;
import io.dkakunsi.bitapp.transaction.application.usecase.RemoveTransaction;
import io.dkakunsi.bitapp.transaction.infrastructure.javalin.CreateTransactionEndpoint;
import io.dkakunsi.bitapp.transaction.infrastructure.javalin.GetAccountTransactionsEndpoint;
import io.dkakunsi.bitapp.transaction.infrastructure.javalin.GetLoanTransactionsEndpoint;
import io.dkakunsi.bitapp.transaction.infrastructure.javalin.GetTransactionEndpoint;
import io.dkakunsi.bitapp.transaction.infrastructure.javalin.GetUserTransactionsEndpoint;
import io.dkakunsi.bitapp.transaction.infrastructure.javalin.RemoveTransactionEndpoint;
import io.dkakunsi.bitapp.transaction.infrastructure.mongo.repository.MongoTransactionRepository;
import io.dkakunsi.bitapp.user.application.usecase.GetUser;
import io.dkakunsi.bitapp.user.application.usecase.RegisterUser;
import io.dkakunsi.bitapp.user.application.usecase.UpdateUser;
import io.dkakunsi.bitapp.user.infrastructure.database.repository.MongoUserRepository;
import io.dkakunsi.bitapp.user.infrastructure.javalin.GetUserEndpoint;
import io.dkakunsi.bitapp.user.infrastructure.javalin.RegisterUserEndpoint;
import io.dkakunsi.bitapp.user.infrastructure.javalin.UpdateUserEndpoint;

public final class AppLauncher implements Launcher {

  private static final String APP_PORT = "app.port";

  private JavalinServer server;

  @Override
  public void launch(Function<String, String> envProvider) {
    var configuration = EnvironmentConfiguration.of(envProvider);
    var mongoConfig = new MongoConfiguration(configuration);

    var sessionManager = mongoConfig.getSessionManager();

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
    var getLoan = new GetLoan(loanRepository);
    var getUserLoans = new GetUserLoans(loanRepository);
    var updateLoan = new UpdateLoan(loanRepository);
    var removeLoan = new RemoveLoan(loanRepository, transactionRepository, sessionManager);
    var createTransaction = new CreateTransaction(transactionRepository, accountRepository, loanRepository,
        sessionManager);
    var getTransaction = new GetTransaction(transactionRepository);
    var getUserTransactions = new GetUserTransactions(transactionRepository);
    var getAccountTransactions = new GetAccountTransactions(transactionRepository);
    var getLoanTransactions = new GetLoanTransactions(transactionRepository);
    var removeTransaction = new RemoveTransaction(transactionRepository, accountRepository, loanRepository,
        sessionManager);

    var createLoan = new CreateLoan(loanRepository, accountRepository, sessionManager, createTransaction);
    var removeAccount = new RemoveAccount(accountRepository, transactionRepository, loanRepository, removeLoan,
        sessionManager);

    // endpoints
    var authorizer = createAuthorizer(configuration);
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
    var removeAccountEndpoint = new RemoveAccountEndpoint(removeAccount)
        .setAuthorizer(authorizer);
    var createLoanEndpoint = new CreateLoanEndpoint(createLoan)
        .setAuthorizer(authorizer);
    var getLoanEndpoint = new GetLoanEndpoint(getLoan)
        .setAuthorizer(authorizer);
    var getUserLoansEndpoint = new GetUserLoansEndpoint(getUserLoans)
        .setAuthorizer(authorizer);
    var updateLoanEndpoint = new UpdateLoanEndpoint(updateLoan)
        .setAuthorizer(authorizer);
    var removeLoanEndpoint = new RemoveLoanEndpoint(removeLoan)
        .setAuthorizer(authorizer);
    var createTransactionEndpoint = new CreateTransactionEndpoint(createTransaction)
        .setAuthorizer(authorizer);
    var getTransactionEndpoint = new GetTransactionEndpoint(getTransaction)
        .setAuthorizer(authorizer);
    var getUserTransactionsEndpoint = new GetUserTransactionsEndpoint(getUserTransactions)
        .setAuthorizer(authorizer);
    var getAccountTransactionsEndpoint = new GetAccountTransactionsEndpoint(getAccountTransactions)
        .setAuthorizer(authorizer);
    var getLoanTransactionsEndpoint = new GetLoanTransactionsEndpoint(getLoanTransactions)
        .setAuthorizer(authorizer);
    var removeTransactionEndpoint = new RemoveTransactionEndpoint(removeTransaction)
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
        .addEndpoint(removeAccountEndpoint)
        .addEndpoint(createLoanEndpoint)
        .addEndpoint(getLoanEndpoint)
        .addEndpoint(getUserLoansEndpoint)
        .addEndpoint(updateLoanEndpoint)
        .addEndpoint(removeLoanEndpoint)
        .addEndpoint(createTransactionEndpoint)
        .addEndpoint(getTransactionEndpoint)
        .addEndpoint(getUserTransactionsEndpoint)
        .addEndpoint(getAccountTransactionsEndpoint)
        .addEndpoint(getLoanTransactionsEndpoint)
        .addEndpoint(removeTransactionEndpoint)
        .start();
  }

  @Override
  public void stop() {
    if (server != null) {
      server.stop();
    }
  }

  private static Authorizer createAuthorizer(Configuration configuration) {
    var isTestEnv = configuration.get("app.env").orElse("").equalsIgnoreCase("test");
    return isTestEnv ? JWTAuthorizer.of(configuration) : JWKAuthorizer.of(configuration);
  }
}
