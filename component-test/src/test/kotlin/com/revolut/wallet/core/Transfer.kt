package com.revolut.wallet.core

import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Transfer(
    val fromId: UUID,
    val toId: UUID,
    val amount: BigDecimal
) {
    constructor(
        fromAccount: Account,
        toAccount: Account,
        amount: BigDecimal
    ) : this(fromAccount.id, toAccount.id, amount)
}
