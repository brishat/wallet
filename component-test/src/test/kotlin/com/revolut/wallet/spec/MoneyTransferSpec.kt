package com.revolut.wallet.spec

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.revolut.wallet.core.Account
import com.revolut.wallet.core.Transfer
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.DescribeSpec
import io.ktor.client.HttpClient
import io.ktor.client.features.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.host
import io.ktor.client.request.port
import io.ktor.client.request.post
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import java.util.UUID

@Suppress("BlockingMethodInNonBlockingContext")
class MoneyTransferSpec : DescribeSpec({
    describe("Simple money transfer") {
        val account1 = createAccount(100.0)
        val account2 = createAccount()

        context("Check account 1") {
            val result = client.get<String>("account/${account1.id}")
            val account = mapper.readValue(result, Account::class.java)
            it("Account 1 created") {
                account.id shouldBe account1.id
                account.balance shouldBe 100.0
            }
        }

        context("Check account 2") {
            val result = client.get<String>("account/${account2.id}")
            val account = mapper.readValue(result, Account::class.java)
            it("Account 2 created") {
                account.id shouldBe account2.id
                account.balance shouldBe 0.0
            }
        }

        context("Transfer 50 from account1 to account2") {
            val transactionId = transfer(account1, account2, 50)
            it("should create transaction") {
                transactionId shouldNotBe null
            }
        }

        context("Transfer 150 from account1 should fail") {
            val transactionId = transfer(account1, account2, 150)
            it("should fail transaction") {
                transactionId shouldBe null
            }
        }
    }
})

private val mapper = jacksonObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
private val client = HttpClient {
    defaultRequest {
        host = "127.0.0.1"
        port = 8080
    }
}

@Suppress("BlockingMethodInNonBlockingContext")
private suspend fun createAccount(initialAmount: Double = 0.0): Account {
    val result = client.post<HttpResponse>("account") {
        body = TextContent(
            text = """{"initial_balance": $initialAmount}""",
            contentType = ContentType.Application.Json
        )
    }
    return mapper.readValue(result.readText(), Account::class.java)
}

@Suppress("BlockingMethodInNonBlockingContext")
private suspend fun transfer(from: Account, to: Account, amount: Long): UUID? {
    val response = client.post<HttpResponse>("transfer") {
        body = TextContent(
            text = mapper.writeValueAsString(Transfer(from, to, amount)),
            contentType = ContentType.Application.Json
        )
    }

    return if (response.status == HttpStatusCode.OK) UUID.fromString(response.readText().trim('"'))
    else null
}
