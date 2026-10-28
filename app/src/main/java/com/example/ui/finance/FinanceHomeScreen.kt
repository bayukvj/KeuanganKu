package com.example.ui.finance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import com.example.ui.settings.SettingsDialog
import com.example.ui.theme.LocalThemePreferences
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.ThemePreferences
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import com.example.ui.util.BudgetNotificationHelper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.model.Transaction
import com.example.ui.model.CategoryRegistry
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedContainer
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenContainer
import com.example.ui.theme.OnExpenseRed
import com.example.ui.theme.OnIncomeGreen
import com.example.ui.util.FormatUtils

enum class FinanceHomeTab(val label: String, val icon: ImageVector) {
  DASHBOARD("Beranda", Icons.Default.Dashboard),
  HISTORY("Riwayat", Icons.Default.ReceiptLong)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceHomeScreen(
  viewModel: FinanceViewModel,
  themePreferences: ThemePreferences = LocalThemePreferences.current,
  onSignOut: () -> Unit
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }

  var showAddSheet by remember { mutableStateOf(false) }
  var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }
  var selectedDetailTransaction by remember { mutableStateOf<Transaction?>(null) }
  var showCategoryBudgetSheet by remember { mutableStateOf(false) }
  var showNotificationCenter by remember { mutableStateOf(false) }
  var showSettingsDialog by remember { mutableStateOf(false) }
  var showSignOutConfirm by remember { mutableStateOf(false) }
  var showDeleteConfirm by remember { mutableStateOf<Transaction?>(null) }
  var showTopMoreMenu by remember { mutableStateOf(false) }

  // Android 13+ Notification Permission Launcher
  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { /* Handled gracefully */ }

  LaunchedEffect(Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    }
  }

  // Collect budget alert events and trigger system notification + in-app snackbar
  LaunchedEffect(Unit) {
    viewModel.budgetAlertEvents.collect { alert ->
      BudgetNotificationHelper.sendBudgetAlert(
        context = context,
        categoryName = alert.categoryName,
        alertLevel = alert.alertLevel,
        spentAmount = alert.spentAmount,
        allocatedBudget = alert.allocatedBudget,
        monthLabel = alert.monthLabel
      )

      val result = snackbarHostState.showSnackbar(
        message = alert.message,
        actionLabel = "Lihat Budget",
        duration = SnackbarDuration.Long
      )
      if (result == SnackbarResult.ActionPerformed) {
        showCategoryBudgetSheet = true
      }
    }
  }

  LaunchedEffect(uiState.userMessage) {
    uiState.userMessage?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearMessages()
    }
  }

  LaunchedEffect(uiState.errorMessage) {
    uiState.errorMessage?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearMessages()
    }
  }

  var selectedTab by remember { mutableStateOf(FinanceHomeTab.DASHBOARD) }

  // Handle back button when on HISTORY tab
  if (selectedTab == FinanceHomeTab.HISTORY) {
    BackHandler {
      selectedTab = FinanceHomeTab.DASHBOARD
    }
  }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      if (selectedTab == FinanceHomeTab.DASHBOARD) {
        TopAppBar(
          title = {
            Column(modifier = Modifier.padding(end = 4.dp)) {
              Text(
                text = "KeuanganKu",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = uiState.userProfile?.displayName?.ifBlank { "Pencatatan & Budgeting" } ?: "Pencatatan & Budgeting",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
              )
            }
          },
          actions = {
            IconButton(
              onClick = { selectedTab = FinanceHomeTab.HISTORY },
              modifier = Modifier.testTag("top_search_button")
            ) {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Cari Riwayat Transaksi",
                tint = MaterialTheme.colorScheme.primary
              )
            }
            // Notification Center Bell with Badge for Budget Warnings
            IconButton(
              onClick = { showNotificationCenter = true },
              modifier = Modifier.testTag("notification_center_button")
            ) {
              BadgedBox(
                badge = {
                  if (uiState.budgetWarnings.isNotEmpty()) {
                    Badge(
                      containerColor = if (uiState.budgetWarnings.any { it.isOverBudget }) ExpenseRed else Color(0xFFD97706)
                    ) {
                      Text(uiState.budgetWarnings.size.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                }
              ) {
                Icon(
                  imageVector = if (uiState.budgetWarnings.isNotEmpty()) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                  contentDescription = "Peringatan Budget",
                  tint = if (uiState.budgetWarnings.isNotEmpty()) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
            // Overflow Menu containing Budgeting, Pengaturan & Keluar Akun
            Box {
              IconButton(
                onClick = { showTopMoreMenu = true },
                modifier = Modifier.testTag("top_more_menu_button")
              ) {
                Icon(
                  imageVector = Icons.Default.MoreVert,
                  contentDescription = "Menu Lainnya",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              DropdownMenu(
                expanded = showTopMoreMenu,
                onDismissRequest = { showTopMoreMenu = false }
              ) {
                DropdownMenuItem(
                  text = { Text("Budgeting Kategori", fontWeight = FontWeight.Medium) },
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.Default.Savings,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary
                    )
                  },
                  onClick = {
                    showTopMoreMenu = false
                    showCategoryBudgetSheet = true
                  },
                  modifier = Modifier.testTag("budget_button")
                )
                DropdownMenuItem(
                  text = { Text("Pengaturan & Tema", fontWeight = FontWeight.Medium) },
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.Default.Settings,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  },
                  onClick = {
                    showTopMoreMenu = false
                    showSettingsDialog = true
                  },
                  modifier = Modifier.testTag("settings_button")
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                DropdownMenuItem(
                  text = { Text("Keluar Akun", color = ExpenseRed, fontWeight = FontWeight.Medium) },
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.Default.ExitToApp,
                      contentDescription = null,
                      tint = ExpenseRed
                    )
                  },
                  onClick = {
                    showTopMoreMenu = false
                    showSignOutConfirm = true
                  },
                  modifier = Modifier.testTag("sign_out_button")
                )
              }
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
          )
        )
      }
    },
    bottomBar = {
      NavigationBar(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
      ) {
        NavigationBarItem(
          selected = selectedTab == FinanceHomeTab.DASHBOARD,
          onClick = { selectedTab = FinanceHomeTab.DASHBOARD },
          icon = { Icon(Icons.Default.Dashboard, contentDescription = "Beranda") },
          label = { Text("Beranda") },
          modifier = Modifier.testTag("nav_tab_dashboard")
        )
        NavigationBarItem(
          selected = selectedTab == FinanceHomeTab.HISTORY,
          onClick = { selectedTab = FinanceHomeTab.HISTORY },
          icon = {
            BadgedBox(
              badge = {
                if (uiState.filteredTransactions.isNotEmpty()) {
                  Badge { Text("${uiState.filteredTransactions.size}") }
                }
              }
            ) {
              Icon(Icons.Default.ReceiptLong, contentDescription = "Riwayat Transaksi")
            }
          },
          label = { Text("Riwayat") },
          modifier = Modifier.testTag("nav_tab_history")
        )
      }
    },
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = {
          transactionToEdit = null
          showAddSheet = true
        },
        modifier = Modifier.testTag("add_transaction_fab"),
        icon = { Icon(Icons.Default.Add, contentDescription = null) },
        text = { Text("Catat Transaksi", fontWeight = FontWeight.Bold) },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
      )
    }
  ) { paddingValues ->
    if (selectedTab == FinanceHomeTab.HISTORY) {
      TransactionHistoryScreen(
        uiState = uiState,
        onSearchQueryChanged = { viewModel.setSearchQuery(it) },
        onCategorySelected = { viewModel.setCategory(it) },
        onTypeSelected = { viewModel.setType(it) },
        onPeriodSelected = { viewModel.setPeriod(it) },
        onDateRangeSelected = { start, end -> viewModel.setDateRange(start, end) },
        onClearDateRange = { viewModel.clearDateRange() },
        onCustomMonthSelected = { viewModel.setCustomFilterMonth(it) },
        onResetFilters = { viewModel.resetFilters() },
        onTransactionClicked = { selectedDetailTransaction = it },
        onBackToHome = { selectedTab = FinanceHomeTab.DASHBOARD },
        modifier = Modifier.padding(paddingValues)
      )
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues),
        contentPadding = PaddingValues(bottom = 96.dp)
      ) {
        // 1. Hero Total Balance & Cashflow Card
        item {
          BalanceHeroCard(
            totalBalance = uiState.totalBalance,
            totalIncome = uiState.totalIncome,
            totalExpense = uiState.totalExpense
          )
        }

        // Budget Warning Alert Banner (approaching or exceeding limit)
        if (uiState.budgetWarnings.isNotEmpty()) {
          item {
            BudgetAlertBanner(
              warnings = uiState.budgetWarnings,
              selectedMonthLabel = uiState.selectedBudgetMonthLabel,
              onManageBudgets = { showCategoryBudgetSheet = true },
              onOpenNotificationCenter = { showNotificationCenter = true },
              modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
          }
        }

        // 2. Category-based Budgeting Cards (User Requested Feature)
        item {
          CategoryBudgetDashboardSection(
            statuses = uiState.categoryBudgetStatuses,
            selectedMonthLabel = uiState.selectedBudgetMonthLabel,
            onPrevMonth = { viewModel.prevBudgetMonth() },
            onNextMonth = { viewModel.nextBudgetMonth() },
            onManageBudgets = { showCategoryBudgetSheet = true },
            selectedCategory = uiState.selectedCategory,
            onSelectCategory = { cat ->
              if (uiState.selectedCategory == cat) viewModel.setCategory(null) else viewModel.setCategory(cat)
            }
          )
        }

        // 3. Category Breakdown (Grafik Pengeluaran Bulanan per Kategori)
        if (uiState.categorySummaries.isNotEmpty()) {
          item {
            MonthlyExpenseChartsSection(
              summaries = uiState.categorySummaries,
              totalExpense = uiState.totalExpense,
              selectedCategory = uiState.selectedCategory,
              onCategorySelected = { cat ->
                if (uiState.selectedCategory == cat) {
                  viewModel.setCategory(null)
                } else {
                  viewModel.setCategory(cat)
                }
              }
            )
          }
        }

        // 4. Quick Search Affordance on Dashboard
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 20.dp, vertical = 6.dp)
              .clickable { selectedTab = FinanceHomeTab.HISTORY }
              .testTag("dashboard_search_entry"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Text(
                text = if (uiState.searchQuery.isNotBlank()) "Pencarian aktif: \"${uiState.searchQuery}\"" else "Cari riwayat pengeluaran & pemasukan...",
                style = MaterialTheme.typography.bodyMedium,
                color = if (uiState.searchQuery.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "Buka Riwayat",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
        }

        // 5. Transactions Header
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Transaksi Terbaru (${uiState.transactions.size})",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(
              onClick = { selectedTab = FinanceHomeTab.HISTORY },
              modifier = Modifier.testTag("view_all_history_button")
            ) {
              Text("Lihat Semua & Cari", fontWeight = FontWeight.Bold)
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }

        // 6. Recent Transactions List
        val recentTransactions = uiState.filteredTransactions.take(8)
        if (recentTransactions.isEmpty()) {
          item {
            EmptyTransactionsState(
              hasFilter = uiState.selectedCategory != null || uiState.searchQuery.isNotBlank() || uiState.selectedType != FilterType.ALL,
              onAddNew = {
                transactionToEdit = null
                showAddSheet = true
              }
            )
          }
        } else {
          items(
            items = recentTransactions,
            key = { it.id }
          ) { transaction ->
            TransactionItemCard(
              transaction = transaction,
              onClick = { selectedDetailTransaction = transaction }
            )
          }

          if (uiState.filteredTransactions.size > 8) {
            item {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
              ) {
                OutlinedButton(
                  onClick = { selectedTab = FinanceHomeTab.HISTORY },
                  modifier = Modifier.testTag("more_transactions_button")
                ) {
                  Text("Lihat Semua (${uiState.filteredTransactions.size}) di Riwayat")
                }
              }
            }
          }
        }
      }
    }
  }

  // Add / Edit Transaction Sheet
  if (showAddSheet) {
    AddEditTransactionSheet(
      transactionToEdit = transactionToEdit,
      categoryBudgetStatuses = uiState.categoryBudgetStatuses,
      onDismiss = { showAddSheet = false },
      onSave = { type, amount, category, title, note, date, account ->
        if (transactionToEdit != null) {
          viewModel.updateTransaction(
            transactionToEdit!!.copy(
              type = type.name,
              amount = amount,
              category = category,
              title = title,
              note = note,
              date = date,
              account = account
            )
          ) { success ->
            if (success) showAddSheet = false
          }
        } else {
          viewModel.addTransaction(
            type = type,
            amount = amount,
            category = category,
            title = title,
            note = note,
            date = date,
            account = account
          ) { success ->
            if (success) showAddSheet = false
          }
        }
      }
    )
  }

  // Category Budget Manager Sheet
  if (showCategoryBudgetSheet) {
    CategoryBudgetSheet(
      selectedMonth = uiState.selectedBudgetMonth,
      selectedMonthLabel = uiState.selectedBudgetMonthLabel,
      budgetStatuses = uiState.categoryBudgetStatuses,
      onPrevMonth = { viewModel.prevBudgetMonth() },
      onNextMonth = { viewModel.nextBudgetMonth() },
      onSelectMonth = { viewModel.setSelectedBudgetMonth(it) },
      onSaveBudget = { category, amount, month ->
        viewModel.saveCategoryBudget(category, amount, month)
      },
      onDeleteBudget = { budgetId, categoryName ->
        viewModel.deleteCategoryBudget(budgetId, categoryName)
      },
      onDismiss = { showCategoryBudgetSheet = false }
    )
  }

  // Budget Notification Center Dialog
  if (showNotificationCenter) {
    BudgetNotificationCenterDialog(
      warnings = uiState.budgetWarnings,
      selectedMonthLabel = uiState.selectedBudgetMonthLabel,
      onDismiss = { showNotificationCenter = false },
      onManageBudgets = {
        showNotificationCenter = false
        showCategoryBudgetSheet = true
      }
    )
  }

  // App Settings Dialog (Dark Mode / Light Mode)
  if (showSettingsDialog) {
    val currentThemeMode by themePreferences.themeMode.collectAsState()
    SettingsDialog(
      currentThemeMode = currentThemeMode,
      onThemeModeSelected = { themePreferences.setThemeMode(it) },
      userEmail = uiState.userProfile?.email,
      onDismiss = { showSettingsDialog = false }
    )
  }

  // Transaction Detail Sheet
  selectedDetailTransaction?.let { tx ->
    TransactionDetailSheet(
      transaction = tx,
      onDismiss = { selectedDetailTransaction = null },
      onEdit = {
        selectedDetailTransaction = null
        transactionToEdit = tx
        showAddSheet = true
      },
      onDelete = {
        selectedDetailTransaction = null
        showDeleteConfirm = tx
      }
    )
  }

  // Delete Confirmation Dialog
  showDeleteConfirm?.let { tx ->
    AlertDialog(
      onDismissRequest = { showDeleteConfirm = null },
      title = { Text("Hapus Transaksi?") },
      text = { Text("Apakah Anda yakin ingin menghapus catatan transaksi \"${tx.title}\" senilai ${FormatUtils.formatRupiah(tx.amount)}?") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteTransaction(tx.id)
            showDeleteConfirm = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
        ) {
          Text("Hapus")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteConfirm = null }) {
          Text("Batal")
        }
      }
    )
  }

  // Sign Out Confirmation Dialog
  if (showSignOutConfirm) {
    AlertDialog(
      onDismissRequest = { showSignOutConfirm = false },
      title = { Text("Keluar dari Akun?") },
      text = { Text("Data keuangan dan anggaran Anda tersimpan dengan aman di cloud dan dapat diakses kembali saat Anda masuk kembali.") },
      confirmButton = {
        Button(
          onClick = {
            showSignOutConfirm = false
            onSignOut()
          }
        ) {
          Text("Keluar")
        }
      },
      dismissButton = {
        TextButton(onClick = { showSignOutConfirm = false }) {
          Text("Batal")
        }
      }
    )
  }
}

@Composable
private fun BalanceHeroCard(
  totalBalance: Double,
  totalIncome: Double,
  totalExpense: Double
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 8.dp),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.primary
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.linearGradient(
            colors = listOf(
              MaterialTheme.colorScheme.primary,
              MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
            )
          )
        )
        .padding(22.dp)
    ) {
      Column {
        Text(
          text = "Total Saldo Kas",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = FormatUtils.formatRupiah(totalBalance),
          style = MaterialTheme.typography.headlineLarge,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onPrimary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Income & Expense Breakdown Pills
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Uang Masuk Pill
          Surface(
            modifier = Modifier.weight(1f),
            color = Color.White.copy(alpha = 0.18f),
            shape = RoundedCornerShape(16.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(IncomeGreen),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.ArrowUpward,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(18.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Uang Masuk",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                )
                Text(
                  text = FormatUtils.formatRupiah(totalIncome),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }

          // Uang Keluar Pill
          Surface(
            modifier = Modifier.weight(1f),
            color = Color.White.copy(alpha = 0.18f),
            shape = RoundedCornerShape(16.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(ExpenseRed),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.ArrowDownward,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(18.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Uang Keluar",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                )
                Text(
                  text = FormatUtils.formatRupiah(totalExpense),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CategoryBudgetDashboardSection(
  statuses: List<CategoryBudgetStatus>,
  selectedMonthLabel: String,
  onPrevMonth: () -> Unit,
  onNextMonth: () -> Unit,
  onManageBudgets: () -> Unit,
  selectedCategory: String?,
  onSelectCategory: (String) -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 6.dp),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Header Row with Title and Action Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f, fill = false)
        ) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Savings,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Budget Kategori",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
          )
        }

        FilledTonalButton(
          onClick = onManageBudgets,
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          modifier = Modifier.testTag("manage_budgets_btn")
        ) {
          Icon(
            imageVector = if (statuses.isNotEmpty()) Icons.Default.Tune else Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (statuses.isNotEmpty()) "Kelola" else "Atur Budget",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Dedicated Month Switcher Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
          .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onPrevMonth,
          modifier = Modifier.size(32.dp).testTag("dash_budget_prev_month")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Bulan Lalu",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onManageBudgets)
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Bulan: $selectedMonthLabel",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }

        IconButton(
          onClick = onNextMonth,
          modifier = Modifier.size(32.dp).testTag("dash_budget_next_month")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Bulan Depan",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
          )
        }
      }

      if (statuses.isEmpty()) {
        Spacer(modifier = Modifier.height(10.dp))
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
            .padding(14.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "Belum ada budget untuk bulan $selectedMonthLabel",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Tetapkan batas anggaran belanja atau kebutuhan agar pengeluaran terkontrol.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedButton(
            onClick = onManageBudgets,
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Tetapkan Budget Kategori", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      } else {
        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal scrolling cards of categorized budgets with remaining amount (Sisa Budget)
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(statuses) { item ->
            val cat = CategoryRegistry.getCategory(item.categoryName)
            val isOver = item.isOverBudget
            val isSelected = selectedCategory.equals(item.categoryName, ignoreCase = true)
            val barColor = when {
              isOver -> ExpenseRed
              item.progress > 0.8f -> Color(0xFFF59E0B)
              else -> IncomeGreen
            }

            Surface(
              modifier = Modifier
                .width(200.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable { onSelectCategory(item.categoryName) },
              shape = RoundedCornerShape(14.dp),
              color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(
                    modifier = Modifier
                      .size(28.dp)
                      .clip(CircleShape)
                      .background(cat.color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = cat.icon,
                      contentDescription = null,
                      tint = cat.color,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = item.categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                  text = "SISA BUDGET",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                  color = if (isOver) ExpenseRed else barColor
                )

                Text(
                  text = if (item.remainingAmount >= 0) {
                    FormatUtils.formatRupiah(item.remainingAmount)
                  } else {
                    "Over " + FormatUtils.formatRupiah(-item.remainingAmount)
                  },
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.ExtraBold,
                  color = if (isOver) ExpenseRed else barColor,
                  maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                  progress = { item.progress.coerceIn(0f, 1f) },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                  color = barColor,
                  trackColor = MaterialTheme.colorScheme.surface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = "Pakai: ${FormatUtils.formatRupiah(item.spentAmount)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = "${(item.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = barColor
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}





@Composable
private fun TransactionItemCard(
  transaction: Transaction,
  onClick: () -> Unit
) {
  val catItem = CategoryRegistry.getCategory(transaction.category)
  val isIncome = transaction.isIncome

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 5.dp)
      .clickable(onClick = onClick)
      .testTag("transaction_item_${transaction.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Category Icon Badge
      Box(
        modifier = Modifier
          .size(46.dp)
          .clip(CircleShape)
          .background(catItem.color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = catItem.icon,
          contentDescription = transaction.category,
          tint = catItem.color,
          modifier = Modifier.size(24.dp)
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      // Title, Category, and Account Badges
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = transaction.title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(3.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
          ) {
            Text(
              text = transaction.category,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          if (transaction.account.isNotBlank()) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ) {
              Text(
                text = transaction.account,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Prominent Date Badge with Calendar Icon
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            imageVector = Icons.Default.CalendarToday,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.primary
          )
          Text(
            text = FormatUtils.formatDateTime(transaction.date),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Amount
      val amountFormatted = (if (isIncome) "+ " else "- ") + FormatUtils.formatRupiah(transaction.amount)
      val amountColor = if (isIncome) IncomeGreen else ExpenseRed

      Text(
        text = amountFormatted,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = amountColor,
        maxLines = 1
      )
    }
  }
}

@Composable
private fun EmptyTransactionsState(
  hasFilter: Boolean,
  onAddNew: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(32.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(72.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (hasFilter) Icons.Default.FilterList else Icons.Default.Savings,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(36.dp)
        )
      }
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = if (hasFilter) "Tidak Ada Transaksi Sesuai Filter" else "Belum Ada Catatan Transaksi",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = if (hasFilter) "Coba sesuaikan kata kunci, periode, atau kategori filter Anda." else "Mulai kelola keuangan Anda dengan mencatat uang masuk atau uang keluar pertama Anda.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
      )
      if (!hasFilter) {
        Spacer(modifier = Modifier.height(16.dp))
        Button(
          onClick = onAddNew,
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("+ Catat Transaksi Baru")
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionDetailSheet(
  transaction: Transaction,
  onDismiss: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  val catItem = CategoryRegistry.getCategory(transaction.category)
  val isIncome = transaction.isIncome

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = MaterialTheme.colorScheme.surface
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
        .padding(bottom = 32.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Detail Transaksi",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Tutup")
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Amount Card
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isIncome) IncomeGreenContainer else ExpenseRedContainer
      ) {
        Column(
          modifier = Modifier.padding(18.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = if (isIncome) "Uang Masuk (Pemasukan)" else "Uang Keluar (Pengeluaran)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isIncome) OnIncomeGreen else OnExpenseRed
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = (if (isIncome) "+ " else "- ") + FormatUtils.formatRupiah(transaction.amount),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = if (isIncome) OnIncomeGreen else OnExpenseRed
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Details Table
      DetailRow(label = "Keterangan", value = transaction.title)
      DetailRow(label = "Kategori", value = transaction.category)
      DetailRow(label = "Akun / Sumber Dana", value = transaction.account)
      DetailRow(label = "Waktu Transaksi", value = FormatUtils.formatDateTime(transaction.date))
      if (transaction.note.isNotBlank()) {
        DetailRow(label = "Catatan", value = transaction.note)
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Button(
          onClick = onEdit,
          modifier = Modifier
            .weight(1f)
            .height(48.dp),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Edit")
        }

        Button(
          onClick = onDelete,
          modifier = Modifier
            .weight(1f)
            .height(48.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
        ) {
          Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Hapus")
        }
      }
    }
  }
}

@Composable
private fun DetailRow(
  label: String,
  value: String
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp)
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}
