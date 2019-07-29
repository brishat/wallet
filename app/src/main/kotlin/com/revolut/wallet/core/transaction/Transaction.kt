package com.revolut.wallet.core.transaction

import com.revolut.wallet.core.account.AccountTable
import java.math.BigDecimal
import java.util.UUID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

data class Transaction(
    val id: UUID,
    val fromAccountId: UUID,
    val toAccountId: UUID,
    val amount: BigDecimal,
    val status: TransactionStatus
)

object TransactionTable : Table() {
    val id = uuid("id").primaryKey()
    val fromAccountId = uuid("from_account_id") references AccountTable.id
    val toAccountId = uuid("to_account_id") references AccountTable.id
    val amount = decimal("amount", 10, 2)
    val status = enumerationByName("status", 20, TransactionStatus::class)
}

fun ResultRow.toTransaction() = Transaction(
    id = this[TransactionTable.id],
    fromAccountId = this[TransactionTable.fromAccountId],
    toAccountId = this[TransactionTable.toAccountId],
    amount = this[TransactionTable.amount],
    status = this[TransactionTable.status]
)
