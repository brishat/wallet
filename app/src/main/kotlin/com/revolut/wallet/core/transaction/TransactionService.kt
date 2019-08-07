package com.revolut.wallet.core.transaction

import com.revolut.wallet.core.account.Account
import com.revolut.wallet.core.account.AccountService
import com.revolut.wallet.core.transfer.TransferDto
import com.revolut.wallet.exception.WalletException
import java.math.BigDecimal
import java.util.UUID
import mu.KLogging
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class TransactionService(
    private val accountService: AccountService
) {

    fun createTransaction(transfer: TransferDto): Transaction = transaction {
        TransactionTable.insert {
            it[id] = UUID.randomUUID()
            it[fromAccountId] = transfer.fromId
            it[toAccountId] = transfer.toId
            it[amount] = transfer.amount
            it[status] = TransactionStatus.IN_PROGRESS
        }.resultedValues
            ?.firstOrNull()?.toTransaction()
            ?: throw WalletException("Error on create transaction")
    }

    fun createCreditTransactionLog(transaction: Transaction, fromAccount: Account): Unit = transaction {
        accountService.credit(fromAccount, transaction)

        TransactionLogTable.insert {
            it[transactionId] = transaction.id
            it[accountId] = fromAccount.id
            it[debit] = BigDecimal.ZERO
            it[credit] = transaction.amount
        }
    }

    fun createDebitTransactionLog(transaction: Transaction, toAccount: Account): Unit = transaction {
        accountService.debit(toAccount, transaction)

        TransactionLogTable.insert {
            it[transactionId] = transaction.id
            it[accountId] = toAccount.id
            it[debit] = BigDecimal.ZERO
            it[credit] = transaction.amount
        }

        TransactionTable.update({ TransactionTable.id eq transaction.id }) {
            it[status] = TransactionStatus.FINISHED
        }
    }

    fun createRollbackTransactionLog(transaction: Transaction, toAccount: Account): Unit = transaction {
        accountService.debit(toAccount, transaction)

        TransactionTable.update({ TransactionTable.id eq transaction.id }) {
            it[status] = TransactionStatus.RETURNED
        }
    }

    companion object : KLogging()
}
