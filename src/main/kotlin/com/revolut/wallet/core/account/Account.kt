package com.revolut.wallet.core.account

import org.jetbrains.exposed.sql.Table

object Account : Table() {
    val id = uuid("id").primaryKey().autoIncrement()
    val balance = decimal("amount", 10, 2)
    val locked = bool("locked")
}
