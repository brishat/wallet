package com.revolut.wallet.core.transaction

import com.revolut.wallet.core.account.Account
import com.revolut.wallet.core.account.AccountService
import com.revolut.wallet.exception.WalletException
import java.math.BigDecimal
import java.util.UUID
import kotlinx.coroutines.delay
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
        lockAccount(transaction.toAccountId)
        try {
            val toAccount = accountService.getAccount(transaction.toAccountId)
            transactionService.createDebitTransactionLog(transaction, toAccount)
        } finally {
            accountService.unlockAccount(transaction.toAccountId)
        }
    }

    private fun Account.checkIsBalanceEnough(amount: BigDecimal) {
        if (this.balance <= amount) throw WalletException("Account has not enough on balance")
    }

    private suspend fun lockAccount(accountId: UUID) {
        var attempt = 0
        var isLocked = false
        while (!isLocked && attempt < ATTEMPT_MAX) {
            try {
                accountService.lockAccount(accountId)
                isLocked = true
            } catch (e: WalletException) {
                logger.info { "Retry lock account" }
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
