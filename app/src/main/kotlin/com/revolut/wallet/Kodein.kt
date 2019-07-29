package com.revolut.wallet

import com.revolut.wallet.core.account.AccountService
import com.revolut.wallet.core.transaction.TransactionService
import com.revolut.wallet.core.transfer.TransferService
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

val kodein = Kodein {
    bind<AccountService>() with singleton { AccountService() }
    bind<TransactionService>() with singleton { TransactionService() }
    bind<TransferService>() with singleton {
        TransferService(instance(), instance())
    }
}
