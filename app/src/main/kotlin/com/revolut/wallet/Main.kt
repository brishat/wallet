package com.revolut.wallet

import com.fasterxml.jackson.databind.SerializationFeature
import com.revolut.wallet.core.account.AccountService
import com.revolut.wallet.core.account.account
import com.revolut.wallet.core.transfer.TransferService
import com.revolut.wallet.core.transfer.transfer
import com.revolut.wallet.db.InMemoryDatabase
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.jackson.jackson
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.kodein.di.generic.instance

private const val PORT = 8080

fun Application.module() {
    install(ContentNegotiation) {
        jackson {
            configure(SerializationFeature.INDENT_OUTPUT, true)
        }
    }

    InMemoryDatabase.init()

    install(Routing) {
        val accountService by kodein.instance<AccountService>()
        val transactionService by kodein.instance<TransferService>()

        routing {
            get("/") { call.respondText("Hello, world!", ContentType.Text.Html) }
            account(accountService)
            transfer(transactionService)
        }
    }
}

fun main() {
    embeddedServer(Netty, PORT, module = Application::module).start(wait = true)
}
