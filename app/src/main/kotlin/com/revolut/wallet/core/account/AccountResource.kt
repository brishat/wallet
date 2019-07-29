package com.revolut.wallet.core.account

import com.revolut.wallet.exception.WalletException
import com.revolut.wallet.exception.getError
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import java.util.UUID
import mu.KotlinLogging.logger

private val logger = logger("Route.account")

fun Route.account(
    accountService: AccountService,
    accountTransactionService: AccountTransactionService
) {
    route("/account") {
        get("/all") {
            call.respond(accountService.getAccountList())
        }

        get("/{id}") {
            try {
                val account = accountService.getAccount(UUID.fromString(call.parameters["id"]))
                call.respond(account)
            } catch (e: WalletException) {
                call.respond(HttpStatusCode.InternalServerError, e.getError())
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.getError())
                logger.error(e) { "Unexpected error" }
            }
        }

        post("/") {
            try {
                val initialBalance = call.receive<CreateAccountDto>().initial_balance
                val account = accountService.createAccount(initialBalance)
                call.respond(account)
            } catch (e: WalletException) {
                call.respond(HttpStatusCode.InternalServerError, e.getError())
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.getError())
                logger.error(e) { "Unexpected error" }
            }
        }

        get("/{id}/transaction/all") {
            try {
                val accountId = UUID.fromString(call.parameters["id"])
                val transactionList = accountTransactionService.getTransactions(accountId)
                call.respond(transactionList)
            } catch (e: WalletException) {
                call.respond(HttpStatusCode.InternalServerError, e.getError())
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.getError())
                logger.error(e) { "Unexpected error" }
            }
        }

        get("/{id}/transaction/{transactionId}") {
            try {
                val accountId = UUID.fromString(call.parameters["id"])
                val transactionId = UUID.fromString(call.parameters["transactionId"])
                val transactionList = accountTransactionService.getTransaction(accountId, transactionId)
                call.respond(transactionList)
            } catch (e: WalletException) {
                call.respond(HttpStatusCode.InternalServerError, e.getError())
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.getError())
                logger.error(e) { "Unexpected error" }
            }
        }
    }
}
