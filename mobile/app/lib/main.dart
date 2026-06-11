import 'package:bitapp/common/util/language.dart';
import 'package:bitapp/common/util/container.dart';
import 'package:bitapp/common/util/database.dart';
import 'package:bitapp/common/util/logger.dart';
import 'package:bitapp/features/account/data/account_repository.dart';
import 'package:bitapp/features/account/domain/account_usecase.dart';
import 'package:bitapp/features/app/extension/navigation_extension.dart';
import 'package:bitapp/features/app/presentation/screen/initial_screen.dart';
import 'package:bitapp/features/app/presentation/widget/module_list.dart';
import 'package:bitapp/features/authentication/data/google_authentication_api.dart';
import 'package:bitapp/features/authentication/domain/authentication_usecase.dart';
import 'package:bitapp/features/authentication/extension/session_extension.dart';
import 'package:bitapp/features/authentication/presentation/bloc/authentication_bloc.dart';
import 'package:bitapp/features/authentication/presentation/screen/authentication_screen.dart';
import 'package:bitapp/features/configuration/data/configuration_store.dart';
import 'package:bitapp/features/configuration/domain/configuration_usecase.dart';
import 'package:bitapp/features/configuration/presentation/bloc/configuration_bloc.dart';
import 'package:bitapp/features/loan/data/loan_repository.dart';
import 'package:bitapp/features/loan/domain/local_loan_service.dart';
import 'package:bitapp/features/transaction/domain/local_tansaction_service.dart';

import 'package:bitapp/features/user/data/user_repository.dart';
import 'package:bitapp/features/user/data/user_api.dart';
import 'package:bitapp/features/user/data/user_store.dart';
import 'package:bitapp/features/user/domain/user_usecase.dart';
import 'package:bitapp/features/user/presentation/bloc/user_bloc.dart';
import 'package:bitapp/features/user/presentation/screen/user_screen.dart';
import 'package:bitapp/firebase_options.dart';
import 'package:bitapp/l10n/app_localizations.dart';
import 'package:bitapp/l10n/localization_extension.dart';
import 'package:bitapp/features/account/presentation/bloc/account_bloc.dart';
import 'package:bitapp/features/loan/presentation/bloc/loan_bloc.dart';
import 'package:bitapp/features/app/presentation/bloc/money_tab_bloc.dart';
import 'package:bitapp/features/summary/presentation/bloc/summary_bloc.dart';
import 'package:bitapp/features/transaction/presentation/bloc/transaction_bloc.dart';
import 'package:bitapp/features/transaction_analytic/presentation/bloc/transaction_analytics_bloc.dart';
import 'package:bitapp/features/account/data/account_api.dart';
import 'package:bitapp/features/loan/data/loan_api.dart';
import 'package:bitapp/features/transaction/data/transaction_api.dart';
import 'package:bitapp/features/account/data/account_store.dart';
import 'package:bitapp/features/loan/data/loan_store.dart';
import 'package:bitapp/features/transaction/data/transaction_store.dart';
import 'package:bitapp/features/account/presentation/screen/account_screen.dart';
import 'package:bitapp/features/loan/presentation/screen/loan_screen.dart';
import 'package:bitapp/features/app/presentation/screen/money_screen.dart';
import 'package:bitapp/features/transaction_analytic/presentation/screen/transaction_analytics_screen.dart';
import 'package:bitapp/features/loan/domain/loan_usecase.dart';
import 'package:bitapp/features/summary/domain/usecase/summary_usecase.dart';
import 'package:bitapp/features/transaction_analytic/domain/usecase/transaction_analytic_usecase.dart';
import 'package:bitapp/features/transaction/domain/transaction_usecase.dart';
import 'package:bitapp/features/app/constant.dart';
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

  if (Firebase.apps.isEmpty) {
    await Firebase.initializeApp(
      options: DefaultFirebaseOptions.currentPlatform,
    );
  }

  await registerIocContainer();
  initializeDateFormatting();
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
                      baseUrl:
                          'https://bitapp-159272058870.asia-southeast2.run.app',
                      // enable for paid customers
                      remoteEnabled: true,
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
                FetchAccounts(user: context.user!),
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
