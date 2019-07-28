package com.revolut.wallet.core.transaction

import com.revolut.wallet.core.account.AccountTable
import org.jetbrains.exposed.sql.Table

object Transaction : Table() {
    val id = uuid("id").primaryKey()
    val fromAccountId = uuid("id") references AccountTable.id
    val toAccountId = uuid("id") references AccountTable.id
    val amount = decimal("amount", 10, 2)
    val status = enumerationByName("status", 10, TransactionStatus::class)
}
