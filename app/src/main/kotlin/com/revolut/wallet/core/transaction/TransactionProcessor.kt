package com.revolut.wallet.core.transaction

import com.revolut.wallet.core.account.Account
import com.revolut.wallet.core.account.AccountService
import com.revolut.wallet.exception.OptmisticLockException
import com.revolut.wallet.exception.WalletException
import kotlinx.coroutines.GlobalScope
import java.math.BigDecimal
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KLogging

class TransactionProcessor(
    private val accountService: AccountService,
    private val transactionService: TransactionService
) {

    suspend fun creditFromAccount(transaction: Transaction) {
        logger.info { "Create credit transaction log: $transaction" }

        retry(ATTEMPT_MAX) {
            val fromAccount = accountService.getAccount(transaction.fromAccountId)
            fromAccount.checkIsBalanceEnough(transaction.amount)
            transactionService.createCreditTransactionLog(transaction, fromAccount)
        }
    }

    suspend fun debitToAccount(transaction: Transaction) {
        logger.info { "Create debit transaction log: $transaction" }

        try {
            retry(ATTEMPT_MAX) {
                val toAccount = accountService.getAccount(transaction.toAccountId)
                transactionService.createDebitTransactionLog(transaction, toAccount)
            }
        } catch (e: Exception) {
            GlobalScope.launch { rollbackToAccount(transaction) }
        }
    }

    suspend fun rollbackToAccount(transaction: Transaction) {
        logger.info { "Create rollback transaction log: $transaction" }
        retry {
            val fromAccount = accountService.getAccount(transaction.fromAccountId)
            transactionService.createRollbackTransactionLog(transaction, fromAccount)
        }
    }

    private suspend fun retry(
        attemptsCount: Int = 0,
        job: suspend () -> Unit
    ) {
        var attempt = 0
        var isProcessed = false
        while (!isProcessed && (attempt < attemptsCount || attemptsCount == 0)) {
            try {
                job()
                isProcessed = true
            } catch (e: OptmisticLockException) {
                logger.info { "Retry account operation" }
                delay(1000)
                attempt++
            }
        }
        if (!isProcessed) throw WalletException("Can not process operation")
    }

    private fun Account.checkIsBalanceEnough(amount: BigDecimal) {
        if (this.balance < amount) throw WalletException("Account has not enough on balance")
    }

    companion object : KLogging() {
        private const val ATTEMPT_MAX = 3
    }
}
