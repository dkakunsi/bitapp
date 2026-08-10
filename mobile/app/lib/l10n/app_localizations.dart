import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:intl/intl.dart' as intl;

import 'app_localizations_en.dart';
import 'app_localizations_id.dart';

// ignore_for_file: type=lint

/// Callers can lookup localized strings with an instance of AppLocalizations
/// returned by `AppLocalizations.of(context)`.
///
/// Applications need to include `AppLocalizations.delegate()` in their app's
/// `localizationDelegates` list, and the locales they support in the app's
/// `supportedLocales` list. For example:
///
/// ```dart
/// import 'l10n/app_localizations.dart';
///
/// return MaterialApp(
///   localizationsDelegates: AppLocalizations.localizationsDelegates,
///   supportedLocales: AppLocalizations.supportedLocales,
///   home: MyApplicationHome(),
/// );
/// ```
///
/// ## Update pubspec.yaml
///
/// Please make sure to update your pubspec.yaml to include the following
/// packages:
///
/// ```yaml
/// dependencies:
///   # Internationalization support.
///   flutter_localizations:
///     sdk: flutter
///   intl: any # Use the pinned version from flutter_localizations
///
///   # Rest of dependencies
/// ```
///
/// ## iOS Applications
///
/// iOS applications define key application metadata, including supported
/// locales, in an Info.plist file that is built into the application bundle.
/// To configure the locales supported by your app, you’ll need to edit this
/// file.
///
/// First, open your project’s ios/Runner.xcworkspace Xcode workspace file.
/// Then, in the Project Navigator, open the Info.plist file under the Runner
/// project’s Runner folder.
///
/// Next, select the Information Property List item, select Add Item from the
/// Editor menu, then select Localizations from the pop-up menu.
///
/// Select and expand the newly-created Localizations item then, for each
/// locale your application supports, add a new item and select the locale
/// you wish to add from the pop-up menu in the Value field. This list should
/// be consistent with the languages listed in the AppLocalizations.supportedLocales
/// property.
abstract class AppLocalizations {
  AppLocalizations(String locale)
    : localeName = intl.Intl.canonicalizedLocale(locale.toString());

  final String localeName;

  static AppLocalizations? of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations);
  }

  static const LocalizationsDelegate<AppLocalizations> delegate =
      _AppLocalizationsDelegate();

  /// A list of this localizations delegate along with the default localizations
  /// delegates.
  ///
  /// Returns a list of localizations delegates containing this delegate along with
  /// GlobalMaterialLocalizations.delegate, GlobalCupertinoLocalizations.delegate,
  /// and GlobalWidgetsLocalizations.delegate.
  ///
  /// Additional delegates can be added by appending to this list in
  /// MaterialApp. This list does not have to be used at all if a custom list
  /// of delegates is preferred or required.
  static const List<LocalizationsDelegate<dynamic>> localizationsDelegates =
      <LocalizationsDelegate<dynamic>>[
        delegate,
        GlobalMaterialLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
      ];

  /// A list of this localizations delegate's supported locales.
  static const List<Locale> supportedLocales = <Locale>[
    Locale('en'),
    Locale('id'),
  ];

  /// Label for account
  ///
  /// In en, this message translates to:
  /// **'Account'**
  String get account;

  /// Message when an account is successfully added
  ///
  /// In en, this message translates to:
  /// **'Account is added'**
  String get accountAdded;

  /// Error message when creating an account fails
  ///
  /// In en, this message translates to:
  /// **'Error creating account'**
  String get accountAdditionError;

  /// Message when an account is successfully deleted
  ///
  /// In en, this message translates to:
  /// **'Account is deleted'**
  String get accountDeleted;

  /// Error message when deleting an account fails
  ///
  /// In en, this message translates to:
  /// **'Error deleting account'**
  String get accountDeletionError;

  /// Error message when fetching accounts fails
  ///
  /// In en, this message translates to:
  /// **'Error fetching accounts'**
  String get accountFetchingError;

  /// Label for account name
  ///
  /// In en, this message translates to:
  /// **'Account name'**
  String get accountName;

  /// Error message when loading accounts fails
  ///
  /// In en, this message translates to:
  /// **'Error loading accounts'**
  String get accountRetrievalError;

  /// Label for account type
  ///
  /// In en, this message translates to:
  /// **'Account type'**
  String get accountType;

  /// Message when an account is successfully updated
  ///
  /// In en, this message translates to:
  /// **'Account is updated'**
  String get accountUpdated;

  /// Error message when updating an account fails
  ///
  /// In en, this message translates to:
  /// **'Error updating account'**
  String get accountUpdatingError;

  /// Error message when account details are not provided
  ///
  /// In en, this message translates to:
  /// **'Cannot save account'**
  String get accountsNotProvided;

  /// Error message when source and destination accounts are the same
  ///
  /// In en, this message translates to:
  /// **'Source and destination accounts should be different'**
  String get accountsShouldBeDifferent;

  /// Label for accumulation
  ///
  /// In en, this message translates to:
  /// **'Accumulation'**
  String get accumulation;

  /// Add button label
  ///
  /// In en, this message translates to:
  /// **'Add'**
  String get add;

  /// Label for adding income
  ///
  /// In en, this message translates to:
  /// **'Add Income'**
  String get addCredit;

  /// Label for adding incoming transfer
  ///
  /// In en, this message translates to:
  /// **'Add Incoming Transfer'**
  String get addCreditTransfer;

  /// Label for adding expense
  ///
  /// In en, this message translates to:
  /// **'Add Expense'**
  String get addDebit;

  /// Label for adding outgoing transfer
  ///
  /// In en, this message translates to:
  /// **'Add Outgoing Transfer'**
  String get addDebitTransfer;

  /// Label for adding debt
  ///
  /// In en, this message translates to:
  /// **'Add Debt'**
  String get addDebt;

  /// Label for adding receivable
  ///
  /// In en, this message translates to:
  /// **'Add Receivable'**
  String get addReceivable;

  /// Label for adding transaction
  ///
  /// In en, this message translates to:
  /// **'Add Transaction'**
  String get addTransaction;

  /// Label for adding transfer
  ///
  /// In en, this message translates to:
  /// **'Add Transfer'**
  String get addTransfer;

  /// Error message when the amount is zero or empty
  ///
  /// In en, this message translates to:
  /// **'Amount should not be zero or empty'**
  String get amountShouldNotBeZeroOrEmpty;

  /// Label for analytics
  ///
  /// In en, this message translates to:
  /// **'Analytics'**
  String get analytics;

  /// Label for app information
  ///
  /// In en, this message translates to:
  /// **'App Information'**
  String get appInfo;

  /// App motto for English locale
  ///
  /// In en, this message translates to:
  /// **'Convenient in every bit'**
  String get appMotto;

  /// App name for English locale
  ///
  /// In en, this message translates to:
  /// **'BitApp'**
  String get appName;

  /// Label for app version
  ///
  /// In en, this message translates to:
  /// **'Version'**
  String get appVersion;

  /// Error message when authentication fails
  ///
  /// In en, this message translates to:
  /// **'Error on authentication'**
  String get authenticationError;

  /// Label for bill
  ///
  /// In en, this message translates to:
  /// **'Bill'**
  String get bill;

  /// Label for bills
  ///
  /// In en, this message translates to:
  /// **'Bills'**
  String get bills;

  /// Label for bonus
  ///
  /// In en, this message translates to:
  /// **'Bonus'**
  String get bonus;

  /// Cancel button label
  ///
  /// In en, this message translates to:
  /// **'Cancel'**
  String get cancel;

  /// Label for charity
  ///
  /// In en, this message translates to:
  /// **'Charity'**
  String get charity;

  /// Error message when there is an issue accessing the configuration
  ///
  /// In en, this message translates to:
  /// **'Configuration access error'**
  String get configurationAccessError;

  /// Error message when configuration update fails
  ///
  /// In en, this message translates to:
  /// **'Configuration update error'**
  String get configurationUpdateError;

  /// Error message when configuration update fails
  ///
  /// In en, this message translates to:
  /// **'Configuration update error'**
  String get configuarationUpdateError;

  /// Label for contact us
  ///
  /// In en, this message translates to:
  /// **'Contact us'**
  String get contactUs;

  /// Label for credit
  ///
  /// In en, this message translates to:
  /// **'Credit'**
  String get credit;

  /// Error message when trying to relate receivable to a non-debit transaction
  ///
  /// In en, this message translates to:
  /// **'Can only relate receivable to debit transaction'**
  String get creditShouldRelateToReceivableLoan;

  /// Error message when data synchronization fails
  ///
  /// In en, this message translates to:
  /// **'Data synchronization failure'**
  String get dataSynchronizationFailure;

  /// Label for debit
  ///
  /// In en, this message translates to:
  /// **'Debit'**
  String get debit;

  /// Error message when trying to relate debt to a non-credit transaction
  ///
  /// In en, this message translates to:
  /// **'Can only relate debt to credit transaction'**
  String get debitShouldRelateToDebtLoan;

  /// Label for debt
  ///
  /// In en, this message translates to:
  /// **'Debt'**
  String get debt;

  /// Delete button label
  ///
  /// In en, this message translates to:
  /// **'Delete'**
  String get delete;

  /// Detail button label
  ///
  /// In en, this message translates to:
  /// **'Detail'**
  String get detail;

  /// Error message when required details are not filled
  ///
  /// In en, this message translates to:
  /// **'Details are not filled'**
  String get detailsAreNotFilled;

  /// Error message when the destination account is not provided
  ///
  /// In en, this message translates to:
  /// **'Destination account is not provided'**
  String get destinationAccountNotProvided;

  /// Label for developer information
  ///
  /// In en, this message translates to:
  /// **'Developed by'**
  String get developedBy;

  /// Label for education
  ///
  /// In en, this message translates to:
  /// **'Education'**
  String get education;

  /// English language label
  ///
  /// In en, this message translates to:
  /// **'English'**
  String get english;

  /// Label for entertainment
  ///
  /// In en, this message translates to:
  /// **'Entertainment'**
  String get entertainment;

  /// Label for expense
  ///
  /// In en, this message translates to:
  /// **'Expense'**
  String get expense;

  /// Export button label
  ///
  /// In en, this message translates to:
  /// **'Export'**
  String get export;

  /// Message when export functionality is not available
  ///
  /// In en, this message translates to:
  /// **'Export is not available yet'**
  String get exportNotAvailable;

  /// Label for food
  ///
  /// In en, this message translates to:
  /// **'Food'**
  String get food;

  /// Label for gift
  ///
  /// In en, this message translates to:
  /// **'Gift'**
  String get gift;

  /// Label for usage this month
  ///
  /// In en, this message translates to:
  /// **'Has been used this month'**
  String get hasBeenUsedThisMonth;

  /// Label for health
  ///
  /// In en, this message translates to:
  /// **'Health'**
  String get health;

  /// Label for Indonesian Rupiah currency
  ///
  /// In en, this message translates to:
  /// **'IDR'**
  String get idr;

  /// Label for income
  ///
  /// In en, this message translates to:
  /// **'Income'**
  String get income;

  /// Indonesian language label
  ///
  /// In en, this message translates to:
  /// **'Indonesian'**
  String get indonesian;

  /// Label for inputting amount
  ///
  /// In en, this message translates to:
  /// **'Input amount'**
  String get inputAmount;

  /// Label for inputting borrower
  ///
  /// In en, this message translates to:
  /// **'Input borrower'**
  String get inputBorrower;

  /// Label for inputting description
  ///
  /// In en, this message translates to:
  /// **'Input description'**
  String get inputDescription;

  /// Label for inputting lender
  ///
  /// In en, this message translates to:
  /// **'Input lender'**
  String get inputLender;

  /// Label for inputting title
  ///
  /// In en, this message translates to:
  /// **'Input title'**
  String get inputTitle;

  /// Label for interest
  ///
  /// In en, this message translates to:
  /// **'Interest'**
  String get interest;

  /// Label for investment
  ///
  /// In en, this message translates to:
  /// **'Investment'**
  String get investment;

  /// Error message when user provides wrong credentials
  ///
  /// In en, this message translates to:
  /// **'Wrong credentials. Please try again!'**
  String get invalidAuthentication;

  /// Language label
  ///
  /// In en, this message translates to:
  /// **'Language'**
  String get language;

  /// Label for loan
  ///
  /// In en, this message translates to:
  /// **'Loan'**
  String get loan;

  /// Message when a loan is successfully added
  ///
  /// In en, this message translates to:
  /// **'Loan is added'**
  String get loanAdded;

  /// Error message when creating a loan fails
  ///
  /// In en, this message translates to:
  /// **'Error creating loan'**
  String get loanAdditionError;

  /// Message when a loan is successfully deleted
  ///
  /// In en, this message translates to:
  /// **'Loan is deleted'**
  String get loanDeleted;

  /// Error message when deleting a loan fails
  ///
  /// In en, this message translates to:
  /// **'Error deleting loan'**
  String get loanDeletionError;

  /// Error message when fetching loans fails
  ///
  /// In en, this message translates to:
  /// **'Error fetching loans'**
  String get loanFetchingError;

  /// Label for loan payment
  ///
  /// In en, this message translates to:
  /// **'Loan Payment'**
  String get loanPayment;

  /// Error message when loading loans fails
  ///
  /// In en, this message translates to:
  /// **'Error loading loans'**
  String get loanRetrievalError;

  /// Message when a loan is successfully updated
  ///
  /// In en, this message translates to:
  /// **'Loan is updated'**
  String get loanUpdated;

  /// Error message when updating a loan fails
  ///
  /// In en, this message translates to:
  /// **'Error updating loan'**
  String get loanUpdatingError;

  /// Logout button label
  ///
  /// In en, this message translates to:
  /// **'Logout'**
  String get logout;

  /// Error message when logout fails
  ///
  /// In en, this message translates to:
  /// **'Error on logout'**
  String get logoutError;

  /// Label for money
  ///
  /// In en, this message translates to:
  /// **'Money'**
  String get money;

  /// Label for money management
  ///
  /// In en, this message translates to:
  /// **'Money management'**
  String get moneyManagement;

  /// Label for the word 'of'
  ///
  /// In en, this message translates to:
  /// **'of'**
  String get ofLabel;

  /// Label for other
  ///
  /// In en, this message translates to:
  /// **'Other'**
  String get other;

  /// Label for paid status
  ///
  /// In en, this message translates to:
  /// **'Paid'**
  String get paid;

  /// Label for pay button
  ///
  /// In en, this message translates to:
  /// **'Pay'**
  String get pay;

  /// Profile label
  ///
  /// In en, this message translates to:
  /// **'Profile'**
  String get profile;

  /// Label for receivable
  ///
  /// In en, this message translates to:
  /// **'Receivable'**
  String get receivable;

  /// Label for rent
  ///
  /// In en, this message translates to:
  /// **'Rent'**
  String get rent;

  /// Label for salary
  ///
  /// In en, this message translates to:
  /// **'Salary'**
  String get salary;

  /// Save button label
  ///
  /// In en, this message translates to:
  /// **'Save'**
  String get save;

  /// Label for savings
  ///
  /// In en, this message translates to:
  /// **'Savings'**
  String get savings;

  /// Label for selecting color
  ///
  /// In en, this message translates to:
  /// **'Select color'**
  String get selectColor;

  /// Label for selecting date
  ///
  /// In en, this message translates to:
  /// **'Select date'**
  String get selectDate;

  /// Label for selecting debt (optional)
  ///
  /// In en, this message translates to:
  /// **'Select debt (Optional)'**
  String get selectDebt;

  /// Label for selecting destination account
  ///
  /// In en, this message translates to:
  /// **'Select destination account'**
  String get selectDestinationAccount;

  /// Label for selecting receivable (optional)
  ///
  /// In en, this message translates to:
  /// **'Select receivable (Optional)'**
  String get selectReceivable;

  /// Label for selecting source account
  ///
  /// In en, this message translates to:
  /// **'Select source account'**
  String get selectSourceAccount;

  /// Label for selecting time
  ///
  /// In en, this message translates to:
  /// **'Select time'**
  String get selectTime;

  /// Error message when the session is empty
  ///
  /// In en, this message translates to:
  /// **'Session is empty'**
  String get sessionIsEmpty;

  /// Label for shopping
  ///
  /// In en, this message translates to:
  /// **'Shopping'**
  String get shopping;

  /// Sign in with Google button label
  ///
  /// In en, this message translates to:
  /// **'Sign in with Google'**
  String get signInWithGoogle;

  /// Error message when the source account is not provided
  ///
  /// In en, this message translates to:
  /// **'Source account is not provided'**
  String get sourceAccountNotProvided;

  /// Label for subscription
  ///
  /// In en, this message translates to:
  /// **'Subscription'**
  String get subscription;

  /// Error message when calculating the asset summary fails
  ///
  /// In en, this message translates to:
  /// **'Error loading your asset summary'**
  String get summaryCalculationError;

  /// Label for synchronizing data
  ///
  /// In en, this message translates to:
  /// **'Synchronize'**
  String get synchronize;

  /// Label for tax
  ///
  /// In en, this message translates to:
  /// **'Tax'**
  String get tax;

  /// Label for user's accounts list
  ///
  /// In en, this message translates to:
  /// **'This is your accounts'**
  String get thisIsYourAccounts;

  /// Label for user's loans list
  ///
  /// In en, this message translates to:
  /// **'This is your loans'**
  String get thisIsYourLoans;

  /// Label for user's transactions list
  ///
  /// In en, this message translates to:
  /// **'This is your transactions'**
  String get thisIsYourTransactions;

  /// Error message when the token is empty
  ///
  /// In en, this message translates to:
  /// **'Token is empty'**
  String get tokenIsEmpty;

  /// Label for transaction
  ///
  /// In en, this message translates to:
  /// **'Transaction'**
  String get transaction;

  /// Message when a transaction is successfully added
  ///
  /// In en, this message translates to:
  /// **'Transaction is added'**
  String get transactionAdded;

  /// Error message when saving a transaction fails
  ///
  /// In en, this message translates to:
  /// **'Error adding transaction'**
  String get transactionAdditionError;

  /// Label for transaction category
  ///
  /// In en, this message translates to:
  /// **'Transaction category'**
  String get transactionCategory;

  /// Message when a transaction is successfully removed
  ///
  /// In en, this message translates to:
  /// **'Transaction is removed'**
  String get transactionDeleted;

  /// Error message when deleting a transaction fails
  ///
  /// In en, this message translates to:
  /// **'Error removing transaction'**
  String get transactionDeletionError;

  /// Error message when fetching transactions fails
  ///
  /// In en, this message translates to:
  /// **'Error fetching transactions'**
  String get transactionFetchingError;

  /// Error message when loading transactions fails
  ///
  /// In en, this message translates to:
  /// **'Error loading transactions'**
  String get transactionRetrievalError;

  /// Message when a transaction is successfully saved
  ///
  /// In en, this message translates to:
  /// **'Transaction is saved'**
  String get transactionSaved;

  /// Label for transfer
  ///
  /// In en, this message translates to:
  /// **'Transfer'**
  String get transfer;

  /// Label for transport
  ///
  /// In en, this message translates to:
  /// **'Transport'**
  String get transport;

  /// Label for travel
  ///
  /// In en, this message translates to:
  /// **'Travel'**
  String get travel;

  /// Error message when an unknown error occurs
  ///
  /// In en, this message translates to:
  /// **'Unknown error'**
  String get unknownError;

  /// Error message when user creation fails
  ///
  /// In en, this message translates to:
  /// **'Error on creating user'**
  String get userCreationError;

  /// Error message when fetching user fails
  ///
  /// In en, this message translates to:
  /// **'Error on fetching user'**
  String get userFetchError;

  /// Error message when the user ID is not set
  ///
  /// In en, this message translates to:
  /// **'User ID is not set'**
  String get userIdIsNotSet;

  /// Error message when user update fails
  ///
  /// In en, this message translates to:
  /// **'Error on updating user'**
  String get userUpdateError;

  /// Label for utilities
  ///
  /// In en, this message translates to:
  /// **'Utilities'**
  String get utilities;

  /// Label for AI draft feature
  ///
  /// In en, this message translates to:
  /// **'AI Draft'**
  String get aiDraft;

  /// Title for AI draft screen
  ///
  /// In en, this message translates to:
  /// **'Create a draft with AI'**
  String get aiDraftTitle;

  /// Description for AI draft screen
  ///
  /// In en, this message translates to:
  /// **'Describe the account, loan, or transaction you want to create.'**
  String get aiDraftDescription;

  /// Label for draft type
  ///
  /// In en, this message translates to:
  /// **'Draft type'**
  String get aiDraftType;

  /// Label for initial draft request
  ///
  /// In en, this message translates to:
  /// **'Request'**
  String get aiDraftRequest;

  /// Hint for initial draft request
  ///
  /// In en, this message translates to:
  /// **'Type what you want to create'**
  String get aiDraftRequestHint;

  /// Button label to select an image for OCR
  ///
  /// In en, this message translates to:
  /// **'Select image'**
  String get aiDraftSelectImage;

  /// Button label to change an image for OCR
  ///
  /// In en, this message translates to:
  /// **'Change image'**
  String get aiDraftChangeImage;

  /// Button label to generate a draft
  ///
  /// In en, this message translates to:
  /// **'Generate draft'**
  String get aiDraftGenerate;

  /// Button label to reset and start a new draft
  ///
  /// In en, this message translates to:
  /// **'Start new draft'**
  String get aiDraftStartNew;

  /// Section title for OCR extracted text
  ///
  /// In en, this message translates to:
  /// **'Extracted text'**
  String get aiDraftExtractedText;

  /// Section title for selected image preview
  ///
  /// In en, this message translates to:
  /// **'Selected image'**
  String get aiDraftSelectedImage;

  /// Label for follow-up free text
  ///
  /// In en, this message translates to:
  /// **'Update request'**
  String get aiDraftUpdateRequest;

  /// Hint for follow-up free text
  ///
  /// In en, this message translates to:
  /// **'Add more details if the draft is not correct yet'**
  String get aiDraftUpdateHint;

  /// Button label to update an existing draft
  ///
  /// In en, this message translates to:
  /// **'Update draft'**
  String get aiDraftUpdate;

  /// Button label to confirm a generated draft
  ///
  /// In en, this message translates to:
  /// **'Confirm'**
  String get aiDraftConfirm;

  /// Section title for AI draft response preview
  ///
  /// In en, this message translates to:
  /// **'Draft preview'**
  String get aiDraftPreview;

  /// Message when the draft response has no data
  ///
  /// In en, this message translates to:
  /// **'No response data available.'**
  String get aiDraftNoResponseData;

  /// Message when OCR finds no text in the image
  ///
  /// In en, this message translates to:
  /// **'No text was found in the selected image.'**
  String get aiDraftNoTextFound;

  /// Message when no follow-up text is provided
  ///
  /// In en, this message translates to:
  /// **'Add more free text before updating the draft.'**
  String get aiDraftUpdateEmpty;

  /// Message when the first request is empty
  ///
  /// In en, this message translates to:
  /// **'Enter a request or choose an image first.'**
  String get aiDraftRequestEmpty;

  /// Message when the draft is confirmed
  ///
  /// In en, this message translates to:
  /// **'Draft confirmed'**
  String get aiDraftConfirmed;

  /// Message when the AI draft cannot be completed
  ///
  /// In en, this message translates to:
  /// **'The draft could not be completed. Start a new draft with clearer instructions.'**
  String get aiDraftFailed;

  /// Message when the draft is no longer editable
  ///
  /// In en, this message translates to:
  /// **'This draft is no longer editable. Start a new draft to continue.'**
  String get aiDraftLocked;

  /// Your Apps label
  ///
  /// In en, this message translates to:
  /// **'Your Apps'**
  String get yourApps;

  /// Your Settings label
  ///
  /// In en, this message translates to:
  /// **'Your Settings'**
  String get yourSettings;
}

class _AppLocalizationsDelegate
    extends LocalizationsDelegate<AppLocalizations> {
  const _AppLocalizationsDelegate();

  @override
  Future<AppLocalizations> load(Locale locale) {
    return SynchronousFuture<AppLocalizations>(lookupAppLocalizations(locale));
  }

  @override
  bool isSupported(Locale locale) =>
      <String>['en', 'id'].contains(locale.languageCode);

  @override
  bool shouldReload(_AppLocalizationsDelegate old) => false;
}

AppLocalizations lookupAppLocalizations(Locale locale) {
  // Lookup logic when only language code is specified.
  switch (locale.languageCode) {
    case 'en':
      return AppLocalizationsEn();
    case 'id':
      return AppLocalizationsId();
  }

  throw FlutterError(
    'AppLocalizations.delegate failed to load unsupported locale "$locale". This is likely '
    'an issue with the localizations generation tool. Please file an issue '
    'on GitHub with a reproducible sample app and the gen-l10n configuration '
    'that was used.',
  );
}
