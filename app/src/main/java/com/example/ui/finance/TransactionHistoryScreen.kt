package com.example.ui.finance

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.ui.model.CategoryRegistry
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedContainer
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenContainer
import com.example.ui.theme.OnExpenseRed
import com.example.ui.theme.OnIncomeGreen
import com.example.ui.util.FormatUtils

@Composable
fun TransactionHistoryScreen(
  uiState: FinanceUiState,
  onSearchQueryChanged: (String) -> Unit,
  onCategorySelected: (String?) -> Unit,
  onTypeSelected: (FilterType) -> Unit,
  onPeriodSelected: (FilterPeriod) -> Unit = {},
  onDateRangeSelected: (startDateMillis: Long, endDateMillis: Long) -> Unit = { _, _ -> },
  onClearDateRange: () -> Unit = {},
  onCustomMonthSelected: (String) -> Unit = {},
  onResetFilters: () -> Unit,
  onTransactionClicked: (Transaction) -> Unit,
  onBackToHome: () -> Unit,
  modifier: Modifier = Modifier
) {
  // BackHandler to satisfy Android guidelines when in sub-screen
  BackHandler {
    onBackToHome()
  }

  val focusManager = LocalFocusManager.current
  val searchFocusRequester = remember { FocusRequester() }
  var showDateRangePickerDialog by remember { mutableStateOf(false) }
  var showCustomMonthPickerDialog by remember { mutableStateOf(false) }

  val filteredTransactions = uiState.filteredTransactions
  val isAnyFilterActive = uiState.searchQuery.isNotBlank() ||
    uiState.selectedCategory != null ||
    uiState.selectedType != FilterType.ALL ||
    uiState.filterDateRangeLabel != null ||
    uiState.selectedPeriod != FilterPeriod.ALL

  val filteredIncome = filteredTransactions.filter { it.isIncome }.sumOf { it.amount }
  val filteredExpense = filteredTransactions.filter { it.isExpense }.sumOf { it.amount }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    // 1. Top Search Header with Navigation & Clear
    Surface(
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 2.dp,
      shadowElevation = 2.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onBackToHome,
            modifier = Modifier.testTag("history_back_button")
          ) {
            Icon(
              imageVector = Icons.Default.ArrowBack,
              contentDescription = "Kembali ke Beranda"
            )
          }

          Spacer(modifier = Modifier.width(4.dp))

          // Top Search Bar
          OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
              .weight(1f)
              .focusRequester(searchFocusRequester)
              .testTag("transaction_search_bar"),
            placeholder = {
              Text(
                text = "Cari pengeluaran, pemasukan, kategori...",
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Cari",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
              )
            },
            trailingIcon = {
              AnimatedVisibility(
                visible = uiState.searchQuery.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
              ) {
                IconButton(
                  onClick = { onSearchQueryChanged("") },
                  modifier = Modifier.testTag("clear_search_button")
                ) {
                  Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Hapus teks pencarian",
                    modifier = Modifier.size(20.dp)
                  )
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
              unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
              focusedBorderColor = MaterialTheme.colorScheme.primary,
              unfocusedBorderColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Transaction Type Tabs (Semua, Uang Masuk, Uang Keluar)
        TabRow(
          selectedTabIndex = uiState.selectedType.ordinal,
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ) {
          FilterType.values().forEach { type ->
            val isSelected = uiState.selectedType == type
            Tab(
              selected = isSelected,
              onClick = { onTypeSelected(type) },
              modifier = Modifier.testTag("type_tab_${type.name.lowercase()}"),
              text = {
                Text(
                  text = type.label,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 13.sp
                )
              }
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Quick Category Filter Chips Carousel
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // "Semua Kategori" Chip
          FilterChip(
            selected = uiState.selectedCategory == null,
            onClick = { onCategorySelected(null) },
            label = { Text("Semua Kategori", fontSize = 12.sp) },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
            },
            shape = RoundedCornerShape(12.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primary,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
              selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.testTag("category_chip_all")
          )

          // Individual Category Chips with Custom Colors & Icons
          CategoryRegistry.defaultCategories.forEach { categoryItem ->
            val isSelected = uiState.selectedCategory.equals(categoryItem.name, ignoreCase = true)
            FilterChip(
              selected = isSelected,
              onClick = {
                if (isSelected) {
                  onCategorySelected(null)
                } else {
                  onCategorySelected(categoryItem.name)
                }
              },
              label = { Text(categoryItem.name, fontSize = 12.sp) },
              leadingIcon = {
                Icon(
                  imageVector = categoryItem.icon,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                  tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else categoryItem.color
                )
              },
              shape = RoundedCornerShape(12.dp),
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = categoryItem.color,
                selectedLabelColor = Color.White,
                selectedLeadingIconColor = Color.White
              ),
              modifier = Modifier.testTag("category_chip_${categoryItem.name.lowercase().replace(" ", "_")}")
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Komponen DateRangePicker Fleksibel (Menggantikan filter statis Hari/Bulan/Tahun)
        Surface(
          color = if (uiState.filterDateRangeLabel != null) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
          } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { showDateRangePickerDialog = true }
            .testTag("date_range_picker_surface")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(
                    if (uiState.filterDateRangeLabel != null) {
                      MaterialTheme.colorScheme.primary
                    } else {
                      MaterialTheme.colorScheme.surfaceVariant
                    }
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.DateRange,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp),
                  tint = if (uiState.filterDateRangeLabel != null) {
                    MaterialTheme.colorScheme.onPrimary
                  } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                  }
                )
              }

              Spacer(modifier = Modifier.width(10.dp))

              Column {
                Text(
                  text = "Rentang Tanggal (DateRangePicker)",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontSize = 11.sp
                )
                Text(
                  text = uiState.filterDateRangeLabel ?: "Semua Tanggal (Klik untuk atur rentang)",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = if (uiState.filterDateRangeLabel != null) FontWeight.Bold else FontWeight.Medium,
                  color = if (uiState.filterDateRangeLabel != null) {
                    MaterialTheme.colorScheme.primary
                  } else {
                    MaterialTheme.colorScheme.onSurface
                  },
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              if (uiState.filterDateRangeLabel != null) {
                IconButton(
                  onClick = onClearDateRange,
                  modifier = Modifier
                    .size(32.dp)
                    .testTag("clear_date_range_btn")
                ) {
                  Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Hapus Rentang Tanggal",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                Spacer(modifier = Modifier.width(4.dp))
              }

              FilledTonalButton(
                onClick = { showDateRangePickerDialog = true },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier
                  .height(34.dp)
                  .testTag("open_date_range_picker_btn")
              ) {
                Icon(
                  imageVector = Icons.Default.DateRange,
                  contentDescription = null,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (uiState.filterDateRangeLabel != null) "Ubah" else "Atur Rentang",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
          }
        }
      }
    }

    // 5. Results Subtotal Summary Bar
    Surface(
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Ditemukan ${filteredTransactions.size} transaksi",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          if (filteredTransactions.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              if (filteredExpense > 0) {
                Text(
                  text = "Keluar: -${FormatUtils.formatRupiah(filteredExpense)}",
                  style = MaterialTheme.typography.labelSmall,
                  color = ExpenseRed,
                  fontWeight = FontWeight.SemiBold
                )
              }
              if (filteredIncome > 0) {
                Text(
                  text = "Masuk: +${FormatUtils.formatRupiah(filteredIncome)}",
                  style = MaterialTheme.typography.labelSmall,
                  color = IncomeGreen,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
          }
        }

        if (isAnyFilterActive) {
          TextButton(
            onClick = onResetFilters,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier.testTag("reset_filters_button")
          ) {
            Icon(
              imageVector = Icons.Default.RestartAlt,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Reset Filter", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // 6. Transactions List or Empty State
    if (filteredTransactions.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
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
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (uiState.searchQuery.isNotBlank()) Icons.Default.Search else Icons.Default.ReceiptLong,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(36.dp)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = if (uiState.searchQuery.isNotBlank()) {
              "Tidak ada transaksi ditemukan untuk \"${uiState.searchQuery}\""
            } else if (uiState.selectedCategory != null) {
              "Belum ada transaksi di kategori \"${uiState.selectedCategory}\""
            } else {
              "Belum ada riwayat transaksi"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "Coba gunakan kata kunci lain, pilih kategori berbeda, atau reset filter pencarian.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
          )

          if (isAnyFilterActive) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
              onClick = onResetFilters,
              modifier = Modifier.testTag("empty_state_reset_button")
            ) {
              Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Reset Semua Filter")
            }
          }
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(
          items = filteredTransactions,
          key = { it.id }
        ) { transaction ->
          val catItem = CategoryRegistry.getCategory(transaction.category)
          val isIncome = transaction.isIncome

          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onTransactionClicked(transaction) }
              .testTag("history_item_${transaction.id}"),
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
              // Category Icon
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

              // Title, Account, Category Badge
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = transaction.title.ifBlank { transaction.category },
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  // Category Pill
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = catItem.color.copy(alpha = 0.12f)
                  ) {
                    Text(
                      text = transaction.category,
                      style = MaterialTheme.typography.labelSmall,
                      color = catItem.color,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }

                  // Account Pill
                  if (transaction.account.isNotBlank()) {
                    Surface(
                      shape = RoundedCornerShape(6.dp),
                      color = MaterialTheme.colorScheme.surfaceVariant
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

                // Prominent Date Badge with Calendar Icon
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                  modifier = Modifier.padding(top = 4.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.primary
                  )
                  Text(
                    text = FormatUtils.formatDateTime(transaction.date),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                  )
                }

                if (transaction.note.isNotBlank()) {
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = transaction.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }

              Spacer(modifier = Modifier.width(8.dp))

              // Amount & Date
              Column(horizontalAlignment = Alignment.End) {
                val sign = if (isIncome) "+" else "-"
                val amountColor = if (isIncome) IncomeGreen else ExpenseRed

                Text(
                  text = "$sign${FormatUtils.formatRupiah(transaction.amount)}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.ExtraBold,
                  color = amountColor,
                  maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                  text = FormatUtils.formatDate(transaction.date),
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Medium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    }
  }

  if (showDateRangePickerDialog) {
    AppDateRangePickerDialog(
      initialStartDateMillis = uiState.filterStartDate,
      initialEndDateMillis = uiState.filterEndDate,
      onDismiss = { showDateRangePickerDialog = false },
      onDateRangeSelected = { start, end ->
        showDateRangePickerDialog = false
        onDateRangeSelected(start, end)
      }
    )
  }

  if (showCustomMonthPickerDialog) {
    MonthPickerDialog(
      initialMonthKey = uiState.customFilterMonth ?: FormatUtils.currentMonthKey(),
      title = "Pilih Periode Riwayat",
      subtitle = "Pilih bulan & tahun (misal: transaksi tahun lalu) untuk disaring",
      onDismiss = { showCustomMonthPickerDialog = false },
      onMonthSelected = { selectedMonthKey ->
        showCustomMonthPickerDialog = false
        onCustomMonthSelected(selectedMonthKey)
      }
    )
  }
}
