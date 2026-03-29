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
  final loanApi = LoanApi(configurationStore: configurationStore);
  final transactionApi = TransactionApi(configurationStore: configurationStore);

  final localTransactionService = LocalTansactionService(
    transactionStore,
    accountStore,
    loanStore,
  );
  final localLoanService = LocalLoanService(loanStore);

  addInstance<ConfigurationStore>(configurationStore);

  addInstance<ConfigurationUseCase>(ConfigurationUseCase(configurationStore));
  addInstance<UserUseCase>(UserUseCase(userApi, userStore, configurationStore));
  addInstance<AuthenticationUseCase>(
    AuthenticationUseCase(authenticationApi: googleAuthApi),
  );
  addInstance<AccountUseCase>(
    AccountUseCase(
      accountApi,
      accountStore,
      transactionStore,
      configurationStore,
    ),
  );
  addInstance<TransactionUseCase>(
    TransactionUseCase(
      transactionApi,
      transactionStore,
      configurationStore,
      localTransactionService,
    ),
  );
  addInstance<LoanUseCase>(
    LoanUseCase(loanApi, loanStore, configurationStore, localLoanService),
  );
  addInstance<SummaryUseCase>(
    SummaryUseCase(accountStore, loanStore, transactionStore),
  );
  addInstance<TransactionAnalyticUseCase>(
    TransactionAnalyticUseCase(transactionStore),
  );
}
