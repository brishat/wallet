package com.revolut.wallet.core.account

import com.revolut.wallet.core.transaction.Transaction
import com.revolut.wallet.core.transaction.TransactionStatus
import org.jetbrains.exposed.sql.Table

object AccountTransaction : Table() {
    val accountId = uuid("account_id").primaryKey() references Account.id
    val transactionId = uuid("transaction_id") references Transaction.id
    val debit = decimal("amount", 10, 2)
    val credit = decimal("amount", 10, 2)
    val status = enumerationByName("status", 10, TransactionStatus::class)
}
