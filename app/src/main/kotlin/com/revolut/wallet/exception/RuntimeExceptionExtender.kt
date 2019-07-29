package com.revolut.wallet.exception

fun Exception.getError() = this.message ?: ""
