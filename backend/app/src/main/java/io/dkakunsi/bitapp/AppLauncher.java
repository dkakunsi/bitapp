package io.dkakunsi.bitapp;

import java.util.Map;
import java.util.function.Function;

import io.dkakunsi.bitapp.Configuration.EnvironmentConfiguration;
import io.dkakunsi.bitapp.account.application.usecase.CreateAccount;
import io.dkakunsi.bitapp.account.application.usecase.GetAccount;
import io.dkakunsi.bitapp.account.application.usecase.GetUserAccounts;
import io.dkakunsi.bitapp.account.application.usecase.RemoveAccount;
import io.dkakunsi.bitapp.account.application.usecase.UpdateAccount;
import io.dkakunsi.bitapp.account.application.usecase.UpdateBalance;
import io.dkakunsi.bitapp.account.infrastructure.javalin.CreateAccountEndpoint;
import io.dkakunsi.bitapp.account.infrastructure.javalin.GetAccountEndpoint;
import io.dkakunsi.bitapp.account.infrastructure.javalin.GetUserAccountsEndpoint;
import io.dkakunsi.bitapp.account.infrastructure.javalin.RemoveAccountEndpoint;
import io.dkakunsi.bitapp.account.infrastructure.javalin.UpdateAccountEndpoint;
import io.dkakunsi.bitapp.account.infrastructure.loan.InProcessAccountLoanAdapter;
import io.dkakunsi.bitapp.account.infrastructure.mongo.repository.MongoAccountRepository;
import io.dkakunsi.bitapp.account.infrastructure.transaction.InProcessAccountTransactionAdapter;
import io.dkakunsi.bitapp.chat.application.usecase.ConfirmDraft;
import io.dkakunsi.bitapp.chat.application.usecase.CreateDraft;
import io.dkakunsi.bitapp.chat.domain.entity.Chat;
import io.dkakunsi.bitapp.chat.infrastructure.account.InProcessChatAccountAdapter;
import io.dkakunsi.bitapp.chat.infrastructure.ai.LanguageModelRepositoryImpl;
import io.dkakunsi.bitapp.chat.infrastructure.ai.prompt.AccountPromptMessage.AccountPromptMessageBuilder;
import io.dkakunsi.bitapp.chat.infrastructure.ai.prompt.LoanPromptMessage.LoanPromptMessageBuilder;
import io.dkakunsi.bitapp.chat.infrastructure.ai.prompt.TransactionPromptMessage.TransactionPromptMessageBuilder;
import io.dkakunsi.bitapp.chat.infrastructure.javalin.ConfirmDraftEndpoint;
import io.dkakunsi.bitapp.chat.infrastructure.javalin.CreateDraftEndpoint;
import io.dkakunsi.bitapp.chat.infrastructure.loan.InProcessChatLoanAdapter;
import io.dkakunsi.bitapp.chat.infrastructure.mongo.repository.MongoDraftRepository;
import io.dkakunsi.bitapp.chat.infrastructure.transaction.InProcessChatTransactionAdapter;
import io.dkakunsi.bitapp.javalin.JavalinServer;
import io.dkakunsi.bitapp.jwt.JWKAuthorizer;
import io.dkakunsi.bitapp.jwt.JWTAuthorizer;
import io.dkakunsi.bitapp.langchain.GeminiLangChainModel;
import io.dkakunsi.bitapp.loan.application.usecase.CreateLoan;
import io.dkakunsi.bitapp.loan.application.usecase.GetLoan;
import io.dkakunsi.bitapp.loan.application.usecase.GetUserLoans;
import io.dkakunsi.bitapp.loan.application.usecase.RemoveLoan;
import io.dkakunsi.bitapp.loan.application.usecase.RemoveLoanByAccount;
import io.dkakunsi.bitapp.loan.application.usecase.UpdateLoan;
import io.dkakunsi.bitapp.loan.application.usecase.UpdateRemainingAmount;
import io.dkakunsi.bitapp.loan.infrastructure.account.InProcessLoanAccountAdapter;
import io.dkakunsi.bitapp.loan.infrastructure.javalin.CreateLoanEndpoint;
import io.dkakunsi.bitapp.loan.infrastructure.javalin.GetLoanEndpoint;
import io.dkakunsi.bitapp.loan.infrastructure.javalin.GetUserLoansEndpoint;
import io.dkakunsi.bitapp.loan.infrastructure.javalin.RemoveLoanEndpoint;
import io.dkakunsi.bitapp.loan.infrastructure.javalin.UpdateLoanEndpoint;
import io.dkakunsi.bitapp.loan.infrastructure.mongo.repository.MongoLoanRepository;
import io.dkakunsi.bitapp.loan.infrastructure.transaction.InProcessLoanTransactionAdapter;
import io.dkakunsi.bitapp.mongo.MongoConfiguration;
import io.dkakunsi.bitapp.transaction.application.usecase.CreateTransaction;
import io.dkakunsi.bitapp.transaction.application.usecase.GetAccountTransactions;
import io.dkakunsi.bitapp.transaction.application.usecase.GetLoanTransactions;
import io.dkakunsi.bitapp.transaction.application.usecase.GetTransaction;
import io.dkakunsi.bitapp.transaction.application.usecase.GetUserTransactions;
import io.dkakunsi.bitapp.transaction.application.usecase.ProcessTransactionByAccountRemoval;
import io.dkakunsi.bitapp.transaction.application.usecase.ProcessTransactionByLoanRemoval;
import io.dkakunsi.bitapp.transaction.application.usecase.RemoveTransaction;
import io.dkakunsi.bitapp.transaction.infrastructure.account.InProcessTransactionAccountAdapter;
import io.dkakunsi.bitapp.transaction.infrastructure.javalin.CreateTransactionEndpoint;
import io.dkakunsi.bitapp.transaction.infrastructure.javalin.GetAccountTransactionsEndpoint;
import io.dkakunsi.bitapp.transaction.infrastructure.javalin.GetLoanTransactionsEndpoint;
import io.dkakunsi.bitapp.transaction.infrastructure.javalin.GetTransactionEndpoint;
import io.dkakunsi.bitapp.transaction.infrastructure.javalin.GetUserTransactionsEndpoint;
import io.dkakunsi.bitapp.transaction.infrastructure.javalin.RemoveTransactionEndpoint;
import io.dkakunsi.bitapp.transaction.infrastructure.loan.InProcessTransactionLoanAdapter;
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
    var languageModel = GeminiLangChainModel.create(configuration);

    // Repositories
    var datastore = mongoConfig.getDatastore();
    var userRepository = new MongoUserRepository(datastore);
    var accountRepository = new MongoAccountRepository(datastore);
    var loanRepository = new MongoLoanRepository(datastore);
    var transactionRepository = new MongoTransactionRepository(datastore);
    var draftRepository = new MongoDraftRepository(datastore);

    var languageModelRepository = new LanguageModelRepositoryImpl(languageModel);

    // UseCases and Adapters
    var getAccount = new GetAccount(accountRepository);
    var updateBalance = new UpdateBalance(accountRepository);
    var transactionAccountPort = new InProcessTransactionAccountAdapter(updateBalance, getAccount);

    var updateRemainingAmount = new UpdateRemainingAmount(loanRepository);
    var getLoan = new GetLoan(loanRepository);
    var transactionLoanPort = new InProcessTransactionLoanAdapter(updateRemainingAmount, getLoan);

    var createTransaction = new CreateTransaction(transactionRepository, transactionAccountPort,
        transactionLoanPort,
        sessionManager);

    var processTransactionByLoanRemoval = new ProcessTransactionByLoanRemoval(transactionRepository);
    var loanTransactionAdapter = new InProcessLoanTransactionAdapter(processTransactionByLoanRemoval,
        createTransaction);
    var removeLoanByAccount = new RemoveLoanByAccount(loanRepository, loanTransactionAdapter);
    var accountLoanAdapter = new InProcessAccountLoanAdapter(removeLoanByAccount, loanRepository);

    var processTransactionByAccountRemoval = new ProcessTransactionByAccountRemoval(transactionRepository);
    var accountTransactionAdapter = new InProcessAccountTransactionAdapter(
        processTransactionByAccountRemoval);

    var removeTransaction = new RemoveTransaction(transactionRepository, transactionAccountPort,
        transactionLoanPort,
        sessionManager);

    var loanAccountPort = new InProcessLoanAccountAdapter(accountRepository);
    var createLoan = new CreateLoan(sessionManager, loanRepository, loanAccountPort, loanTransactionAdapter);

    var removeAccount = new RemoveAccount(accountRepository, accountTransactionAdapter, accountLoanAdapter,
        sessionManager);

    var registerUser = new RegisterUser(userRepository);
    var getUser = new GetUser(userRepository);
    var updateUser = new UpdateUser(userRepository);
    var createAccount = new CreateAccount(accountRepository);
    var getUserAccounts = new GetUserAccounts(accountRepository);
    var updateAccount = new UpdateAccount(accountRepository);
    var getUserLoans = new GetUserLoans(loanRepository);
    var updateLoan = new UpdateLoan(loanRepository);
    var removeLoan = new RemoveLoan(loanRepository, loanTransactionAdapter, sessionManager);
    var getTransaction = new GetTransaction(transactionRepository);
    var getUserTransactions = new GetUserTransactions(transactionRepository);
    var getAccountTransactions = new GetAccountTransactions(transactionRepository);
    var getLoanTransactions = new GetLoanTransactions(transactionRepository);

    var chatAccountPort = new InProcessChatAccountAdapter(accountRepository, createAccount);
    var chatLoanPort = new InProcessChatLoanAdapter(loanRepository, createLoan);
    var chatTransactionPort = new InProcessChatTransactionAdapter(createTransaction);
    var promptMessageBuilders = Map.of(
        Chat.Type.TRANSACTION, new TransactionPromptMessageBuilder(chatAccountPort, chatLoanPort),
        Chat.Type.LOAN, new LoanPromptMessageBuilder(chatAccountPort),
        Chat.Type.ACCOUNT, new AccountPromptMessageBuilder());

    var chatUseCase = new CreateDraft(languageModelRepository, draftRepository, promptMessageBuilders);
    var confirmChatUseCase = new ConfirmDraft(draftRepository, chatAccountPort, chatLoanPort,
        chatTransactionPort);

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
    var chatEndpoint = new CreateDraftEndpoint(chatUseCase)
        .setAuthorizer(authorizer);
    var confirmChatEndpoint = new ConfirmDraftEndpoint(confirmChatUseCase)
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
        .addEndpoint(chatEndpoint)
        .addEndpoint(confirmChatEndpoint)
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
