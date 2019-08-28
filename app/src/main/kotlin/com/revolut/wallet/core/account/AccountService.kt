package com.revolut.wallet.core.account

import com.revolut.wallet.core.transaction.Transaction
import com.revolut.wallet.exception.OptmisticLockException
import com.revolut.wallet.exception.WalletException
import java.math.BigDecimal
import java.util.UUID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.transaction
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime

class AccountService {

    suspend fun createAccount(initialBalance: BigDecimal): Account = transaction {
        val result = AccountTable.insert {
            it[id] = UUID.randomUUID()
            it[balance] = initialBalance
            it[version] = DateTime.now()
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

    fun credit(account: Account, transaction: Transaction) {
        val accountResult = AccountTable.update({
            AccountTable.id eq account.id and
                (AccountTable.version eq account.version)
        }) {
            it[balance] = account.balance - transaction.amount
            it[version] = DateTime.now()
        }

        if (accountResult == 0) throw OptmisticLockException()

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
                (AccountTable.version eq account.version)
        }) {
            it[balance] = account.balance + transaction.amount
            it[version] = DateTime.now()
        }

        if (result == 0) throw OptmisticLockException()

        AccountTransactionTable.insert {
            it[accountId] = account.id
            it[transactionId] = transaction.id
            it[debit] = transaction.amount
            it[credit] = BigDecimal.ZERO
        }
    }
}
