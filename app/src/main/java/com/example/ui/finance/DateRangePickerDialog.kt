package com.example.ui.finance

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.util.FormatUtils

/**
 * Dialog pemilih rentang tanggal (DateRangePicker) berbasis Material 3
 * untuk memilih rentang tanggal fleksibel (tanggal awal s/d tanggal akhir).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDateRangePickerDialog(
  initialStartDateMillis: Long? = null,
  initialEndDateMillis: Long? = null,
  onDismiss: () -> Unit,
  onDateRangeSelected: (startDateMillis: Long, endDateMillis: Long) -> Unit
) {
  val dateRangePickerState = rememberDateRangePickerState(
    initialSelectedStartDateMillis = initialStartDateMillis,
    initialSelectedEndDateMillis = initialEndDateMillis
  )

  val hasValidSelection = dateRangePickerState.selectedStartDateMillis != null

  DatePickerDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(
        onClick = {
          val startUtc = dateRangePickerState.selectedStartDateMillis
          val endUtc = dateRangePickerState.selectedEndDateMillis ?: startUtc
          if (startUtc != null) {
            val localStart = FormatUtils.utcMillisToLocalDayStart(startUtc)
            val localEnd = FormatUtils.utcMillisToLocalDayEnd(endUtc ?: startUtc)
            onDateRangeSelected(localStart, localEnd)
          }
        },
        enabled = hasValidSelection,
        modifier = Modifier.testTag("apply_date_range_button")
      ) {
        Text("Terapkan", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("cancel_date_range_button")
      ) {
        Text("Batal")
      }
    }
  ) {
    DateRangePicker(
      state = dateRangePickerState,
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 520.dp),
      title = {
        Text(
          text = "Pilih Rentang Tanggal",
          modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 4.dp),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
      },
      headline = {
        DateRangePickerDefaults.DateRangePickerHeadline(
          selectedStartDateMillis = dateRangePickerState.selectedStartDateMillis,
          selectedEndDateMillis = dateRangePickerState.selectedEndDateMillis,
          displayMode = dateRangePickerState.displayMode,
          dateFormatter = remember { DatePickerDefaults.dateFormatter() },
          modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
        )
      }
    )
  }
}
