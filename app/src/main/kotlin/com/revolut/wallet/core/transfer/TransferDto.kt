package com.revolut.wallet.core.transfer

import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class TransferDto(
    val fromId: UUID,
    val toId: UUID,
    val amount: BigDecimal
)
