package com.revolut.wallet.core

import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Account(
    val id: UUID,
    val balance: BigDecimal
)
