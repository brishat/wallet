package com.revolut.wallet.core.account

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import java.util.UUID

fun Route.account(accountService: AccountService) {
    route("/account") {

        get("/all") {
            call.respond(accountService.getAccountList())
        }

        get("/{id}") {
            val account = accountService.getAccount(UUID.fromString(call.parameters["id"]))
            if (account == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(account)
        }

        post("/") {
            val account = accountService.createAccount()
            if (account == null) call.respond(HttpStatusCode.InternalServerError)
            else call.respond(HttpStatusCode.Created, account)
        }
    }
}
