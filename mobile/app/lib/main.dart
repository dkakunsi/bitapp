import 'package:bitapp/common/common.dart';
import 'package:bitapp/firebase_options.dart';
import 'package:bitapp/l10n/app_localizations.dart';
import 'package:bitapp/l10n/localization_extension.dart';
import 'package:bitapp/money/bloc/account/account_bloc.dart';
import 'package:bitapp/money/bloc/loan/loan_bloc.dart';
import 'package:bitapp/money/bloc/money_tab/money_tab_bloc.dart';
import 'package:bitapp/money/bloc/summary/summary_bloc.dart';
import 'package:bitapp/money/bloc/transaction/transaction_bloc.dart';
import 'package:bitapp/money/bloc/transaction_analytics/transaction_analytics_bloc.dart';
import 'package:bitapp/money/data/api/account_api.dart';
import 'package:bitapp/money/data/api/loan_api.dart';
import 'package:bitapp/money/data/api/transaction_api.dart';
import 'package:bitapp/money/data/store/account_store.dart';
import 'package:bitapp/money/data/store/loan_store.dart';
import 'package:bitapp/money/data/store/transaction_store.dart';
import 'package:bitapp/money/presentation/screen/account_screen.dart';
import 'package:bitapp/money/presentation/screen/loan_screen.dart';
import 'package:bitapp/money/presentation/screen/money_screen.dart';
import 'package:bitapp/money/presentation/screen/transaction_analytics_screen.dart';
import 'package:bitapp/money/usecase/account_usecase.dart';
import 'package:bitapp/money/usecase/loan_usecase.dart';
import 'package:bitapp/money/usecase/summary_usecase.dart';
import 'package:bitapp/money/usecase/transaction_analytic_usecase.dart';
import 'package:bitapp/money/usecase/transaction_usecase.dart';
import 'package:bitapp/money/util/constant.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/date_symbol_data_local.dart';
import 'package:logging/logging.dart';
import 'package:package_info_plus/package_info_plus.dart';

part 'main.container.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp(options: DefaultFirebaseOptions.currentPlatform);
  await registerIocContainer();
  initializeDateFormatting();
  WidgetsFlutterBinding.ensureInitialized();
  final packageInfo = await PackageInfo.fromPlatform();

  LogConfig.configure(Level.INFO, ConsoleWriter());

  runApp(BitApp(packageInfo: packageInfo));
}

class BitApp extends StatelessWidget {
  final PackageInfo packageInfo;

  BitApp({super.key, required this.packageInfo});

  @override
  Widget build(BuildContext context) {
    return MultiBlocProvider(
      providers: [
        BlocProvider(
          create:
              (context) =>
                  ConfigurationBloc(getInstance<ConfigurationUseCase>())..add(
                    SetAppSettings(
                      baseUrl: 'http://192.168.1.10:8081',
                      // enable for paid customers
                      remoteEnabled: false,
                      startColor: Colors.white,
                      endColor: Colors.cyanAccent,
                      appLogoUrl: 'assets/images/bitapp_logo.png',
                      appName: 'BitApp',
                      appMotto: 'Convenient in Every Bit',
                      appVersion: packageInfo.version,
                      buildNumber: packageInfo.buildNumber,
                      contact: 'contact@cortech.com',
                      developerName: 'Cortech',
                    ),
                  ),
        ),
        BlocProvider(
          create:
              (context) => UserBloc(
                getInstance<UserUseCase>(),
                context.read<ConfigurationBloc>(),
              ),
        ),
        BlocProvider(
          create:
              (context) => AuthenticationBloc(
                getInstance<AuthenticationUseCase>(),
                context.read<ConfigurationBloc>(),
                context.read<UserBloc>(),
              ),
        ),
        BlocProvider(
          create: (context) => AccountBloc(getInstance<AccountUseCase>()),
        ),
        BlocProvider(create: (context) => LoanBloc(getInstance<LoanUseCase>())),
        BlocProvider(
          create:
              (context) => TransactionBloc(
                getInstance<TransactionUseCase>(),
                getInstance<ConfigurationUseCase>(),
                context.read<AccountBloc>(),
                context.read<LoanBloc>(),
              ),
        ),
        BlocProvider(
          create: (context) => SummaryBloc(getInstance<SummaryUseCase>()),
        ),
        BlocProvider(create: (context) => MoneyTabBloc()),
        BlocProvider(
          create:
              (context) => TransactionAnalyticsBloc(
                getInstance<TransactionAnalyticUseCase>(),
              ),
        ),
      ],
      child: MaterialApp.router(
        debugShowCheckedModeBanner: false,
        title: 'BitApp',
        localizationsDelegates: AppLocalizations.localizationsDelegates,
        supportedLocales: AppLocalizations.supportedLocales,
        theme: ThemeData(primarySwatch: Colors.blue),
        routerConfig: _router,
      ),
    );
  }

  final GoRouter _router = GoRouter(
    routes: [
      GoRoute(
        name: InitialScreen.routeName,
        path: InitialScreen.routeName,
        builder: (context, state) {
          final nextRoute = state.extra as String?;
          return InitialScreen(
            mainRoute: nextRoute ?? MoneyScreen.routeName,
            authRoute: AuthenticationScreen.routeName,
          );
        },
      ),
      GoRoute(
        name: AuthenticationScreen.routeName,
        path: AuthenticationScreen.routeName,
        builder:
            (context, state) => AuthenticationScreen(
              appRouteName: MoneyScreen.routeName,
              signInWithGoogleLabel: context.locale.signInWithGoogle,
              authenticationFailedMessage: context.locale.authenticationError,
            ),
      ),
      GoRoute(
        name: MoneyScreen.routeName,
        path: MoneyScreen.routeName,
        builder: (context, state) {
          context.read<SummaryBloc>().add(
            CalculateSummary(userId: context.userId),
          );
          context.read<MoneyTabBloc>().add(
            SelectMoneyTab(MoneyTab.transaction.name),
          );
          context.read<TransactionBloc>().add(
            GetTransactions(userId: context.userId),
          );
          return MoneyScreen(
            title: context.locale.money,
            listener:
                (context, child) => accountListener(
                  context,
                  loanListener(context, transactionListener(context, child)),
                ),
          );
        },
      ),
      GoRoute(
        name: UserScreen.routeName,
        path: UserScreen.routeName,
        builder: (context, state) {
          return UserScreen(
            modules: [
              ModuleConfig(
                title: context.locale.moneyManagement,
                routeName: MoneyScreen.routeName,
              ),
            ],
            // modules: {Label.moneyManagement.key: MoneyScreen.routeName},
            moduleLabel: context.locale.profile,
            yourAppsLabel: context.locale.yourApps,
            yourSettingsLabel: context.locale.yourSettings,
            logoutLabel: context.locale.logout,
            languageLabel: context.locale.language,
            appInfoLabel: context.locale.appInfo,
            contactUsLabel: context.locale.contactUs,
            developedByLabel: context.locale.developedBy,
            appVersionLabel: context.locale.appVersion,
            availableLanguages: {
              Language.en: context.locale.english,
              Language.id: context.locale.indonesian,
            },
            synchronizeLabel: context.locale.synchronize,
            onSynchronize: (context) {
              context.nextRoute(MoneyScreen.routeName);
              context.read<AccountBloc>().add(
                FetchAccounts(userId: context.userId),
              );
              context.read<LoanBloc>().add(FetchLoans(userId: context.userId));
              context.read<TransactionBloc>().add(
                FetchTransactions(userId: context.userId),
              );
              context.read<SummaryBloc>().add(
                CalculateSummary(userId: context.userId),
              );
            },
          );
        },
      ),
      GoRoute(
        name: AccountScreen.routeName,
        path: AccountScreen.routeName,
        builder: (context, state) {
          final accountId = state.extra! as String;
          context.read<AccountBloc>().add(GetAccount(id: accountId));
          context.read<TransactionBloc>().add(
            GetAccountTransactions(accountId: accountId),
          );
          return AccountScreen(
            accountId: accountId,
            title: context.locale.account,
            listener:
                (context, child) => accountListener(
                  context,
                  transactionListener(context, child),
                ),
          );
        },
      ),
      GoRoute(
        name: LoanScreen.routeName,
        path: LoanScreen.routeName,
        builder: (context, state) {
          final loanId = state.extra! as String;
          context.read<LoanBloc>().add(GetLoan(id: loanId));
          context.read<TransactionBloc>().add(
            GetLoanTransactions(loanId: loanId),
          );
          return LoanScreen(
            loanId: loanId,
            title: context.locale.loan,
            listener:
                (context, child) =>
                    loanListener(context, transactionListener(context, child)),
          );
        },
      ),
      GoRoute(
        name: TransactionAnalyticsScreen.routeName,
        path: TransactionAnalyticsScreen.routeName,
        builder: (context, state) {
          context.read<TransactionAnalyticsBloc>().add(
            AnalyzeTransactions(userId: context.userId, date: DateTime.now()),
          );
          return TransactionAnalyticsScreen(title: context.locale.money);
        },
      ),
    ],
  );
}

BlocListener accountListener(BuildContext context, Widget child) {
  return BlocListener<AccountBloc, AccountState>(
    listener: (context, state) {
      if (state is AccountAdded) {
        context.successMessage(context.locale.accountAdded);
      } else if (state is AccountUpdated) {
        context.successMessage(context.locale.accountUpdated);
      } else if (state is AccountDeleted) {
        context.successMessage(context.locale.accountDeleted);
      } else if (state is AccountsFetchingFailed) {
        context.errorMessage(context.locale.accountFetchingError);
      } else if (state is AccountsRetrievalFailed) {
        context.errorMessage(context.locale.accountRetrievalError);
      } else if (state is AccountRetrievalFailed) {
        context.errorMessage(context.locale.accountRetrievalError);
      } else if (state is AccountAdditionFailed) {
        context.errorMessage(context.locale.accountAdditionError);
      } else if (state is AccountUpdateFailed) {
        context.errorMessage(context.locale.accountUpdatingError);
      } else if (state is AccountDeletionFailed) {
        context.errorMessage(context.locale.accountDeletionError);
      }
    },
    child: child,
  );
}

BlocListener loanListener(BuildContext context, Widget child) {
  return BlocListener<LoanBloc, LoanState>(
    listener: (context, state) {
      if (state is LoanAdded) {
        context.successMessage(context.locale.loanAdded);
      } else if (state is LoanUpdated) {
        context.successMessage(context.locale.loanUpdated);
      } else if (state is LoanDeleted) {
        context.successMessage(context.locale.loanDeleted);
      } else if (state is LoansFetchingFailed) {
        context.errorMessage(context.locale.loanFetchingError);
      } else if (state is LoansRetrievalFailed) {
        context.errorMessage(context.locale.loanRetrievalError);
      } else if (state is LoanRetrievalFailed) {
        context.errorMessage(context.locale.loanRetrievalError);
      } else if (state is LoanAdditionFailed) {
        context.errorMessage(context.locale.loanAdditionError);
      } else if (state is LoanUpdateFailed) {
        context.errorMessage(context.locale.loanUpdatingError);
      } else if (state is LoanDeletionFailed) {
        context.errorMessage(context.locale.loanDeletionError);
      }
    },
    child: child,
  );
}

BlocListener transactionListener(BuildContext context, Widget child) {
  return BlocListener<TransactionBloc, TransactionState>(
    listener: (context, state) {
      if (state is TransactionAdded) {
        context.successMessage(context.locale.transactionAdded);
      } else if (state is TransactionDeleted) {
        context.successMessage(context.locale.transactionDeleted);
      } else if (state is TransactionsFetchingFailed) {
        context.errorMessage(context.locale.transactionFetchingError);
      } else if (state is TransactionsRetrievalFailed) {
        context.errorMessage(context.locale.transactionRetrievalError);
      } else if (state is TransactionsFetchingFailed) {
        context.errorMessage(context.locale.transactionFetchingError);
      } else if (state is TransactionsRetrievalFailed) {
        context.errorMessage(context.locale.transactionRetrievalError);
      } else if (state is TransactionAdditionFailed) {
        context.errorMessage(context.locale.transactionAdditionError);
      } else if (state is TransactionDeletionFailed) {
        context.errorMessage(context.locale.transactionDeletionError);
      }
    },
    child: child,
  );
}
