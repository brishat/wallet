package com.revolut.wallet.core

import java.util.UUID

data class Transaction(
    val accountId: UUID,
    val transactionId: UUID,
    val debit: Double,
    val credit: Double
)
