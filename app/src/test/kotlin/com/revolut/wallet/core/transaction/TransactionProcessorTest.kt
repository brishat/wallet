package com.revolut.wallet.core.transaction

import com.revolut.wallet.core.account.Account
import com.revolut.wallet.core.account.AccountService
import com.revolut.wallet.exception.WalletException
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.runBlocking
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
        every { accountService.getAccount(ACCOUNT_1.id) } returns ACCOUNT_1
        every { accountService.getAccount(ACCOUNT_2.id) } returns ACCOUNT_2

        every { accountService.lockAccount(any()) } just Runs
        every { accountService.unlockAccount(any()) } just Runs
    }

    @Test
    fun `'creditFromAccount' should take money from account`() {
        every { transactionService.createCreditTransactionLog(TRANSACTION, ACCOUNT_1) } just Runs

        runBlocking { transactionProcessor.creditFromAccount(TRANSACTION) }

        verify { transactionService.createCreditTransactionLog(TRANSACTION, ACCOUNT_1) }
        verify { accountService.unlockAccount(ACCOUNT_1.id) }
    }

    @Test
    fun `'creditFromAccount' shouldn't take money from account, if balance not enough`() {
        every { accountService.getAccount(ACCOUNT_1.id) } returns ACCOUNT_1.copy(balance = BigDecimal.ZERO)

        assertThrows<WalletException> {
            runBlocking { transactionProcessor.creditFromAccount(TRANSACTION) }
        }

        verify(inverse = true) { transactionService.createCreditTransactionLog(TRANSACTION, ACCOUNT_1) }
        verify { accountService.unlockAccount(ACCOUNT_1.id) }
    }

    @Test
    fun `'creditFromAccount' shouldn't take money from account, if can not lock`() {
        every { accountService.lockAccount(ACCOUNT_1.id) } throws WalletException("")

        assertThrows<WalletException> {
            runBlocking { transactionProcessor.creditFromAccount(TRANSACTION) }
        }

        verify(inverse = true) { transactionService.createCreditTransactionLog(TRANSACTION, ACCOUNT_1) }
        verify(inverse = true) { accountService.unlockAccount(ACCOUNT_1.id) }
    }

    @Test
    fun `'debitToAccount' should put money to account`() {
        every { transactionService.createDebitTransactionLog(TRANSACTION, ACCOUNT_2) } just Runs

        runBlocking { transactionProcessor.debitToAccount(TRANSACTION) }

        verify { transactionService.createDebitTransactionLog(TRANSACTION, ACCOUNT_2) }
        verify { accountService.unlockAccount(ACCOUNT_2.id) }
    }

    @Test
    fun `'debitToAccount' should rollback money, if can not lock`() {
        every { accountService.lockAccount(ACCOUNT_2.id) } throws WalletException("")
        coEvery { transactionService.createRollbackTransactionLog(TRANSACTION, ACCOUNT_1) } just runs

        runBlocking { transactionProcessor.debitToAccount(TRANSACTION) }

        coVerify { transactionService.createRollbackTransactionLog(TRANSACTION, ACCOUNT_1) }
        verify(inverse = true) { transactionService.createCreditTransactionLog(TRANSACTION, ACCOUNT_2) }
        verify(inverse = true) { accountService.unlockAccount(ACCOUNT_2.id) }
    }

    companion object {
        private val ACCOUNT_1 = Account(
            id = UUID.randomUUID(),
            balance = BigDecimal.valueOf(10),
            locked = false
        )
        private val ACCOUNT_2 = Account(
            id = UUID.randomUUID(),
            balance = BigDecimal.valueOf(5),
            locked = false
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
