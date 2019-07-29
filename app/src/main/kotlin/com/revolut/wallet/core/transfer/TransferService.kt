package com.revolut.wallet.core.transfer

import com.revolut.wallet.core.account.Account
import com.revolut.wallet.core.account.AccountService
import com.revolut.wallet.core.transaction.Transaction
import com.revolut.wallet.core.transaction.TransactionService
import com.revolut.wallet.exception.WalletException
import java.math.BigDecimal
import java.util.UUID
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KLogging

class TransferService(
    private val accountService: AccountService,
    private val transactionService: TransactionService
) {

    fun transfer(transfer: TransferDto): Transaction {
        logger.info { "Transfer: $transfer" }

        accountService.getAccount(transfer.toId)
        val fromAccount = lockAccount(transfer.fromId)
        fromAccount.checkIsBalanceEnough(transfer.amount)
        val transaction = transactionService.createTransaction(transfer)

        accountService.credit(fromAccount, transfer.amount)
        accountService.unlockAccount(transfer.fromId)

        GlobalScope.launch { transactionService.processTransaction(transaction) }

        return transaction
    }

    private fun lockAccount(accountId: UUID): Account {
        var attempt = 0
        while (attempt < ATTEMPT_MAX) {
            try {
                return accountService.lockAccount(accountId)
            } catch (e: WalletException) {
                logger.info { "Retry lock account" }
                attempt++
            }
        }
        throw WalletException("Can not lock account")
    }

    private fun Account.checkIsBalanceEnough(amount: BigDecimal) {
        if (this.balance <= amount) throw WalletException("Account has not enough on balance")
    }

    companion object : KLogging() {
        private const val ATTEMPT_MAX = 3
    }
}
