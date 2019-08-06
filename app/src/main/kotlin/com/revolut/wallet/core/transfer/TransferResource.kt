package com.revolut.wallet.core.transfer

import com.revolut.wallet.exception.WalletException
import com.revolut.wallet.exception.getError
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import mu.KotlinLogging.logger

private val logger = logger("Route.transfer")

fun Route.transfer(transferService: TransferService) {
    route("/transfer") {
        post("/") {
            try {
                val transfer = call.receive<TransferDto>()
                val transaction = transferService.transfer(transfer)
                call.respond(transaction.id)
            } catch (e: WalletException) {
                call.respond(HttpStatusCode.Conflict, e.getError())
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.getError())
                logger.error(e) { "Can not transfer money" }
            }
        }
    }
}
