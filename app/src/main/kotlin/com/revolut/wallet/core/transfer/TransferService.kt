package com.revolut.wallet.core.transfer

import com.revolut.wallet.core.account.AccountService
import com.revolut.wallet.core.transaction.Transaction
import com.revolut.wallet.core.transaction.TransactionProcessor
import com.revolut.wallet.core.transaction.TransactionService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KLogging

class TransferService(
    private val accountService: AccountService,
    private val transactionService: TransactionService,
    private val transactionProcessor: TransactionProcessor
) {

    suspend fun transfer(transfer: TransferDto): Transaction {
        logger.info { "Transfer: $transfer" }

        accountService.getAccount(transfer.fromId)
        accountService.getAccount(transfer.toId)

        val transaction = transactionService.createTransaction(transfer)

        transactionProcessor.creditFromAccount(transaction)
        GlobalScope.launch { transactionProcessor.debitToAccount(transaction) }

        return transaction
    }

    companion object : KLogging()
}
