package com.revolut.wallet.core.account

import com.revolut.wallet.core.transaction.Transaction
import com.revolut.wallet.exception.WalletException
import java.math.BigDecimal
import java.util.UUID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.transaction
import org.jetbrains.exposed.sql.update

class AccountService {

    suspend fun createAccount(initialBalance: BigDecimal): Account = transaction {
        val result = AccountTable.insert {
            it[id] = UUID.randomUUID()
            it[balance] = initialBalance
            it[locked] = false
        }
        return@transaction result.resultedValues?.firstOrNull()?.toAccount()
            ?: throw WalletException("Error on create account")
    }

    suspend fun getAccount(id: UUID): Account = transaction {
        return@transaction AccountTable.select { AccountTable.id eq id }.firstOrNull()?.toAccount()
            ?: throw WalletException("Account not found")
    }

    suspend fun getAccountList(): List<Account> = transaction {
        return@transaction AccountTable.selectAll().map { it.toAccount() }
    }

    suspend fun lockAccount(accountId: UUID, transactionId: UUID) = transaction {
        val result = AccountTable.update({
            AccountTable.id eq accountId and
                (AccountTable.locked eq false) and
                (AccountTable.lockTransactionId.isNull())
        }) {
            it[locked] = true
            it[lockTransactionId] = transactionId
        }

        if (result == 0) throw WalletException("Can not lock account")
    }

    suspend fun unlockAccount(accountId: UUID, transactionId: UUID): Unit = transaction {
        AccountTable.update({
            AccountTable.id eq accountId and
                (AccountTable.locked eq true) and
                (AccountTable.lockTransactionId eq transactionId)
        }) {
            it[locked] = false
            it[lockTransactionId] = null
        }
        Unit
    }

    fun credit(account: Account, transaction: Transaction) {
        val accountResult = AccountTable.update({
            AccountTable.id eq account.id and
                (AccountTable.locked eq true)
        }) {
            it[balance] = account.balance - transaction.amount
        }

        if (accountResult == 0) throw IllegalStateException()

        AccountTransactionTable.insert {
            it[accountId] = account.id
            it[transactionId] = transaction.id
            it[debit] = BigDecimal.ZERO
            it[credit] = transaction.amount
        }
    }

    fun debit(account: Account, transaction: Transaction) {
        val result = AccountTable.update({
            AccountTable.id eq account.id and
                (AccountTable.locked eq true)
        }) {
            it[balance] = account.balance + transaction.amount
        }

        if (result == 0) throw IllegalStateException()

        AccountTransactionTable.insert {
            it[accountId] = account.id
            it[transactionId] = transaction.id
            it[debit] = transaction.amount
            it[credit] = BigDecimal.ZERO
        }
    }
}
