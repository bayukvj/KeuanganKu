package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.data.model.CategoryBudget
import com.example.data.model.Transaction
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TransactionRepository(
  private val db: FirebaseFirestore
) {
  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  private val auth = Firebase.auth

  private fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
  }

  fun observeTransactions(userId: String = requireUserId()): Flow<List<Transaction>> = callbackFlow {
    val path = "users/$userId/transactions"
    val collectionRef = db.collection("users").document(userId).collection("transactions")
      .orderBy("date", Query.Direction.DESCENDING)

    val listener = collectionRef.addSnapshotListener { snapshot, error ->
      if (error != null) {
        handleFirestoreError(error, OperationType.LIST, path)
        close(error)
        return@addSnapshotListener
      }
      if (snapshot != null) {
        val transactions = snapshot.documents.mapNotNull { doc ->
          doc.toObject(Transaction::class.java)?.copy(id = doc.id)
        }
        trySend(transactions)
      }
    }

    awaitClose {
      listener.remove()
    }
  }

  suspend fun addTransaction(transaction: Transaction): Result<String> {
    val uid = requireUserId()
    val collectionRef = db.collection("users").document(uid).collection("transactions")
    val docRef = if (transaction.id.isNotBlank()) collectionRef.document(transaction.id) else collectionRef.document()
    val payload = mapOf(
      "userId" to uid,
      "type" to transaction.type,
      "amount" to transaction.amount,
      "category" to transaction.category,
      "title" to transaction.title,
      "note" to transaction.note,
      "date" to transaction.date,
      "account" to transaction.account,
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp()
    )

    return try {
      docRef.set(payload).await()
      Result.success(docRef.id)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun updateTransaction(transaction: Transaction): Result<Unit> {
    val uid = requireUserId()
    require(transaction.id.isNotBlank()) { "Transaction ID must not be blank" }
    val docRef = db.collection("users").document(uid).collection("transactions").document(transaction.id)
    val updatePayload = mapOf(
      "type" to transaction.type,
      "amount" to transaction.amount,
      "category" to transaction.category,
      "title" to transaction.title,
      "note" to transaction.note,
      "date" to transaction.date,
      "account" to transaction.account,
      "updatedAt" to FieldValue.serverTimestamp()
    )

    return try {
      docRef.update(updatePayload).await()
      Result.success(Unit)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun deleteTransaction(transactionId: String): Result<Unit> {
    val uid = requireUserId()
    require(transactionId.isNotBlank()) { "Transaction ID must not be blank" }
    val docRef = db.collection("users").document(uid).collection("transactions").document(transactionId)
    return try {
      docRef.delete().await()
      Result.success(Unit)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      Result.failure(e)
    }
  }

  fun observeUserProfile(userId: String = requireUserId()): Flow<UserProfile?> = callbackFlow {
    val path = "users/$userId"
    val docRef = db.collection("users").document(userId)
    val listener = docRef.addSnapshotListener { snapshot, error ->
      if (error != null) {
        handleFirestoreError(error, OperationType.GET, path)
        close(error)
        return@addSnapshotListener
      }
      if (snapshot != null && snapshot.exists()) {
        trySend(snapshot.toObject(UserProfile::class.java)?.copy(id = snapshot.id))
      } else {
        trySend(null)
      }
    }

    awaitClose {
      listener.remove()
    }
  }

  suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid)
    val docSnapshot = docRef.get().await()

    return try {
      if (!docSnapshot.exists()) {
        val payload = mapOf(
          "userId" to uid,
          "displayName" to profile.displayName,
          "email" to profile.email,
          "monthlyBudget" to profile.monthlyBudget,
          "currency" to profile.currency,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
      } else {
        val updatePayload = mapOf(
          "displayName" to profile.displayName,
          "email" to profile.email,
          "monthlyBudget" to profile.monthlyBudget,
          "currency" to profile.currency,
          "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.update(updatePayload).await()
      }
      Result.success(Unit)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      Result.failure(e)
    }
  }

  // --- Category Budget Methods ---

  fun observeCategoryBudgets(userId: String = requireUserId()): Flow<List<CategoryBudget>> = callbackFlow {
    val path = "users/$userId/budgets"
    val collectionRef = db.collection("users").document(userId).collection("budgets")

    val listener = collectionRef.addSnapshotListener { snapshot, error ->
      if (error != null) {
        handleFirestoreError(error, OperationType.LIST, path)
        close(error)
        return@addSnapshotListener
      }
      if (snapshot != null) {
        val budgets = snapshot.documents.mapNotNull { doc ->
          doc.toObject(CategoryBudget::class.java)?.copy(id = doc.id)
        }
        trySend(budgets)
      }
    }

    awaitClose {
      listener.remove()
    }
  }

  suspend fun saveCategoryBudget(category: String, amount: Double, month: String = ""): Result<String> {
    val uid = requireUserId()
    val cleanMonth = month.trim()
    val safeCat = category.lowercase().replace(Regex("[^a-z0-9_]"), "_").take(25)
    val safeCatId = if (cleanMonth.isNotBlank()) {
      "b_" + cleanMonth.replace("-", "_") + "_" + safeCat
    } else {
      "budget_" + safeCat
    }
    val docRef = db.collection("users").document(uid).collection("budgets").document(safeCatId)

    return try {
      val existing = docRef.get().await()
      if (!existing.exists()) {
        val payload = mutableMapOf<String, Any>(
          "userId" to uid,
          "category" to category,
          "amount" to amount,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp()
        )
        if (cleanMonth.isNotBlank()) {
          payload["month"] = cleanMonth
        }
        docRef.set(payload).await()
      } else {
        val updatePayload = mutableMapOf<String, Any>(
          "category" to category,
          "amount" to amount,
          "updatedAt" to FieldValue.serverTimestamp()
        )
        if (cleanMonth.isNotBlank()) {
          updatePayload["month"] = cleanMonth
        }
        docRef.update(updatePayload).await()
      }
      Result.success(docRef.id)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun deleteCategoryBudget(budgetId: String, categoryName: String = "", month: String = ""): Result<Unit> {
    val uid = requireUserId()
    return try {
      val collectionRef = db.collection("users").document(uid).collection("budgets")

      // 1. Delete directly by budgetId if provided
      if (budgetId.isNotBlank()) {
        try {
          collectionRef.document(budgetId).delete().await()
        } catch (_: Exception) {}
      }

      val cleanMonth = month.trim()
      val safeCat = categoryName.lowercase().replace(Regex("[^a-z0-9_]"), "_").take(25)

      // 2. Delete by computed ID variants
      if (safeCat.isNotBlank()) {
        val altId1 = if (cleanMonth.isNotBlank()) "b_" + cleanMonth.replace("-", "_") + "_" + safeCat else "budget_" + safeCat
        val altId2 = "budget_" + safeCat
        if (altId1 != budgetId) {
          try { collectionRef.document(altId1).delete().await() } catch (_: Exception) {}
        }
        if (altId2 != budgetId) {
          try { collectionRef.document(altId2).delete().await() } catch (_: Exception) {}
        }
      }

      // 3. Scan and delete all matching documents in the collection to ensure no lingering/orphan budgets remain
      try {
        val snapshot = collectionRef.get().await()
        val targetCatLower = categoryName.trim().lowercase()
        val targetMonthFormatted = cleanMonth.replace("-", "_")
        for (doc in snapshot.documents) {
          val docCat = (doc.getString("category") ?: "").trim().lowercase()
          val docMonth = (doc.getString("month") ?: "").trim()
          val idMatches = budgetId.isNotBlank() && doc.id == budgetId
          val categoryMatches = targetCatLower.isNotBlank() && (
            docCat == targetCatLower ||
            docCat == safeCat ||
            doc.id.endsWith(safeCat)
          )
          val monthMatches = cleanMonth.isBlank() || docMonth.isBlank() || docMonth == cleanMonth || doc.id.contains(targetMonthFormatted)

          if (idMatches || (categoryMatches && monthMatches)) {
            try {
              doc.reference.delete().await()
            } catch (_: Exception) {}
          }
        }
      } catch (_: Exception) {}

      Result.success(Unit)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, "users/$uid/budgets/$budgetId")
      Result.failure(e)
    }
  }
}
