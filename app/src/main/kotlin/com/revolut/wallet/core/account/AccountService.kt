package com.revolut.wallet.core.account

import java.math.BigDecimal
import java.util.UUID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class AccountService {

    fun createAccount(): Account? = transaction {
        val result = AccountTable.insert {
            it[id] = UUID.randomUUID()
            it[balance] = BigDecimal.ZERO
            it[locked] = false
        }
        return@transaction result.resultedValues?.firstOrNull()?.toAccount()
    }

    fun getAccount(id: UUID): Account? = transaction {
        return@transaction AccountTable.select { AccountTable.id eq id }.firstOrNull()?.toAccount()
    }

    fun getAccountList(): List<Account> = transaction {
        return@transaction AccountTable.selectAll().map { it.toAccount() }
    }
}
