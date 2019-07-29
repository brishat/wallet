package com.revolut.wallet.core.transaction

import com.revolut.wallet.core.transfer.TransferDto
import com.revolut.wallet.exception.WalletException
import java.util.UUID
import kotlinx.coroutines.delay
import mu.KLogging
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class TransactionService {

    fun createTransaction(transfer: TransferDto): Transaction = transaction {
        val result = TransactionTable.insert {
            it[id] = UUID.randomUUID()
            it[fromAccountId] = transfer.fromId
            it[toAccountId] = transfer.toId
            it[amount] = transfer.amount
            it[status] = TransactionStatus.IN_PROGRESS
        }
        return@transaction result.resultedValues?.firstOrNull()?.toTransaction()
            ?: throw WalletException("Error on create transaction")
    }

    suspend fun processTransaction(transaction: Transaction) {
        delay(10000L)
        logger.info { "Process transaction: $transaction" }
    }

    companion object : KLogging()
}
