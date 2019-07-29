package com.revolut.wallet.core.account

import com.revolut.wallet.core.transaction.TransactionStatus
import com.revolut.wallet.core.transaction.TransactionTable
import org.jetbrains.exposed.sql.Table

object AccountTransactionTable : Table() {
    val accountId = uuid("account_id").primaryKey() references AccountTable.id
    val transactionId = uuid("transaction_id") references TransactionTable.id
    val debit = decimal("amount", 10, 2)
    val credit = decimal("amount", 10, 2)
    val status = enumerationByName("status", 10, TransactionStatus::class)
}
