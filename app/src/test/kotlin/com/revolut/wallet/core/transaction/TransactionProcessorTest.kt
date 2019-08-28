package com.revolut.wallet.core.transaction

import com.revolut.wallet.core.account.Account
import com.revolut.wallet.core.account.AccountService
import com.revolut.wallet.exception.OptmisticLockException
import com.revolut.wallet.exception.WalletException
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.util.UUID

@ExtendWith(MockKExtension::class)
class TransactionProcessorTest {

    @MockK
    private lateinit var accountService: AccountService
    @MockK
    private lateinit var transactionService: TransactionService
    @InjectMockKs
    private lateinit var transactionProcessor: TransactionProcessor

    @BeforeEach
    fun beforeEach() {
        coEvery { accountService.getAccount(ACCOUNT_1.id) } returns ACCOUNT_1
        coEvery { accountService.getAccount(ACCOUNT_2.id) } returns ACCOUNT_2
    }

    @Test
    fun `'creditFromAccount' should take money from account`() {
        coEvery { transactionService.createCreditTransactionLog(TRANSACTION, ACCOUNT_1) } just Runs

        runBlocking { transactionProcessor.creditFromAccount(TRANSACTION) }

        coVerify { transactionService.createCreditTransactionLog(TRANSACTION, ACCOUNT_1) }
    }

    @Test
    fun `'creditFromAccount' shouldn't take money from account, if balance not enough`() {
        coEvery { accountService.getAccount(ACCOUNT_1.id) } returns ACCOUNT_1.copy(balance = BigDecimal.ZERO)

        assertThrows<WalletException> {
            runBlocking { transactionProcessor.creditFromAccount(TRANSACTION) }
        }

        coVerify(inverse = true) { transactionService.createCreditTransactionLog(TRANSACTION, ACCOUNT_1) }
    }

    @Test
    fun `'creditFromAccount' shouldn't take money from account, if can not credit`() {
        coEvery { transactionService.createCreditTransactionLog(TRANSACTION, ACCOUNT_1) } throws OptmisticLockException()

        assertThrows<WalletException> {
            runBlocking { transactionProcessor.creditFromAccount(TRANSACTION) }
        }
    }

    @Test
    fun `'debitToAccount' should put money to account`() {
        coEvery { transactionService.createDebitTransactionLog(TRANSACTION, ACCOUNT_2) } just Runs

        runBlocking { transactionProcessor.debitToAccount(TRANSACTION) }

        coVerify { transactionService.createDebitTransactionLog(TRANSACTION, ACCOUNT_2) }
    }

    @Test
    fun `'debitToAccount' should rollback money, if can not debit`() {
        coEvery { transactionService.createDebitTransactionLog(TRANSACTION, ACCOUNT_2) } throws OptmisticLockException()
        coEvery { transactionService.createRollbackTransactionLog(TRANSACTION, ACCOUNT_1) } just runs

        runBlocking { transactionProcessor.debitToAccount(TRANSACTION) }

        coVerify { transactionService.createRollbackTransactionLog(TRANSACTION, ACCOUNT_1) }
        coVerify(inverse = true) { transactionService.createCreditTransactionLog(TRANSACTION, ACCOUNT_2) }
    }

    companion object {
        private val ACCOUNT_1 = Account(
            id = UUID.randomUUID(),
            balance = BigDecimal.valueOf(10),
            version = DateTime.now()
        )
        private val ACCOUNT_2 = Account(
            id = UUID.randomUUID(),
            balance = BigDecimal.valueOf(5),
            version = DateTime.now()
        )
        private val TRANSACTION = Transaction(
            id = UUID.randomUUID(),
            fromAccountId = ACCOUNT_1.id,
            toAccountId = ACCOUNT_2.id,
            amount = BigDecimal.valueOf(2),
            status = TransactionStatus.IN_PROGRESS
        )
    }
}
