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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.model.CategoryRegistry
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.util.FormatUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionSheet(
  transactionToEdit: Transaction? = null,
  categoryBudgetStatuses: List<CategoryBudgetStatus> = emptyList(),
  onDismiss: () -> Unit,
  onSave: (type: TransactionType, amount: Double, category: String, title: String, note: String, date: Long, account: String) -> Unit,
  sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
  val context = LocalContext.current

  var selectedType by remember {
    mutableStateOf(
      if (transactionToEdit != null) {
        if (transactionToEdit.isIncome) TransactionType.INCOME else TransactionType.EXPENSE
      } else TransactionType.EXPENSE
    )
  }

  var amountText by remember {
    mutableStateOf(
      if (transactionToEdit != null && transactionToEdit.amount > 0) {
        transactionToEdit.amount.toLong().toString()
      } else ""
    )
  }

  var title by remember {
    mutableStateOf(transactionToEdit?.title ?: "")
  }

  var selectedDateMillis by remember {
    mutableStateOf(transactionToEdit?.date ?: System.currentTimeMillis())
  }

  var selectedCategory by remember {
    mutableStateOf(
      transactionToEdit?.category ?: if (selectedType == TransactionType.EXPENSE) "Belanja" else "Gaji"
    )
  }

  var categoryDropdownExpanded by remember { mutableStateOf(false) }

  var selectedAccount by remember {
    mutableStateOf(transactionToEdit?.account ?: "Tunai")
  }

  var note by remember {
    mutableStateOf(transactionToEdit?.note ?: "")
  }

  var validationError by remember { mutableStateOf<String?>(null) }

  val accounts = listOf("Tunai", "Rekening Bank", "E-Wallet", "Kartu")

  val currentCategoryBudget = if (selectedType == TransactionType.EXPENSE) {
    categoryBudgetStatuses.firstOrNull { it.categoryName.equals(selectedCategory, ignoreCase = true) }
  } else null

  val enteredAmount = amountText.toDoubleOrNull() ?: 0.0

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
        .padding(bottom = 32.dp)
        .verticalScroll(rememberScrollState())
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (transactionToEdit != null) "Edit Transaksi" else "Catat Transaksi Baru",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Tutup")
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Type Selector Tabs (Uang Keluar vs Uang Masuk)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          .padding(4.dp)
      ) {
        // Uang Keluar (Expense)
        Surface(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .clickable {
              selectedType = TransactionType.EXPENSE
              if (selectedCategory == "Gaji" || selectedCategory == "Investasi" || selectedCategory == "Usaha / Bisnis") {
                selectedCategory = "Belanja"
              }
            },
          color = if (selectedType == TransactionType.EXPENSE) ExpenseRed else Color.Transparent,
          shape = RoundedCornerShape(10.dp)
        ) {
          Row(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.ArrowDownward,
              contentDescription = null,
              tint = if (selectedType == TransactionType.EXPENSE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Uang Keluar",
              fontWeight = FontWeight.SemiBold,
              color = if (selectedType == TransactionType.EXPENSE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Uang Masuk (Income)
        Surface(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .clickable {
              selectedType = TransactionType.INCOME
              if (selectedCategory == "Belanja" || selectedCategory == "Pendidikan" || selectedCategory == "Makanan & Minuman") {
                selectedCategory = "Gaji"
              }
            },
          color = if (selectedType == TransactionType.INCOME) IncomeGreen else Color.Transparent,
          shape = RoundedCornerShape(10.dp)
        ) {
          Row(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.ArrowUpward,
              contentDescription = null,
              tint = if (selectedType == TransactionType.INCOME) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Uang Masuk",
              fontWeight = FontWeight.SemiBold,
              color = if (selectedType == TransactionType.INCOME) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Category Selector
      Text(
        text = "Pilih Kategori",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(8.dp))

      val availableCategories = CategoryRegistry.defaultCategories.filter {
        if (selectedType == TransactionType.EXPENSE) {
          it.defaultType == TransactionType.EXPENSE || it.name == "Lainnya"
        } else {
          it.defaultType == TransactionType.INCOME || it.name == "Lainnya"
        }
      }

      val currentCategoryItem = CategoryRegistry.defaultCategories.firstOrNull {
        it.name.equals(selectedCategory, ignoreCase = true)
      }

      ExposedDropdownMenuBox(
        expanded = categoryDropdownExpanded,
        onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("category_dropdown_box")
      ) {
        OutlinedTextField(
          value = selectedCategory,
          onValueChange = {},
          readOnly = true,
          leadingIcon = {
            if (currentCategoryItem != null) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(currentCategoryItem.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = currentCategoryItem.icon,
                  contentDescription = null,
                  tint = currentCategoryItem.color,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          },
          trailingIcon = {
            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded)
          },
          colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            .fillMaxWidth()
            .testTag("category_dropdown_field")
        )

        ExposedDropdownMenu(
          expanded = categoryDropdownExpanded,
          onDismissRequest = { categoryDropdownExpanded = false }
        ) {
          availableCategories.forEach { cat ->
            val isSelected = selectedCategory.equals(cat.name, ignoreCase = true)
            DropdownMenuItem(
              text = {
                Text(
                  text = cat.name,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
              },
              leadingIcon = {
                Box(
                  modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(cat.color.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = cat.icon,
                    contentDescription = null,
                    tint = cat.color,
                    modifier = Modifier.size(16.dp)
                  )
                }
              },
              trailingIcon = if (isSelected) {
                {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Terpilih",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                  )
                }
              } else null,
              onClick = {
                selectedCategory = cat.name
                categoryDropdownExpanded = false
              },
              modifier = Modifier.testTag("category_dropdown_item_${cat.name.lowercase().replace(" ", "_")}")
            )
          }
        }
      }

      // Category Budget Indicator Banner (Direct User Feature!)
      if (currentCategoryBudget != null) {
        Spacer(modifier = Modifier.height(12.dp))
        val newSpent = currentCategoryBudget.spentAmount + enteredAmount
        val ratio = if (currentCategoryBudget.allocatedBudget > 0) newSpent / currentCategoryBudget.allocatedBudget else 0.0
        val willExceed = newSpent > currentCategoryBudget.allocatedBudget
        val willNearLimit = ratio >= 0.8 && !willExceed

        val bannerBg = when {
          willExceed -> ExpenseRed.copy(alpha = 0.12f)
          willNearLimit -> Color(0xFFFEF3C7)
          else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        }
        val bannerBorderColor = when {
          willExceed -> ExpenseRed
          willNearLimit -> Color(0xFFD97706)
          else -> MaterialTheme.colorScheme.primary
        }

        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          color = bannerBg
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (willExceed || willNearLimit) Icons.Default.Warning else Icons.Default.Savings,
              contentDescription = null,
              tint = bannerBorderColor,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Budget Kategori $selectedCategory: ${FormatUtils.formatRupiah(currentCategoryBudget.allocatedBudget)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "Sisa Saat Ini: ${FormatUtils.formatRupiah(currentCategoryBudget.remainingAmount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = when {
                  currentCategoryBudget.isOverBudget -> ExpenseRed
                  currentCategoryBudget.isNearLimit -> Color(0xFFD97706)
                  else -> MaterialTheme.colorScheme.onSurface
                }
              )
              if (enteredAmount > 0) {
                val newRemaining = currentCategoryBudget.allocatedBudget - newSpent
                val warningText = when {
                  willExceed -> "🚨 Peringatan: Transaksi ini akan melebihi budget sebesar ${FormatUtils.formatRupiah(-newRemaining)}!"
                  willNearLimit -> "⚠️ Perhatian: Transaksi ini menyebabkan budget terpakai ${(ratio * 100).toInt()}%. Sisa: ${FormatUtils.formatRupiah(newRemaining)}"
                  else -> "Setelah transaksi ini, sisa budget menjadi ${FormatUtils.formatRupiah(newRemaining)}"
                }
                val warningColor = when {
                  willExceed -> ExpenseRed
                  willNearLimit -> Color(0xFFD97706)
                  else -> IncomeGreen
                }
                Text(
                  text = warningText,
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Bold,
                  color = warningColor
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Amount Input
      Text(
        text = "Nominal",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(6.dp))
      OutlinedTextField(
        value = amountText,
        onValueChange = { input ->
          val filtered = input.filter { it.isDigit() }
          amountText = filtered
          validationError = null
        },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("amount_input"),
        prefix = {
          Text(
            text = "Rp ",
            fontWeight = FontWeight.Bold,
            color = if (selectedType == TransactionType.EXPENSE) ExpenseRed else IncomeGreen
          )
        },
        placeholder = { Text("0") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
      )

      // Quick Amount Chips
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val quickAmounts = listOf(10000L, 25000L, 50000L, 100000L, 500000L, 1000000L)
        quickAmounts.forEach { amt ->
          SuggestionChip(
            onClick = {
              val current = amountText.toLongOrNull() ?: 0L
              amountText = (current + amt).toString()
            },
            label = { Text("+${FormatUtils.formatRupiah(amt.toDouble())}") }
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Title Input
      Text(
        text = "Keterangan Transaksi",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(6.dp))
      OutlinedTextField(
        value = title,
        onValueChange = {
          title = it
          validationError = null
        },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("title_input"),
        placeholder = {
          Text(
            if (selectedType == TransactionType.EXPENSE)
              "Contoh: Belanja Bulanan, SPP Kuliah, Makan Siang"
            else
              "Contoh: Gaji Kantor, Bonus Usaha, Hasil Investasi"
          )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
      )

      Spacer(modifier = Modifier.height(18.dp))

      // Account Selector (Tunai, Bank, E-Wallet)
      Text(
        text = "Sumber Dana / Akun",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        accounts.forEach { acc ->
          val isSelected = selectedAccount.equals(acc, ignoreCase = true)
          val icon = when (acc) {
            "Tunai" -> Icons.Default.AccountBalanceWallet
            "Rekening Bank" -> Icons.Default.AccountBalance
            "E-Wallet" -> Icons.Default.PhoneAndroid
            else -> Icons.Default.CreditCard
          }
          FilterChip(
            selected = isSelected,
            onClick = { selectedAccount = acc },
            label = { Text(acc) },
            leadingIcon = {
              Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
            },
            shape = RoundedCornerShape(10.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Tanggal Transaksi (Date & Time Selector)
      Text(
        text = "Tanggal & Waktu Transaksi",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(6.dp))

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
            android.app.DatePickerDialog(
              context,
              { _, y, m, d ->
                val updated = Calendar.getInstance().apply {
                  timeInMillis = selectedDateMillis
                  set(Calendar.YEAR, y)
                  set(Calendar.MONTH, m)
                  set(Calendar.DAY_OF_MONTH, d)
                }
                selectedDateMillis = updated.timeInMillis
              },
              cal.get(Calendar.YEAR),
              cal.get(Calendar.MONTH),
              cal.get(Calendar.DAY_OF_MONTH)
            ).show()
          }
          .testTag("transaction_date_picker_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = FormatUtils.formatDateWithDay(selectedDateMillis),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Pukul ${FormatUtils.formatTimeOnly(selectedDateMillis)} WIB",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Text(
            text = "Ubah Tanggal",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      // Quick Date Presets: Hari Ini, Kemarin, Kalender
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val nowCal = Calendar.getInstance()
        val isToday = run {
          val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
          cal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR) &&
            cal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)
        }
        val isYesterday = run {
          val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
          val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
          cal.get(Calendar.DAY_OF_YEAR) == yesterdayCal.get(Calendar.DAY_OF_YEAR) &&
            cal.get(Calendar.YEAR) == yesterdayCal.get(Calendar.YEAR)
        }

        FilterChip(
          selected = isToday,
          onClick = {
            selectedDateMillis = System.currentTimeMillis()
          },
          label = { Text("Hari Ini", fontSize = 12.sp) },
          shape = RoundedCornerShape(10.dp)
        )

        FilterChip(
          selected = isYesterday,
          onClick = {
            val yCal = Calendar.getInstance().apply {
              timeInMillis = selectedDateMillis
              add(Calendar.DAY_OF_YEAR, -1)
            }
            selectedDateMillis = yCal.timeInMillis
          },
          label = { Text("Kemarin", fontSize = 12.sp) },
          shape = RoundedCornerShape(10.dp)
        )

        FilterChip(
          selected = false,
          onClick = {
            val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
            android.app.DatePickerDialog(
              context,
              { _, y, m, d ->
                val updated = Calendar.getInstance().apply {
                  timeInMillis = selectedDateMillis
                  set(Calendar.YEAR, y)
                  set(Calendar.MONTH, m)
                  set(Calendar.DAY_OF_MONTH, d)
                }
                selectedDateMillis = updated.timeInMillis
              },
              cal.get(Calendar.YEAR),
              cal.get(Calendar.MONTH),
              cal.get(Calendar.DAY_OF_MONTH)
            ).show()
          },
          label = { Text("Kalender", fontSize = 12.sp) },
          leadingIcon = {
            Icon(Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(14.dp))
          },
          shape = RoundedCornerShape(10.dp)
        )
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Note (Optional)
      Text(
        text = "Catatan Tambahan (Opsional)",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(6.dp))
      OutlinedTextField(
        value = note,
        onValueChange = { note = it },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("note_input"),
        placeholder = { Text("Tambahkan rincian bila diperlukan...") },
        maxLines = 3,
        shape = RoundedCornerShape(12.dp)
      )

      if (validationError != null) {
        Text(
          text = validationError ?: "",
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.padding(top = 10.dp)
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Save Button
      Button(
        onClick = {
          val amt = amountText.toDoubleOrNull() ?: 0.0
          if (amt <= 0.0) {
            validationError = "Silakan masukkan nominal yang lebih dari 0"
            return@Button
          }
          if (title.isBlank()) {
            validationError = "Keterangan transaksi tidak boleh kosong"
            return@Button
          }

          onSave(
            selectedType,
            amt,
            selectedCategory,
            title.trim(),
            note.trim(),
            selectedDateMillis,
            selectedAccount
          )
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("save_transaction_button"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (selectedType == TransactionType.EXPENSE) ExpenseRed else IncomeGreen
        )
      ) {
        Text(
          text = if (transactionToEdit != null) "Perbarui Transaksi" else "Simpan Transaksi",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }
    }
  }
}
