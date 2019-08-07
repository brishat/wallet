package com.revolut.wallet.core.account

import java.math.BigDecimal
import java.util.UUID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

data class Account(
    val id: UUID,
    val balance: BigDecimal,
    val locked: Boolean,
    val lockTransactionId: UUID?
)

object AccountTable : Table("account") {
    val id = uuid("id").primaryKey()
    val balance = decimal("balance", 10, 2)
    val locked = bool("locked")
    val lockTransactionId = uuid("lockTransactionId").nullable()
}

fun ResultRow.toAccount() = Account(
    id = this[AccountTable.id],
    balance = this[AccountTable.balance],
    locked = this[AccountTable.locked],
    lockTransactionId = this[AccountTable.lockTransactionId]
)
