package com.revolut.wallet

import com.revolut.wallet.db.InMemoryDatabase
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

private const val PORT = 8080

fun main() {
    InMemoryDatabase.init()

    embeddedServer(Netty, PORT) {
        routing {
            get("/") {
                call.respondText("Hello, world!", ContentType.Text.Html)
            }
        }
    }.start(wait = true)
}
