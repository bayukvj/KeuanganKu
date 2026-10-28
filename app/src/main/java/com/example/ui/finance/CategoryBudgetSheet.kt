package com.example.ui.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
import android.app.DatePickerDialog
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionType
import com.example.ui.model.CategoryRegistry
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.util.FormatUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CategoryBudgetSheet(
  selectedMonth: String,
  selectedMonthLabel: String,
  budgetStatuses: List<CategoryBudgetStatus>,
  onPrevMonth: () -> Unit,
  onNextMonth: () -> Unit,
  onSelectMonth: (String) -> Unit,
  onSaveBudget: (category: String, amount: Double, month: String) -> Unit,
  onDeleteBudget: (budgetId: String, categoryName: String) -> Unit,
  onDismiss: () -> Unit,
  sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
  val context = LocalContext.current
  var showAddForm by remember { mutableStateOf(budgetStatuses.isEmpty()) }
  var selectedCategory by remember { mutableStateOf("Belanja") }
  var formTargetMonth by remember(selectedMonth) { mutableStateOf(selectedMonth) }
  var amountText by remember { mutableStateOf("") }
  var validationError by remember { mutableStateOf<String?>(null) }
  var showMonthPickerDialogForHeader by remember { mutableStateOf(false) }
  var showMonthPickerDialogForForm by remember { mutableStateOf(false) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
        .padding(bottom = 36.dp)
        .verticalScroll(rememberScrollState())
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Savings,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Budgeting per Kategori",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
        }
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Tutup")
        }
      }

      Text(
        text = "Tetapkan budget per bulan (misal belanja Oktober, pendidikan November). Pengeluaran pada bulan tersebut otomatis memotong sisa budget kategori.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
      )

      // Month Selector Card with Navigation
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("budget_month_selector_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        )
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(
              onClick = onPrevMonth,
              modifier = Modifier
                .size(40.dp)
                .testTag("budget_prev_month_btn")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Bulan Sebelumnya",
                tint = MaterialTheme.colorScheme.primary
              )
            }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { showMonthPickerDialogForHeader = true }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("budget_header_month_click")
            ) {
              Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = "BULAN ANGGARAN (KLIK)",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
                Text(
                  text = selectedMonthLabel,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.ExtraBold,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }

            IconButton(
              onClick = onNextMonth,
              modifier = Modifier
                .size(40.dp)
                .testTag("budget_next_month_btn")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Bulan Berikutnya",
                tint = MaterialTheme.colorScheme.primary
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Quick Month Chips Carousel (Current Year Months)
          val currentYear = run {
            val parts = selectedMonth.split("-")
            parts.getOrNull(0)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
          }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            FormatUtils.indonesianMonths.forEachIndexed { index, mName ->
              val mKey = FormatUtils.toMonthKey(currentYear, index)
              val isSelected = selectedMonth == mKey
              FilterChip(
                selected = isSelected,
                onClick = { onSelectMonth(mKey) },
                label = { Text(mName.take(3), fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                shape = RoundedCornerShape(8.dp),
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MaterialTheme.colorScheme.primary,
                  selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("month_chip_${index + 1}")
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Active Budget Items List for Selected Month
      Text(
        text = "Anggaran Bulan $selectedMonthLabel (${budgetStatuses.size})",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(8.dp))

      if (budgetStatuses.isEmpty()) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
          )
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
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
              text = "Gunakan tombol di bawah untuk menetapkan budget belanja atau kebutuhan lainnya.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
          }
        }
      } else {
        budgetStatuses.forEach { item ->
          val cat = CategoryRegistry.getCategory(item.categoryName)
          val progressColor = when {
            item.isOverBudget -> ExpenseRed
            item.progress > 0.8f -> Color(0xFFF59E0B) // Amber
            else -> IncomeGreen
          }

          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 5.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            )
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .background(cat.color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = cat.icon,
                      contentDescription = null,
                      tint = cat.color,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = item.categoryName,
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold
                    )
                    Text(
                      text = "Budget $selectedMonthLabel: ${FormatUtils.formatRupiah(item.allocatedBudget)}",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  // Edit quick action
                  IconButton(
                    onClick = {
                      selectedCategory = item.categoryName
                      amountText = item.allocatedBudget.toLong().toString()
                      formTargetMonth = selectedMonth
                      showAddForm = true
                    },
                    modifier = Modifier.size(32.dp)
                  ) {
                    Icon(
                      Icons.Default.Edit,
                      contentDescription = "Edit budget",
                      modifier = Modifier.size(16.dp),
                      tint = MaterialTheme.colorScheme.primary
                    )
                  }
                  // Delete quick action
                  IconButton(
                    onClick = { onDeleteBudget(item.budgetId, item.categoryName) },
                    modifier = Modifier.size(32.dp)
                  ) {
                    Icon(
                      Icons.Default.Delete,
                      contentDescription = "Hapus budget",
                      modifier = Modifier.size(16.dp),
                      tint = ExpenseRed
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Sisa Budget Berkurang
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "SISA BUDGET $selectedMonthLabel",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (item.isOverBudget) ExpenseRed else progressColor
                  )
                  Text(
                    text = if (item.remainingAmount >= 0) {
                      FormatUtils.formatRupiah(item.remainingAmount)
                    } else {
                      "Overbudget " + FormatUtils.formatRupiah(-item.remainingAmount)
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (item.isOverBudget) ExpenseRed else progressColor
                  )
                }

                Column(horizontalAlignment = Alignment.End) {
                  Text(
                    text = "Terpakai: ${FormatUtils.formatRupiah(item.spentAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = "${(item.progress * 100).toInt()}% terpakai",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = progressColor
                  )
                }
              }

              Spacer(modifier = Modifier.height(8.dp))

              LinearProgressIndicator(
                progress = { item.progress.coerceIn(0f, 1f) },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(8.dp)
                  .clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surface
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Add New / Edit Budget Section Button or Form
      if (!showAddForm) {
        Button(
          onClick = {
            amountText = ""
            formTargetMonth = selectedMonth
            showAddForm = true
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("add_category_budget_button"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.Add, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Tetapkan Budget Baru")
        }
      } else {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
          )
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Tetapkan Budget Kategori",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Form Target Month Picker
            Text(
              text = "Bulan yang Ditetapkan:",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Form Target Month Picker Component
            Text(
              text = "Pilih Bulan Spesifik Anggaran:",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Dedicated Interactive MonthPicker / DatePicker Card
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { showMonthPickerDialogForForm = true }
                .testTag("budget_form_month_picker_card"),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
              )
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f)
                ) {
                  Box(
                    modifier = Modifier
                      .size(38.dp)
                      .clip(CircleShape)
                      .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.CalendarMonth,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = "BULAN ANGGARAN",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                      text = FormatUtils.formatMonthLabel(formTargetMonth),
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.ExtraBold,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = "Sentuh untuk membuka MonthPicker",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }

                FilledTonalButton(
                  onClick = { showMonthPickerDialogForForm = true },
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.testTag("budget_form_change_month_btn")
                ) {
                  Text("Pilih Bulan", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Month Presets & DatePicker Button
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Direct Android DatePickerDialog Trigger
              OutlinedButton(
                onClick = {
                  val (y, m) = FormatUtils.parseMonthKey(formTargetMonth)
                  val dpd = DatePickerDialog(
                    context,
                    { _, selectedYear, selectedMonthIndex, _ ->
                      formTargetMonth = FormatUtils.toMonthKey(selectedYear, selectedMonthIndex)
                    },
                    y,
                    m,
                    1
                  )
                  dpd.setTitle("Pilih Bulan Anggaran")
                  dpd.show()
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("budget_form_native_datepicker_btn")
              ) {
                Icon(
                  imageVector = Icons.Default.CalendarToday,
                  contentDescription = null,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Kalender DatePicker", fontSize = 11.sp)
              }

              val currentMonthKey = FormatUtils.currentMonthKey()
              val nextMonthKey = FormatUtils.addMonths(currentMonthKey, 1)
              val twoMonthsKey = FormatUtils.addMonths(currentMonthKey, 2)
              val threeMonthsKey = FormatUtils.addMonths(currentMonthKey, 3)

              listOf(
                "Bulan Ini" to currentMonthKey,
                "Bulan Depan" to nextMonthKey,
                "+2 Bulan" to twoMonthsKey,
                "+3 Bulan" to threeMonthsKey
              ).forEach { (label, key) ->
                val isSelected = (formTargetMonth == key)
                FilterChip(
                  selected = isSelected,
                  onClick = { formTargetMonth = key },
                  label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                  shape = RoundedCornerShape(8.dp),
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                  )
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = "Pilih Kategori:",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              val expenseCategories = CategoryRegistry.defaultCategories.filter {
                it.defaultType == TransactionType.EXPENSE || it.name == "Lainnya"
              }

              expenseCategories.forEach { cat ->
                val isSelected = selectedCategory.equals(cat.name, ignoreCase = true)
                FilterChip(
                  selected = isSelected,
                  onClick = { selectedCategory = cat.name },
                  label = { Text(cat.name, style = MaterialTheme.typography.labelSmall) },
                  leadingIcon = {
                    Icon(
                      imageVector = cat.icon,
                      contentDescription = null,
                      tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else cat.color,
                      modifier = Modifier.size(16.dp)
                    )
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                  ),
                  shape = RoundedCornerShape(8.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = "Nominal Budget untuk $selectedCategory (${FormatUtils.formatMonthLabel(formTargetMonth)}):",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
              value = amountText,
              onValueChange = { input ->
                amountText = input.filter { it.isDigit() }
                validationError = null
              },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("category_budget_input"),
              prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
              placeholder = { Text("Contoh: 2000000") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              shape = RoundedCornerShape(12.dp)
            )

            // Preset Quick Buttons
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              listOf(500000L, 1000000L, 2000000L, 3000000L, 5000000L).forEach { amt ->
                SuggestionChip(
                  onClick = { amountText = amt.toString() },
                  label = { Text(FormatUtils.formatRupiah(amt.toDouble())) }
                )
              }
            }

            if (validationError != null) {
              Text(
                text = validationError ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              if (budgetStatuses.isNotEmpty()) {
                Button(
                  onClick = { showAddForm = false },
                  modifier = Modifier.weight(1f),
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Text("Batal")
                }
              }

              Button(
                onClick = {
                  val amt = amountText.toDoubleOrNull() ?: 0.0
                  if (amt <= 0.0) {
                    validationError = "Silakan masukkan nominal budget yang lebih dari 0"
                    return@Button
                  }
                  onSaveBudget(selectedCategory, amt, formTargetMonth)
                  onSelectMonth(formTargetMonth)
                  showAddForm = false
                  amountText = ""
                },
                modifier = Modifier
                  .weight(1.5f)
                  .testTag("save_category_budget_button"),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text("Simpan Budget", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }

  // Dialog MonthPicker for the form
  if (showMonthPickerDialogForForm) {
    MonthPickerDialog(
      initialMonthKey = formTargetMonth,
      title = "Pilih Bulan Anggaran",
      subtitle = "Tetapkan bulan spesifik untuk kategori $selectedCategory",
      onDismiss = { showMonthPickerDialogForForm = false },
      onMonthSelected = { chosenMonth ->
        formTargetMonth = chosenMonth
        showMonthPickerDialogForForm = false
      }
    )
  }

  // Dialog MonthPicker for the header
  if (showMonthPickerDialogForHeader) {
    MonthPickerDialog(
      initialMonthKey = selectedMonth,
      title = "Beralih Bulan Anggaran",
      subtitle = "Lihat status budget dan pengeluaran pada bulan terpilih",
      onDismiss = { showMonthPickerDialogForHeader = false },
      onMonthSelected = { chosenMonth ->
        onSelectMonth(chosenMonth)
        showMonthPickerDialogForHeader = false
      }
    )
  }
}

