package com.revolut.wallet.core.transaction

import com.revolut.wallet.core.account.AccountTable
import org.jetbrains.exposed.sql.Table

object TransactionLog : Table() {
    val id = long("id").primaryKey().autoIncrement()
    val transactionId = uuid("transaction_id") references TransactionTable.id
    val accountId = uuid("account_id") references AccountTable.id
    val debit = decimal("amount", 10, 2)
    val credit = decimal("amount", 10, 2)
}
