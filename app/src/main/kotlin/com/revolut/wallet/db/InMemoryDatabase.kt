package com.revolut.wallet.db

import com.revolut.wallet.core.account.AccountTable
import com.revolut.wallet.core.account.AccountTransactionTable
import com.revolut.wallet.core.transaction.TransactionLogTable
import com.revolut.wallet.core.transaction.TransactionTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

object InMemoryDatabase {

    fun init() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(
                AccountTable,
                TransactionTable,
                TransactionLogTable,
                AccountTransactionTable
            )
        }
    }
}
