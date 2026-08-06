part of 'main.dart';

Future<void> registerIocContainer() async {
  var database = await getDatabase('bitapp.db');
  final configurationStore = ConfigurationStore(database);
  final accountStore = AccountStore(database);
  final loanStore = LoanStore(database);
  final transactionStore = TransactionStore(database);
  final userStore = UserStore(database);

  final googleAuthApi = GoogleAuthenticationApi(
    serverClientId: DefaultFirebaseOptions.androidServerClientId,
  );
  await googleAuthApi.initialize();
  final userApi = UserApi(configurationStore: configurationStore);
  final accountApi = AccountApi(configurationStore: configurationStore);
  final draftApi = DraftApi(configurationStore: configurationStore);
  final loanApi = LoanApi(configurationStore: configurationStore);
  final transactionApi = TransactionApi(configurationStore: configurationStore);

  final userRepository = UserRepository(
    userApi: userApi,
    userStore: userStore,
    configurationStore: configurationStore,
  );
  final accountRepository = AccountRepository(
    accountApi: accountApi,
    accountStore: accountStore,
    configurationStore: configurationStore,
  );
  final loanRepository = LoanRepository(
    loanApi: loanApi,
    loanStore: loanStore,
    configurationStore: configurationStore,
  );

  final localTransactionService = LocalTransactionService(
    transactionStore: transactionStore,
    accountStore: accountStore,
    loanStore: loanStore,
  );
  final localLoanService = LocalLoanService(loanStore);

  addInstance<ConfigurationStore>(configurationStore);

  addInstance<ConfigurationUseCase>(ConfigurationUseCase(configurationStore));
  addInstance<UserUseCase>(UserUseCase(userRepository));
  addInstance<AuthenticationUseCase>(
    AuthenticationUseCase(authenticationApi: googleAuthApi),
  );
  addInstance<AccountUseCase>(
    AccountUseCase(
      accountRepository: accountRepository,
      transactionStore: transactionStore,
    ),
  );
  addInstance<TransactionUseCase>(
    TransactionUseCase(
      transactionApi: transactionApi,
      transactionStore: transactionStore,
      configurationStore: configurationStore,
      localTransactionService: localTransactionService,
    ),
  );
  addInstance<DraftUseCase>(DraftUseCase(draftApi: draftApi));
  addInstance<LoanUseCase>(
    LoanUseCase(
      configurationStore: configurationStore,
      localLoanService: localLoanService,
      loanRepository: loanRepository,
    ),
  );
  addInstance<SummaryUseCase>(
    SummaryUseCase(
      accountStore: accountStore,
      loanStore: loanStore,
      transactionStore: transactionStore,
    ),
  );
  addInstance<TransactionAnalyticUseCase>(
    TransactionAnalyticUseCase(
      transactionStore: transactionStore,
      localTransactionService: localTransactionService,
    ),
  );
}
