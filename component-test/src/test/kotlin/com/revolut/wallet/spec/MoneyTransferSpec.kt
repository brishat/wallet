package com.revolut.wallet.spec

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.revolut.wallet.core.Account
import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec
import io.ktor.client.HttpClient
import io.ktor.client.features.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.host
import io.ktor.client.request.port
import io.ktor.client.request.post

@Suppress("BlockingMethodInNonBlockingContext")
class MoneyTransferSpec : DescribeSpec({
    describe("Simple money transfer") {
        val account1 = createAccount()
        val account2 = createAccount()

        context("Check account 1") {
            val result = client.get<String>("account/${account1.id}")
            val account = mapper.readValue(result, Account::class.java)
            it("account 1 is correct") {
                account.id shouldBe account1.id
                account.balance shouldBe account1.balance
            }
        }

        context("Check account 2") {
            val result = client.get<String>("account/${account2.id}")
            val account = mapper.readValue(result, Account::class.java)
            it("account 2 is correct") {
                account.id shouldBe account2.id
                account.balance shouldBe account2.balance
            }
        }
    }
})

private val mapper = jacksonObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
private val client = HttpClient() {
    defaultRequest {
        host = "127.0.0.1"
        port = 8080
    }
}

@Suppress("BlockingMethodInNonBlockingContext")
private suspend fun createAccount(): Account {
    val createResult = client.post<String>("account")
    return mapper.readValue(createResult, Account::class.java)
}
