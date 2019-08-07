package com.revolut.wallet.core.transaction

import com.revolut.wallet.core.account.Account
import com.revolut.wallet.core.account.AccountService
import com.revolut.wallet.exception.WalletException
import kotlinx.coroutines.GlobalScope
import java.math.BigDecimal
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KLogging

class TransactionProcessor(
    private val accountService: AccountService,
    private val transactionService: TransactionService
) {

    suspend fun creditFromAccount(transaction: Transaction) {
        logger.info { "Create credit transaction log: $transaction" }
        lockAccount(transaction.fromAccountId)

        try {
            val fromAccount = accountService.getAccount(transaction.fromAccountId)
            fromAccount.checkIsBalanceEnough(transaction.amount)

            transactionService.createCreditTransactionLog(transaction, fromAccount)
        } finally {
            accountService.unlockAccount(transaction.fromAccountId)
        }
    }

    suspend fun debitToAccount(transaction: Transaction) {
        logger.info { "Create debit transaction log: $transaction" }
        val locked = try {
            lockAccount(transaction.toAccountId)
            true
        } catch (e: Exception) {
            GlobalScope.launch { rollbackToAccount(transaction) }
            false
        }
        if (!locked) return
        try {
            val toAccount = accountService.getAccount(transaction.toAccountId)
            transactionService.createDebitTransactionLog(transaction, toAccount)
        } finally {
            accountService.unlockAccount(transaction.toAccountId)
        }
    }

    suspend fun rollbackToAccount(transaction: Transaction) {
        logger.info { "Create rollback transaction log: $transaction" }
        var locked = false
        while (!locked) {
            try {
                lockAccount(transaction.fromAccountId)
                locked = true
            } catch (e: Exception) {
            }
        }
        try {
            val toAccount = accountService.getAccount(transaction.fromAccountId)
            transactionService.createRollbackTransactionLog(transaction, toAccount)
        } finally {
            accountService.unlockAccount(transaction.fromAccountId)
        }
    }

    private fun Account.checkIsBalanceEnough(amount: BigDecimal) {
        if (this.balance < amount) throw WalletException("Account has not enough on balance")
    }

    private suspend fun lockAccount(accountId: UUID) {
        var attempt = 0
        var isLocked = false
        while (!isLocked && attempt < ATTEMPT_MAX) {
            try {
                accountService.lockAccount(accountId)
                isLocked = true
            } catch (e: WalletException) {
                logger.info { "Retry lock account: $accountId" }
                delay(1000)
                attempt++
            }
        }
        if (!isLocked) throw WalletException("Can not lock account")
    }

    companion object : KLogging() {
        private const val ATTEMPT_MAX = 3
    }
}
