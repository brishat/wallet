package com.revolut.wallet.core.transaction

import com.revolut.wallet.core.account.AccountTable
import java.math.BigDecimal
import java.util.UUID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

data class TransactionLog(
    val id: Long,
    val transactionId: UUID,
    val accountId: UUID,
    val debit: BigDecimal,
    val credit: BigDecimal
)

object TransactionLogTable : Table() {
    val id = long("id").primaryKey().autoIncrement()
    val transactionId = uuid("transaction_id") references TransactionTable.id
    val accountId = uuid("account_id") references AccountTable.id
    val debit = decimal("debit", 10, 2)
    val credit = decimal("credit", 10, 2)
}

fun ResultRow.toTransactionLog() = TransactionLog(
    id = this[TransactionLogTable.id],
    transactionId = this[TransactionLogTable.transactionId],
    accountId = this[TransactionLogTable.accountId],
    debit = this[TransactionLogTable.debit],
    credit = this[TransactionLogTable.credit]
)
