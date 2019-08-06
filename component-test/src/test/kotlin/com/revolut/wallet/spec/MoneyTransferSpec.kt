package com.revolut.wallet.spec

import com.fasterxml.jackson.module.kotlin.readValue
import com.revolut.wallet.core.Account
import com.revolut.wallet.core.Transaction
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.DescribeSpec
import io.ktor.client.request.get
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.http.HttpStatusCode

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

            context("Check account 1 balance change") {
                val result = client.get<String>("account/${account1.id}")
                val account = mapper.readValue(result, Account::class.java)
                it("Account 1 balance changed") {
                    account.id shouldBe account1.id
                    account.balance shouldBe 50.0
                }
            }
            context("Check account 2 balance change") {
                val result = client.get<String>("account/${account2.id}")
                val account = mapper.readValue(result, Account::class.java)
                it("Account 2 balance changed") {
                    account.id shouldBe account2.id
                    account.balance shouldBe 50.0
                }
            }

            context("Check transaction for account1") {
                val getTransactionsUrl = "account/${account1.id}/transaction/all"
                val result = client.get<HttpResponse>(getTransactionsUrl)
                it("Transaction is applied to account1") {
                    result.status shouldBe HttpStatusCode.OK

                    val transactions: List<Transaction> = mapper.readValue(result.readText())

                    transactions.first().accountId shouldBe account1.id
                    transactions.first().transactionId shouldBe transactionId
                    transactions.first().debit shouldBe 0.0
                    transactions.first().credit shouldBe 50.0
                }
            }

            context("Check transaction for account2") {
                val getTransactionsUrl = "account/${account2.id}/transaction/$transactionId"
                val result = client.get<HttpResponse>(getTransactionsUrl)
                it("Transaction is applied to account2") {
                    result.status shouldBe HttpStatusCode.OK

                    val transaction: Transaction = mapper.readValue(result.readText())

                    transaction.accountId shouldBe account2.id
                    transaction.transactionId shouldBe transactionId
                    transaction.debit shouldBe 50.0
                    transaction.credit shouldBe 0.0
                }
            }
        }

        context("Transfer 150 from account1 should fail") {
            val transactionId = transfer(account1, account2, 150)
            it("Transfer 150 from account1 failed") {
                transactionId shouldBe null
            }
        }
    }
})
