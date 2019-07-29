package com.revolut.wallet.core.account

import com.revolut.wallet.exception.WalletException
import java.util.UUID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class AccountTransactionService {

    fun getTransactions(accountId: UUID): List<AccountTransaction> = transaction {
        AccountTransactionTable.select {
            AccountTransactionTable.accountId eq accountId
        }.toList().map { it.toAccountTransaction() }
    }

    fun getTransaction(accountId: UUID, transactionId: UUID): AccountTransaction = transaction {
        AccountTransactionTable.select {
            AccountTransactionTable.accountId eq accountId and
                (AccountTransactionTable.transactionId eq transactionId)
        }.firstOrNull()?.toAccountTransaction()
            ?: throw WalletException("Transaction not found")
    }
}
