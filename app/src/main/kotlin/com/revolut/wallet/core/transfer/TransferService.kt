package com.revolut.wallet.core.transfer

import com.revolut.wallet.core.account.AccountService
import com.revolut.wallet.core.transaction.Transaction
import com.revolut.wallet.core.transaction.TransactionProcessor
import com.revolut.wallet.core.transaction.TransactionService
import com.revolut.wallet.exception.WalletException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KLogging
import java.math.BigDecimal

class TransferService(
    private val accountService: AccountService,
    private val transactionService: TransactionService,
    private val transactionProcessor: TransactionProcessor
) {

    suspend fun transfer(transfer: TransferDto): Transaction {
        logger.info { "Transfer: $transfer" }

        if (transfer.fromId == transfer.toId) throw WalletException("Invalid request")
        if (transfer.amount <= BigDecimal.ZERO) throw WalletException("Invalid request")

        accountService.getAccount(transfer.fromId)
        accountService.getAccount(transfer.toId)
        val transaction = transactionService.createTransaction(transfer)

        transactionProcessor.creditFromAccount(transaction)
        GlobalScope.launch { transactionProcessor.debitToAccount(transaction) }

        return transaction
    }

    companion object : KLogging()
}
