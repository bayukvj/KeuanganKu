package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class TransactionType {
  INCOME,
  EXPENSE
}

@IgnoreExtraProperties
data class Transaction(
  @DocumentId
  val id: String = "",
  val userId: String = "",
  val type: String = TransactionType.EXPENSE.name,
  val amount: Double = 0.0,
  val category: String = "",
  val title: String = "",
  val note: String = "",
  val date: Long = System.currentTimeMillis(),
  val account: String = "Tunai",
  @ServerTimestamp
  val createdAt: Timestamp? = null,
  @ServerTimestamp
  val updatedAt: Timestamp? = null
) {
  val isIncome: Boolean get() = type == TransactionType.INCOME.name
  val isExpense: Boolean get() = type == TransactionType.EXPENSE.name
}

@IgnoreExtraProperties
data class UserProfile(
  @DocumentId
  val id: String = "",
  val userId: String = "",
  val displayName: String = "",
  val email: String = "",
  val monthlyBudget: Double = 0.0,
  val currency: String = "IDR",
  @ServerTimestamp
  val createdAt: Timestamp? = null,
  @ServerTimestamp
  val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CategoryBudget(
  @DocumentId
  val id: String = "",
  val userId: String = "",
  val category: String = "",
  val amount: Double = 0.0,
  val month: String = "", // e.g. "2026-10" (YYYY-MM)
  @ServerTimestamp
  val createdAt: Timestamp? = null,
  @ServerTimestamp
  val updatedAt: Timestamp? = null
)
