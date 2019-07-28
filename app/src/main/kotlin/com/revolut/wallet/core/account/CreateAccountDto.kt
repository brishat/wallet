package com.revolut.wallet.core.account

import java.math.BigDecimal

data class CreateAccountDto(
    val initial_balance: BigDecimal = BigDecimal.ZERO
)
