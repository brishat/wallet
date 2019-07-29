package com.revolut.wallet.core.transfer

import com.revolut.wallet.core.account.Account
import com.revolut.wallet.core.account.AccountService
import com.revolut.wallet.core.transaction.Transaction
import com.revolut.wallet.core.transaction.TransactionProcessor
import com.revolut.wallet.core.transaction.TransactionService
import com.revolut.wallet.core.transaction.TransactionStatus
import com.revolut.wallet.exception.WalletException
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.util.UUID

@ExtendWith(MockKExtension::class)
class TransferServiceTest {

    @MockK
    private lateinit var accountService: AccountService
    @MockK
    private lateinit var transactionService: TransactionService
    @MockK
    private lateinit var transactionProcessor: TransactionProcessor
    @InjectMockKs
    private lateinit var transferService: TransferService

    @BeforeEach
    fun beforeEach() {
        every { accountService.getAccount(ACCOUNT_1.id) } returns ACCOUNT_1
        every { accountService.getAccount(ACCOUNT_2.id) } returns ACCOUNT_2

        every { transactionService.createTransaction(any()) } returns TRANSACTION

        coEvery { transactionProcessor.creditFromAccount(TRANSACTION) } just Runs
        coEvery { transactionProcessor.debitToAccount(TRANSACTION) } just Runs
    }

    @Test
    fun `'transfer' should create transaction`() {
        val transaction = runBlocking { transferService.transfer(TRANSFER) }

        assertEquals(TRANSACTION, transaction)

        coVerify { transactionProcessor.creditFromAccount(TRANSACTION) }
        coVerify { transactionProcessor.debitToAccount(TRANSACTION) }
    }

    @Test
    fun `'transfer' should't create transaction, if from_account not exist`() {
        every { accountService.getAccount(ACCOUNT_1.id) } throws WalletException("")

        assertThrows<WalletException> {
            runBlocking { transferService.transfer(TRANSFER) }
        }

        coVerify(inverse = true) { transactionProcessor.creditFromAccount(TRANSACTION) }
        coVerify(inverse = true) { transactionProcessor.debitToAccount(TRANSACTION) }
    }

    @Test
    fun `'transfer' should't create transaction, if to_account not exist`() {
        every { accountService.getAccount(ACCOUNT_2.id) } throws WalletException("")

        assertThrows<WalletException> {
            runBlocking { transferService.transfer(TRANSFER) }
        }

        coVerify(inverse = true) { transactionProcessor.creditFromAccount(TRANSACTION) }
        coVerify(inverse = true) { transactionProcessor.debitToAccount(TRANSACTION) }
    }

    @Test
    fun `'transfer' should't create transaction, if from_account = to_account`() {
        val transfer = TRANSFER.copy(fromId = ACCOUNT_1.id, toId = ACCOUNT_1.id)
        assertThrows<WalletException> {
            runBlocking { transferService.transfer(transfer) }
        }

        coVerify(inverse = true) { transactionProcessor.creditFromAccount(TRANSACTION) }
        coVerify(inverse = true) { transactionProcessor.debitToAccount(TRANSACTION) }
    }

    @Test
    fun `'transfer' should't create transaction, if amount less than 0`() {
        val transfer = TRANSFER.copy(amount = BigDecimal.valueOf(-100))
        assertThrows<WalletException> {
            runBlocking { transferService.transfer(transfer) }
        }

        coVerify(inverse = true) { transactionProcessor.creditFromAccount(TRANSACTION) }
        coVerify(inverse = true) { transactionProcessor.debitToAccount(TRANSACTION) }
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
        private val TRANSFER = TransferDto(
            fromId = ACCOUNT_1.id,
            toId = ACCOUNT_2.id,
            amount = BigDecimal.valueOf(2)
        )
    }
}