package com.revolut.wallet

import com.revolut.wallet.core.account.AccountService
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton

val kodein = Kodein {
    bind<AccountService>() with singleton { AccountService() }
}
