package com.revolut.wallet.core.transfer

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import java.lang.Exception

fun Route.transfer(transferService: TransferService) {
    route("/transfer") {
        post("/") {
            try {
                val transfer = call.receive<TransferDto>()
                val transaction = transferService.transfer(transfer)
                call.respond(HttpStatusCode.Created, transaction.id)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
            }
        }
    }
}
