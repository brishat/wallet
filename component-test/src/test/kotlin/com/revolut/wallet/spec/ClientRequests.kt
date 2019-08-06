package com.revolut.wallet.spec

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.revolut.wallet.core.Account
import com.revolut.wallet.core.Transfer
import io.ktor.client.HttpClient
import io.ktor.client.features.defaultRequest
import io.ktor.client.request.host
import io.ktor.client.request.port
import io.ktor.client.request.post
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import java.util.UUID

val mapper: ObjectMapper = jacksonObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
val client = HttpClient {
    defaultRequest {
        host = "127.0.0.1"
        port = 8080
    }
}

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun createAccount(initialAmount: Double = 0.0): Account {
    val result = client.post<HttpResponse>("account") {
        body = TextContent(
            text = """{"initial_balance": $initialAmount}""",
            contentType = ContentType.Application.Json
        )
    }
    return mapper.readValue(result.readText(), Account::class.java)
}

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun transfer(from: Account, to: Account, amount: Long): UUID? {
    val response = client.post<HttpResponse>("transfer") {
        body = TextContent(
            text = mapper.writeValueAsString(Transfer(from, to, amount)),
            contentType = ContentType.Application.Json
        )
    }
    return if (response.status == HttpStatusCode.OK) UUID.fromString(response.readText().trim('"'))
    else null
}
