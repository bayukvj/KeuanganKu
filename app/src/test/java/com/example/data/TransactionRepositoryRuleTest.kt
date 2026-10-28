package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.repository.TransactionRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class TransactionRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun createTransaction_validPayload_createsDocumentAndReturnsId() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repository = TransactionRepository(firestore)

    val tx = Transaction(
      id = "tx_" + UUID.randomUUID().toString().take(8),
      userId = uid,
      type = TransactionType.EXPENSE.name,
      amount = 75000.0,
      category = "Belanja",
      title = "Belanja Mingguan",
      note = "Sayur dan buah",
      date = System.currentTimeMillis()
    )

    val result = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.addTransaction(tx)
    }
    assertTrue("Transaction creation should succeed", result.isSuccess)
    val txId = result.getOrThrow()
    assertTrue("Transaction ID should not be empty", txId.isNotEmpty())
  }

  @Test
  fun observeTransactions_authenticatedOwner_emitsRealtimeUpdates() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repository = TransactionRepository(firestore)

    val txId = "tx_" + UUID.randomUUID().toString().take(8)
    val tx = Transaction(
      id = txId,
      userId = uid,
      type = TransactionType.INCOME.name,
      amount = 5000000.0,
      category = "Gaji",
      title = "Gaji Bulanan",
      date = System.currentTimeMillis()
    )

    repository.addTransaction(tx)

    val list = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeTransactions(uid).first { txList: List<Transaction> ->
        txList.any { item -> item.id == txId }
      }
    }
    assertTrue("Observed list should contain new transaction", list.any { it.id == txId })
  }

  @Test
  fun deleteTransaction_authenticatedOwner_removesDocument() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repository = TransactionRepository(firestore)

    val txId = "tx_" + UUID.randomUUID().toString().take(8)
    val tx = Transaction(
      id = txId,
      userId = uid,
      type = TransactionType.EXPENSE.name,
      amount = 20000.0,
      category = "Transportasi",
      title = "Bensin Motor",
      date = System.currentTimeMillis()
    )

    repository.addTransaction(tx)
    val deleteResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.deleteTransaction(txId)
    }
    assertTrue("Delete should succeed", deleteResult.isSuccess)
  }

  @Test
  fun observeTransactions_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
    auth.signOut()
    val repository = TransactionRepository(firestore)

    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        repository.observeTransactions("dummy_user").first()
      }
      fail("Expected exception when querying unauthenticated")
    } catch (e: Exception) {
      val firestoreException = e.unwrapFirestoreException()
      assertNotNull("Expected nested FirebaseFirestoreException", firestoreException)
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
    }
  }

  @Test
  fun saveAndObserveCategoryBudget_authenticatedOwner_succeeds() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repository = TransactionRepository(firestore)

    val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.saveCategoryBudget("Belanja_" + UUID.randomUUID().toString().take(6), 1500000.0)
    }
    if (saveResult.isFailure) {
      System.err.println("SAVE_BUDGET_FAILED: " + saveResult.exceptionOrNull()?.message)
      saveResult.exceptionOrNull()?.printStackTrace()
    }
    assertTrue("Save category budget should succeed: " + saveResult.exceptionOrNull()?.message, saveResult.isSuccess)

    val budgets = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeCategoryBudgets(uid).first { list ->
        list.any { it.category.startsWith("Belanja_") && it.amount == 1500000.0 }
      }
    }
    assertTrue("Observed category budgets should contain Belanja", budgets.any { it.category.startsWith("Belanja_") })
  }

  @Test
  fun saveAndObserveCategoryBudget_withMonth_succeeds() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repository = TransactionRepository(firestore)

    val catName = "Pendidikan_" + UUID.randomUUID().toString().take(6)
    val targetMonth = "2026-10"
    val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.saveCategoryBudget(catName, 2500000.0, targetMonth)
    }
    assertTrue("Save category budget with month should succeed: " + saveResult.exceptionOrNull()?.message, saveResult.isSuccess)

    val budgets = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeCategoryBudgets(uid).first { list ->
        list.any { it.category == catName && it.amount == 2500000.0 && it.month == targetMonth }
      }
    }
    assertTrue("Observed category budgets should contain Pendidikan for target month", budgets.any { it.category == catName && it.month == targetMonth })
  }

  private companion object {
    const val ALICE_EMAIL = "alice_finance@test.com"
    const val DEFAULT_TIMEOUT_MS = 6000L
    const val FLOW_TIMEOUT_MS = 6000L
  }
}
