package com.example.ui.finance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.util.FormatUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BudgetDialog(
  currentBudget: Double,
  initialMonth: String = FormatUtils.currentMonthKey(),
  onDismiss: () -> Unit,
  onSave: (Double) -> Unit,
  onSaveWithMonth: ((Double, String) -> Unit)? = null
) {
  var budgetText by remember {
    mutableStateOf(if (currentBudget > 0) currentBudget.toLong().toString() else "")
  }
  var selectedMonth by remember { mutableStateOf(initialMonth) }
  var showMonthPicker by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Atur Anggaran Bulanan",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Tentukan batas pengeluaran bulanan Anda untuk memantau pengeluaran agar tidak overbudget.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Month Picker Selector Card
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { showMonthPicker = true }
            .testTag("budget_dialog_month_picker_card"),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
          )
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "BULAN ANGGARAN",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
                Text(
                  text = FormatUtils.formatMonthLabel(selectedMonth),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold
                )
              }
            }
            FilledTonalButton(
              onClick = { showMonthPicker = true },
              shape = RoundedCornerShape(8.dp)
            ) {
              Text("Pilih Bulan", fontSize = 11.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = budgetText,
          onValueChange = { input ->
            budgetText = input.filter { it.isDigit() }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("budget_input"),
          prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
          placeholder = { Text("Contoh: 5000000") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Preset Cepat:",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))

        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(2000000L, 3000000L, 5000000L, 7500000L, 10000000L).forEach { amt ->
            SuggestionChip(
              onClick = { budgetText = amt.toString() },
              label = { Text(FormatUtils.formatRupiah(amt.toDouble())) }
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amt = budgetText.toDoubleOrNull() ?: 0.0
          if (onSaveWithMonth != null) {
            onSaveWithMonth(amt, selectedMonth)
          } else {
            onSave(amt)
          }
          onDismiss()
        },
        modifier = Modifier.testTag("save_budget_button"),
        shape = RoundedCornerShape(10.dp)
      ) {
        Text("Simpan Anggaran")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Batal")
      }
    },
    shape = RoundedCornerShape(20.dp)
  )

  if (showMonthPicker) {
    MonthPickerDialog(
      initialMonthKey = selectedMonth,
      onDismiss = { showMonthPicker = false },
      onMonthSelected = {
        selectedMonth = it
        showMonthPicker = false
      }
    )
  }
}

