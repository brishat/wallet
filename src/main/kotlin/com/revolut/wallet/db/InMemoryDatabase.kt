package com.revolut.wallet.db

import org.jetbrains.exposed.sql.Database

object InMemoryDatabase {

    fun init() {
        Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")
    }
}
