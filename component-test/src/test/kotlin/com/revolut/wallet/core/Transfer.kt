package com.revolut.wallet.core

import com.fasterxml.jackson.annotation.JsonInclude
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Transfer(
    val fromId: UUID,
    val toId: UUID,
    val amount: Double
) {
    constructor(
        fromAccount: Account,
        toAccount: Account,
        amount: Long
    ) : this(fromAccount.id, toAccount.id, amount.toDouble())
}
