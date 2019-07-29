package com.revolut.wallet.core.account

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import java.lang.Exception
import java.util.UUID

fun Route.account(accountService: AccountService) {
    route("/account") {
        get("/all") {
            call.respond(accountService.getAccountList())
        }

        get("/{id}") {
            try {
                val account = accountService.getAccount(UUID.fromString(call.parameters["id"]))
                call.respond(account)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound, e.localizedMessage)
            }
        }

        post("/") {
            try {
                val initialBalance = call.receive<CreateAccountDto>().initial_balance
                val account = accountService.createAccount(initialBalance)
                call.respond(account)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
            }
        }
    }
}
