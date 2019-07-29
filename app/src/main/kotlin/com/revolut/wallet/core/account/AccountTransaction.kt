package com.revolut.wallet.core.account

import com.revolut.wallet.core.transaction.TransactionTable
import java.math.BigDecimal
import java.util.UUID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

data class AccountTransaction(
    val accountId: UUID,
    val transactionId: UUID,
    val debit: BigDecimal,
    val credit: BigDecimal
)

object AccountTransactionTable : Table() {
    val accountId = uuid("account_id") references AccountTable.id
    val transactionId = uuid("transaction_id") references TransactionTable.id
    val debit = decimal("debit", 10, 2)
    val credit = decimal("credit", 10, 2)
}

fun ResultRow.toAccountTransaction() = AccountTransaction(
    accountId = this[AccountTransactionTable.accountId],
    transactionId = this[AccountTransactionTable.transactionId],
    debit = this[AccountTransactionTable.debit],
    credit = this[AccountTransactionTable.credit]
)
