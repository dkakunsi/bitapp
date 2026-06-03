import 'package:bitapp/features/account/data/account.dart';
import 'package:bitapp/features/account/data/account_store.dart';
import 'package:bitapp/features/loan/data/loan.dart';
import 'package:bitapp/features/loan/data/loan_store.dart';
import 'package:bitapp/features/transaction/data/transaction_model.dart';
import 'package:bitapp/features/transaction/data/transaction_store.dart';
import 'package:bitapp/features/transaction/domain/transaction.dart';
import 'package:bitapp/features/transaction/domain/transaction_type.dart';

class LocalTansactionService {
  final AccountStore _accountStore;
  final LoanStore _loanStore;
  final TransactionStore _transactionStore;

  LocalTansactionService(
    this._transactionStore,
    this._accountStore,
    this._loanStore,
  );

  Future<void> save(Transaction transaction) async {
    await _transactionStore.save(transaction.toModel());
    await postCreation(transaction);
  }

  Future<void> postCreation(Transaction transaction) async {
    if (transaction.type == TransactionType.debit) {
      await debitAccount(transaction.sourceAccount!.id!, transaction.amount);
    } else if (transaction.type == TransactionType.credit) {
      await creditAccount(
        transaction.destinationAccount!.id!,
        transaction.amount,
      );
    } else {
      await debitAccount(transaction.sourceAccount!.id!, transaction.amount);
      await creditAccount(
        transaction.destinationAccount!.id!,
        transaction.amount,
      );
    }
    if (transaction.loan != null) {
      await decreaseLoan(transaction.loan!.id!, transaction.amount);
    }
  }

  Future<void> debitAccount(String accountId, double amount) async {
    final existingAccount = await _accountStore.get(accountId);
    if (existingAccount == null) {
      return;
    }
    final updatedAccount = existingAccount.copyWith(
      balance: (existingAccount.balance ?? 0) - amount,
    );
    _accountStore.save(updatedAccount);
  }

  Future<void> creditAccount(String accountId, double amount) async {
    final existingAccount = await _accountStore.get(accountId);
    if (existingAccount == null) {
      return;
    }
    final updatedAccount = existingAccount.copyWith(
      balance: (existingAccount.balance ?? 0) + amount,
    );
    _accountStore.save(updatedAccount);
  }

  Future<void> decreaseLoan(String loanId, double amount) async {
    final loan = await _loanStore.get(loanId);
    if (loan == null) {
      return;
    }
    _loanStore.save(
      loan.copyWith(
        remainingAmount: (loan.remainingAmount ?? loan.amount) - amount,
      ),
    );
  }

  Future<void> postDeletion(TransactionModel transactionModel) async {
    if (transactionModel.transactionType == TransactionType.debit) {
      await creditAccount(transactionModel.sourceAccountId!, transactionModel.amount);
    } else if (transactionModel.transactionType == TransactionType.credit) {
      await debitAccount(
        transactionModel.destinationAccountId!,
        transactionModel.amount,
      );
    } else {
      await debitAccount(
        transactionModel.destinationAccountId!,
        transactionModel.amount,
      );
      await creditAccount(transactionModel.sourceAccountId!, transactionModel.amount);
    }
    if (transactionModel.loanId != null) {
      await increaseLoan(transactionModel.loanId!, transactionModel.amount);
    }
  }

  Future<void> increaseLoan(String loanId, double amount) async {
    final loan = await _loanStore.get(loanId);
    if (loan == null) {
      return;
    }
    _loanStore.save(
      loan.copyWith(remainingAmount: loan.remainingAmount! + amount),
    );
  }

  Future<Transaction> buildTransaction(
    TransactionModel transactionModel,
  ) async {
    return Transaction.fromModel(
      transactionModel,
      await _getAccount(transactionModel.sourceAccountId),
      await _getAccount(transactionModel.destinationAccountId),
      await _getLoan(transactionModel.loanId),
    );
  }

  Future<Account?> _getAccount(String? accountId) async =>
      accountId != null ? await _accountStore.get(accountId) : null;

  Future<Loan?> _getLoan(String? loanId) async =>
      loanId != null ? await _loanStore.get(loanId) : null;
}
