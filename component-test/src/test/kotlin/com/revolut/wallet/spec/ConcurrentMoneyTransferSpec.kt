package com.revolut.wallet.spec

import com.revolut.wallet.core.Account
import io.kotlintest.matchers.numerics.shouldBeLessThanOrEqual
import io.kotlintest.shouldBe
import io.kotlintest.specs.DescribeSpec
import io.ktor.client.request.get
import kotlinx.coroutines.delay
import mu.KotlinLogging

@Suppress("BlockingMethodInNonBlockingContext")
class ConcurrentMoneyTransferSpec : DescribeSpec({
    val logger = KotlinLogging.logger {}

    describe("Concurrent money transfer") {
        val account1 = createAccount(100.0)
        val account2 = createAccount(100.0)
        val account3 = createAccount()
        val overallBalance = listOf(account1, account2, account3).sumByDouble { it.balance }

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
                account.balance shouldBe 100.0
            }
        }

        context("Check account 3") {
            val result = client.get<String>("account/${account3.id}")
            val account = mapper.readValue(result, Account::class.java)
            it("Account 3 created") {
                account.id shouldBe account3.id
                account.balance shouldBe 0.0
            }
        }

        context("Transfer 60 from account1 to account2 and from account1 to account3") {

            it("should create transaction").config(invocations = 50, parallelism = 5) {
                val transaction1Id = transfer(account1, account3, 1)
                val transaction2Id = transfer(account2, account3, 1)
                logger.info { transaction1Id }
                logger.info { transaction2Id }
                listOfNotNull(transaction1Id, transaction2Id).size shouldBeLessThanOrEqual 2
            }

            delay(5000)

            context("Check account 3 balance change") {
                val result1 = client.get<String>("account/${account1.id}")
                val account1Balance = mapper.readValue(result1, Account::class.java).balance

                val result2 = client.get<String>("account/${account2.id}")
                val account2Balance = mapper.readValue(result2, Account::class.java).balance

                val result3 = client.get<String>("account/${account3.id}")
                val account3Balance = mapper.readValue(result3, Account::class.java).balance

                it("Overall amount of money should be $overallBalance") {
                    (account1Balance + account2Balance + account3Balance) shouldBe overallBalance
                }
                logger.info { "${account1.id}: $account1Balance" }
                logger.info { "${account2.id}: $account2Balance" }
                logger.info { "${account3.id}: $account3Balance" }
            }
        }
    }
})
