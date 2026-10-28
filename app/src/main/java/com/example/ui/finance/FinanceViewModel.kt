package com.example.ui.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CategoryBudget
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.model.UserProfile
import com.example.data.repository.TransactionRepository
import com.example.ui.util.BudgetAlertLevel
import com.example.ui.util.FormatUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class FilterPeriod(val label: String) {
  ALL("Semua"),
  TODAY("Hari Ini"),
  THIS_MONTH("Bulan Ini"),
  THIS_YEAR("Tahun Ini"),
  CUSTOM_MONTH("Pilih Bulan")
}

enum class FilterType(val label: String) {
  ALL("Semua"),
  INCOME("Uang Masuk"),
  EXPENSE("Uang Keluar")
}

data class CategorySummary(
  val categoryName: String,
  val totalAmount: Double,
  val count: Int,
  val percentage: Float
)

data class CategoryBudgetStatus(
  val budgetId: String,
  val categoryName: String,
  val month: String = "",
  val allocatedBudget: Double,
  val spentAmount: Double,
  val remainingAmount: Double,
  val progress: Float,
  val isOverBudget: Boolean,
  val isNearLimit: Boolean = progress >= 0.8f && !isOverBudget
) {
  val alertLevel: BudgetAlertLevel
    get() = when {
      isOverBudget -> BudgetAlertLevel.OVER_BUDGET
      isNearLimit -> BudgetAlertLevel.NEAR_LIMIT
      else -> BudgetAlertLevel.SAFE
    }
}

data class BudgetAlertNotification(
  val categoryName: String,
  val alertLevel: BudgetAlertLevel,
  val spentAmount: Double,
  val allocatedBudget: Double,
  val monthLabel: String,
  val message: String
)

data class FinanceUiState(
  val transactions: List<Transaction> = emptyList(),
  val filteredTransactions: List<Transaction> = emptyList(),
  val userProfile: UserProfile? = null,
  val categoryBudgets: List<CategoryBudget> = emptyList(),
  val categoryBudgetStatuses: List<CategoryBudgetStatus> = emptyList(),
  val budgetWarnings: List<CategoryBudgetStatus> = emptyList(),
  val totalAllocatedBudget: Double = 0.0,
  val totalBudgetSpent: Double = 0.0,
  val totalBudgetRemaining: Double = 0.0,
  val totalIncome: Double = 0.0,
  val totalExpense: Double = 0.0,
  val totalBalance: Double = 0.0,
  val currentMonthExpense: Double = 0.0,
  val monthlyBudget: Double = 0.0,
  val budgetProgress: Float = 0.0f,
  val categorySummaries: List<CategorySummary> = emptyList(),
  val selectedPeriod: FilterPeriod = FilterPeriod.THIS_MONTH,
  val selectedType: FilterType = FilterType.ALL,
  val selectedCategory: String? = null,
  val searchQuery: String = "",
  val selectedBudgetMonth: String = "",
  val selectedBudgetMonthLabel: String = "",
  val customFilterMonth: String? = null,
  val customFilterMonthLabel: String? = null,
  val filterStartDate: Long? = null,
  val filterEndDate: Long? = null,
  val filterDateRangeLabel: String? = null,
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val userMessage: String? = null
)

class FinanceViewModel(
  private val repository: TransactionRepository,
  private val currentUserId: String
) : ViewModel() {

  private val initialMonthKey = run {
    val cal = Calendar.getInstance()
    FormatUtils.toMonthKey(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
  }

  private val _selectedPeriod = MutableStateFlow(FilterPeriod.ALL)
  private val _selectedType = MutableStateFlow(FilterType.ALL)
  private val _selectedCategory = MutableStateFlow<String?>(null)
  private val _searchQuery = MutableStateFlow("")
  private val _selectedBudgetMonth = MutableStateFlow(initialMonthKey)
  private val _customFilterMonth = MutableStateFlow<String?>(null)
  private val _filterStartDate = MutableStateFlow<Long?>(null)
  private val _filterEndDate = MutableStateFlow<Long?>(null)
  private val _userMessage = MutableStateFlow<String?>(null)
  private val _errorMessage = MutableStateFlow<String?>(null)

  @Volatile private var latestTransactions: List<Transaction> = emptyList()
  @Volatile private var latestBudgets: List<CategoryBudget> = emptyList()

  private val _budgetAlertEvents = MutableSharedFlow<BudgetAlertNotification>(extraBufferCapacity = 5)
  val budgetAlertEvents: SharedFlow<BudgetAlertNotification> = _budgetAlertEvents.asSharedFlow()

  private fun parseMonthKey(key: String): Pair<Int, Int> {
    val parts = key.split("-")
    val y = parts.getOrNull(0)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
    val m = (parts.getOrNull(1)?.toIntOrNull() ?: (Calendar.getInstance().get(Calendar.MONTH) + 1)) - 1
    return Pair(y, m)
  }

  val uiState: StateFlow<FinanceUiState> = combine(
    repository.observeTransactions(currentUserId).catch { emit(emptyList()) },
    repository.observeUserProfile(currentUserId).catch { emit(null) },
    repository.observeCategoryBudgets(currentUserId).catch { emit(emptyList()) },
    _selectedPeriod,
    _selectedType,
    _selectedCategory,
    _searchQuery,
    _selectedBudgetMonth,
    _customFilterMonth,
    _filterStartDate,
    _filterEndDate,
    _userMessage,
    _errorMessage
  ) { args ->
    @Suppress("UNCHECKED_CAST")
    val allTransactions = args[0] as List<Transaction>
    val profile = args[1] as? UserProfile
    @Suppress("UNCHECKED_CAST")
    val categoryBudgets = args[2] as List<CategoryBudget>
    val period = args[3] as FilterPeriod
    val type = args[4] as FilterType
    val category = args[5] as? String
    val query = args[6] as String
    val budgetMonth = args[7] as String
    val customMonth = args[8] as? String
    val startDate = args[9] as? Long
    val endDate = args[10] as? Long
    val userMsg = args[11] as? String
    val errorMsg = args[12] as? String

    latestTransactions = allTransactions
    latestBudgets = categoryBudgets

    // Overall totals
    val totalIncome = allTransactions.filter { it.isIncome }.sumOf { it.amount }
    val totalExpense = allTransactions.filter { it.isExpense }.sumOf { it.amount }
    val totalBalance = totalIncome - totalExpense

    // Current month expenses calculation (for overall dashboard stats)
    val now = Calendar.getInstance()
    val curMonth = now.get(Calendar.MONTH)
    val curYear = now.get(Calendar.YEAR)
    val curMonthKey = FormatUtils.toMonthKey(curYear, curMonth)

    val currentMonthExpenses = allTransactions.filter { tx ->
      if (!tx.isExpense) return@filter false
      val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
      cal.get(Calendar.MONTH) == curMonth && cal.get(Calendar.YEAR) == curYear
    }
    val currentMonthExpense = currentMonthExpenses.sumOf { it.amount }

    // Target month expenses for selected budget month
    val (targetBudgetYear, targetBudgetMonth) = parseMonthKey(budgetMonth)
    val targetMonthExpenses = allTransactions.filter { tx ->
      if (!tx.isExpense) return@filter false
      val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
      cal.get(Calendar.MONTH) == targetBudgetMonth && cal.get(Calendar.YEAR) == targetBudgetYear
    }

    // Filter budgets that belong to this selected month (or legacy budgets with empty month if viewing current month)
    val activeMonthBudgets = categoryBudgets.filter { b ->
      b.month == budgetMonth || (b.month.isBlank() && budgetMonth == curMonthKey)
    }

    // Category-specific budget calculations for the selected month
    val categoryBudgetStatuses = activeMonthBudgets.map { budget ->
      val spentForCat = targetMonthExpenses
        .filter { it.category.equals(budget.category, ignoreCase = true) }
        .sumOf { it.amount }
      val remaining = budget.amount - spentForCat
      val progress = if (budget.amount > 0) (spentForCat / budget.amount).toFloat() else 0f
      val isOver = spentForCat > budget.amount
      val isNear = progress >= 0.8f && !isOver
      CategoryBudgetStatus(
        budgetId = budget.id,
        categoryName = budget.category,
        month = budgetMonth,
        allocatedBudget = budget.amount,
        spentAmount = spentForCat,
        remainingAmount = remaining,
        progress = progress,
        isOverBudget = isOver,
        isNearLimit = isNear
      )
    }.sortedByDescending { it.allocatedBudget }

    val totalAllocated = activeMonthBudgets.sumOf { it.amount }
    val totalBudgetSpent = categoryBudgetStatuses.sumOf { it.spentAmount }
    val totalBudgetRemaining = totalAllocated - totalBudgetSpent

    // Overall monthly budget from profile (or sum of categorized budgets if not explicitly set)
    val monthlyBudget = if (profile?.monthlyBudget != null && profile.monthlyBudget > 0) {
      profile.monthlyBudget
    } else {
      totalAllocated
    }

    val budgetProgress = if (monthlyBudget > 0.0) {
      (currentMonthExpense / monthlyBudget).toFloat().coerceIn(0f, 1f)
    } else 0f

    // Filtered transactions
    val filtered = allTransactions.filter { tx ->
      val matchesDate = when {
        startDate != null && endDate != null -> tx.date in startDate..endDate
        startDate != null -> tx.date >= startDate
        endDate != null -> tx.date <= endDate
        else -> when (period) {
          FilterPeriod.ALL -> true
          FilterPeriod.TODAY -> {
            val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
            cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) &&
              cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
          }
          FilterPeriod.THIS_MONTH -> {
            val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
            cal.get(Calendar.MONTH) == curMonth && cal.get(Calendar.YEAR) == curYear
          }
          FilterPeriod.THIS_YEAR -> {
            val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
            cal.get(Calendar.YEAR) == curYear
          }
          FilterPeriod.CUSTOM_MONTH -> {
            if (customMonth.isNullOrBlank()) {
              true
            } else {
              val (mYear, mMonth) = parseMonthKey(customMonth)
              val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
              cal.get(Calendar.MONTH) == mMonth && cal.get(Calendar.YEAR) == mYear
            }
          }
        }
      }

      val matchesType = when (type) {
        FilterType.ALL -> true
        FilterType.INCOME -> tx.isIncome
        FilterType.EXPENSE -> tx.isExpense
      }

      val matchesCategory = category == null || tx.category.equals(category, ignoreCase = true)

      val matchesQuery = query.isBlank() ||
        tx.title.contains(query, ignoreCase = true) ||
        tx.category.contains(query, ignoreCase = true) ||
        tx.note.contains(query, ignoreCase = true) ||
        tx.account.contains(query, ignoreCase = true) ||
        tx.amount.toLong().toString().contains(query.filter { it.isDigit() })

      matchesDate && matchesType && matchesCategory && matchesQuery
    }

    // Category breakdown for expenses in filtered set
    val expenseInFiltered = filtered.filter { it.isExpense }
    val sumExpenseInFiltered = expenseInFiltered.sumOf { it.amount }
    val categorySummaries = expenseInFiltered
      .groupBy { it.category }
      .map { (catName, list) ->
        val catTotal = list.sumOf { it.amount }
        val pct = if (sumExpenseInFiltered > 0.0) (catTotal / sumExpenseInFiltered).toFloat() else 0f
        CategorySummary(
          categoryName = catName,
          totalAmount = catTotal,
          count = list.size,
          percentage = pct
        )
      }
      .sortedByDescending { it.totalAmount }

    FinanceUiState(
      transactions = allTransactions,
      filteredTransactions = filtered,
      userProfile = profile,
      categoryBudgets = activeMonthBudgets,
      categoryBudgetStatuses = categoryBudgetStatuses,
      budgetWarnings = categoryBudgetStatuses.filter { it.alertLevel != BudgetAlertLevel.SAFE },
      totalAllocatedBudget = totalAllocated,
      totalBudgetSpent = totalBudgetSpent,
      totalBudgetRemaining = totalBudgetRemaining,
      totalIncome = totalIncome,
      totalExpense = totalExpense,
      totalBalance = totalBalance,
      currentMonthExpense = currentMonthExpense,
      monthlyBudget = monthlyBudget,
      budgetProgress = budgetProgress,
      categorySummaries = categorySummaries,
      selectedPeriod = period,
      selectedType = type,
      selectedCategory = category,
      searchQuery = query,
      selectedBudgetMonth = budgetMonth,
      selectedBudgetMonthLabel = FormatUtils.formatMonthLabel(budgetMonth),
      customFilterMonth = customMonth,
      customFilterMonthLabel = customMonth?.let { FormatUtils.formatMonthLabel(it) },
      filterStartDate = startDate,
      filterEndDate = endDate,
      filterDateRangeLabel = if (startDate != null && endDate != null) FormatUtils.formatDateRange(startDate, endDate) else null,
      isLoading = false,
      userMessage = userMsg,
      errorMessage = errorMsg
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = FinanceUiState(isLoading = true)
  )

  fun setPeriod(period: FilterPeriod) {
    _selectedPeriod.value = period
  }

  fun setCustomFilterMonth(monthKey: String) {
    _customFilterMonth.value = monthKey
    _selectedPeriod.value = FilterPeriod.CUSTOM_MONTH
  }

  fun setDateRange(startDateMillis: Long, endDateMillis: Long) {
    _filterStartDate.value = startDateMillis
    _filterEndDate.value = endDateMillis
  }

  fun clearDateRange() {
    _filterStartDate.value = null
    _filterEndDate.value = null
  }

  fun setType(type: FilterType) {
    _selectedType.value = type
  }

  fun setCategory(category: String?) {
    _selectedCategory.value = category
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun resetFilters() {
    _selectedPeriod.value = FilterPeriod.ALL
    _selectedType.value = FilterType.ALL
    _selectedCategory.value = null
    _searchQuery.value = ""
    _filterStartDate.value = null
    _filterEndDate.value = null
    _customFilterMonth.value = null
  }

  fun clearMessages() {
    _userMessage.value = null
    _errorMessage.value = null
  }

  private fun checkBudgetAlertOnTransaction(
    category: String,
    expenseAmount: Double,
    txDate: Long
  ) {
    val txCal = Calendar.getInstance().apply { timeInMillis = txDate }
    val txMonthKey = FormatUtils.toMonthKey(txCal.get(Calendar.YEAR), txCal.get(Calendar.MONTH))
    val txMonthLabel = FormatUtils.formatMonthLabel(txMonthKey)

    val budget = latestBudgets.firstOrNull {
      it.category.equals(category, ignoreCase = true) &&
        (it.month == txMonthKey || (it.month.isBlank() && txMonthKey == FormatUtils.currentMonthKey()))
    } ?: return

    if (budget.amount <= 0.0) return

    val currentSpentForCat = latestTransactions
      .filter { tx ->
        tx.isExpense &&
          tx.category.equals(category, ignoreCase = true) &&
          run {
            val c = Calendar.getInstance().apply { timeInMillis = tx.date }
            c.get(Calendar.MONTH) == txCal.get(Calendar.MONTH) && c.get(Calendar.YEAR) == txCal.get(Calendar.YEAR)
          }
      }
      .sumOf { it.amount }

    val newTotalSpent = currentSpentForCat + expenseAmount
    val ratio = newTotalSpent / budget.amount
    val alertLevel = when {
      ratio >= 1.0 -> BudgetAlertLevel.OVER_BUDGET
      ratio >= 0.8 -> BudgetAlertLevel.NEAR_LIMIT
      else -> BudgetAlertLevel.SAFE
    }

    if (alertLevel != BudgetAlertLevel.SAFE) {
      val message = when (alertLevel) {
        BudgetAlertLevel.OVER_BUDGET -> {
          val over = newTotalSpent - budget.amount
          "🚨 Budget $category Terlampaui! Pengeluaran telah melampaui limit anggaran sebesar ${FormatUtils.formatRupiah(over)}."
        }
        BudgetAlertLevel.NEAR_LIMIT -> {
          val remaining = (budget.amount - newTotalSpent).coerceAtLeast(0.0)
          "⚠️ Budget $category Mendekati Limit! Sudah ${(ratio * 100).toInt()}% terpakai. Sisa: ${FormatUtils.formatRupiah(remaining)}."
        }
        BudgetAlertLevel.SAFE -> ""
      }

      _budgetAlertEvents.tryEmit(
        BudgetAlertNotification(
          categoryName = category,
          alertLevel = alertLevel,
          spentAmount = newTotalSpent,
          allocatedBudget = budget.amount,
          monthLabel = txMonthLabel,
          message = message
        )
      )
    }
  }

  fun addTransaction(
    type: TransactionType,
    amount: Double,
    category: String,
    title: String,
    note: String,
    date: Long,
    account: String,
    onComplete: (Boolean) -> Unit
  ) {
    viewModelScope.launch {
      val tx = Transaction(
        userId = currentUserId,
        type = type.name,
        amount = amount,
        category = category,
        title = title,
        note = note,
        date = date,
        account = account
      )
      val result = repository.addTransaction(tx)
      if (result.isSuccess) {
        _userMessage.value = "Transaksi berhasil disimpan!"
        if (type == TransactionType.EXPENSE) {
          checkBudgetAlertOnTransaction(category, amount, date)
        }
        onComplete(true)
      } else {
        _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Gagal menambah transaksi"
        onComplete(false)
      }
    }
  }

  fun updateTransaction(
    transaction: Transaction,
    onComplete: (Boolean) -> Unit
  ) {
    viewModelScope.launch {
      val result = repository.updateTransaction(transaction)
      if (result.isSuccess) {
        _userMessage.value = "Transaksi berhasil diperbarui!"
        if (transaction.isExpense) {
          checkBudgetAlertOnTransaction(transaction.category, 0.0, transaction.date)
        }
        onComplete(true)
      } else {
        _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Gagal memperbarui transaksi"
        onComplete(false)
      }
    }
  }

  fun deleteTransaction(transactionId: String) {
    viewModelScope.launch {
      val result = repository.deleteTransaction(transactionId)
      if (result.isSuccess) {
        _userMessage.value = "Transaksi berhasil dihapus"
      } else {
        _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Gagal menghapus transaksi"
      }
    }
  }

  fun setSelectedBudgetMonth(monthKey: String) {
    _selectedBudgetMonth.value = monthKey
  }

  fun nextBudgetMonth() {
    val (y, m) = parseMonthKey(_selectedBudgetMonth.value)
    val cal = Calendar.getInstance().apply {
      set(Calendar.YEAR, y)
      set(Calendar.MONTH, m)
      add(Calendar.MONTH, 1)
    }
    _selectedBudgetMonth.value = FormatUtils.toMonthKey(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
  }

  fun prevBudgetMonth() {
    val (y, m) = parseMonthKey(_selectedBudgetMonth.value)
    val cal = Calendar.getInstance().apply {
      set(Calendar.YEAR, y)
      set(Calendar.MONTH, m)
      add(Calendar.MONTH, -1)
    }
    _selectedBudgetMonth.value = FormatUtils.toMonthKey(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
  }

  fun saveCategoryBudget(
    category: String,
    amount: Double,
    month: String = _selectedBudgetMonth.value,
    oldBudgetId: String? = null,
    oldCategory: String? = null,
    oldMonth: String? = null
  ) {
    viewModelScope.launch {
      val targetMonth = month.ifBlank { _selectedBudgetMonth.value }
      if (!oldCategory.isNullOrBlank() && (oldCategory != category || (!oldMonth.isNullOrBlank() && oldMonth != targetMonth))) {
        repository.deleteCategoryBudget(oldBudgetId ?: "", oldCategory, oldMonth ?: targetMonth)
      }
      val result = repository.saveCategoryBudget(category, amount, targetMonth)
      if (result.isSuccess) {
        val monthLabel = FormatUtils.formatMonthLabel(targetMonth)
        _userMessage.value = "Budget $category untuk bulan $monthLabel berhasil disimpan!"
      } else {
        _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Gagal menyimpan budget"
      }
    }
  }

  fun deleteCategoryBudget(budgetId: String, categoryName: String, month: String = _selectedBudgetMonth.value) {
    viewModelScope.launch {
      val result = repository.deleteCategoryBudget(budgetId, categoryName, month)
      if (result.isSuccess) {
        _userMessage.value = "Budget untuk $categoryName telah dihapus"
      } else {
        _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Gagal menghapus budget"
      }
    }
  }

  fun saveBudget(monthlyBudget: Double) {
    viewModelScope.launch {
      val currentProfile = uiState.value.userProfile ?: UserProfile(userId = currentUserId)
      val updated = currentProfile.copy(monthlyBudget = monthlyBudget)
      val result = repository.saveUserProfile(updated)
      if (result.isSuccess) {
        _userMessage.value = "Target anggaran berhasil diperbarui!"
      } else {
        _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Gagal menyimpan anggaran"
      }
    }
  }
}
