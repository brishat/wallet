package com.revolut.wallet.core.account

import com.fasterxml.jackson.annotation.JsonIgnore
import java.math.BigDecimal
import java.util.UUID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.joda.time.DateTime

data class Account(
    val id: UUID,
    val balance: BigDecimal,
    @JsonIgnore
    val version: DateTime
)

object AccountTable : Table("account") {
    val id = uuid("id").primaryKey()
    val balance = decimal("balance", 10, 2)
    val version = datetime("version")
}

fun ResultRow.toAccount() = Account(
    id = this[AccountTable.id],
    balance = this[AccountTable.balance],
    version = this[AccountTable.version]
)
