package com.example.ui.finance

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.util.FormatUtils
import java.util.Calendar

/**
 * Komponen MonthPicker dialog interaktif untuk memilih bulan dan tahun secara spesifik,
 * dilengkapi opsi membuka DatePicker standar Android.
 */
@Composable
fun MonthPickerDialog(
  initialMonthKey: String,
  title: String = "Pilih Bulan Anggaran",
  subtitle: String = "Pilih bulan dan tahun spesifik untuk budget ini",
  onDismiss: () -> Unit,
  onMonthSelected: (String) -> Unit
) {
  val context = LocalContext.current
  val (initialYear, initialMonthIndex) = remember(initialMonthKey) {
    FormatUtils.parseMonthKey(initialMonthKey)
  }

  val todayCal = remember { Calendar.getInstance() }
  val currentSystemYear = todayCal.get(Calendar.YEAR)
  val currentSystemMonth = todayCal.get(Calendar.MONTH)

  var displayedYear by remember { mutableIntStateOf(initialYear) }
  var selectedMonthIndex by remember { mutableIntStateOf(initialMonthIndex) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Year Stepper Header
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(
              onClick = { displayedYear-- },
              modifier = Modifier
                .size(36.dp)
                .testTag("picker_prev_year_btn")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Tahun Sebelumnya",
                tint = MaterialTheme.colorScheme.primary
              )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = displayedYear.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
              )
              if (displayedYear == currentSystemYear) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                  Text(
                    text = "Tahun Ini",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }

            IconButton(
              onClick = { displayedYear++ },
              modifier = Modifier
                .size(36.dp)
                .testTag("picker_next_year_btn")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Tahun Berikutnya",
                tint = MaterialTheme.colorScheme.primary
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Month Grid: 4 rows x 3 columns
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          for (row in 0 until 4) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              for (col in 0 until 3) {
                val monthIndex = row * 3 + col
                val monthName = FormatUtils.indonesianMonths[monthIndex]
                val isSelected = (monthIndex == selectedMonthIndex)
                val isCurrentMonth = (displayedYear == currentSystemYear && monthIndex == currentSystemMonth)

                Surface(
                  modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { selectedMonthIndex = monthIndex }
                    .testTag("month_picker_cell_${monthIndex + 1}"),
                  shape = RoundedCornerShape(10.dp),
                  color = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isCurrentMonth -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                  },
                  tonalElevation = if (isSelected) 4.dp else 0.dp
                ) {
                  Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                  ) {
                    Column(
                      horizontalAlignment = Alignment.CenterHorizontally,
                      verticalArrangement = Arrangement.Center
                    ) {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                      ) {
                        Text(
                          text = monthName.take(3),
                          style = MaterialTheme.typography.bodyMedium,
                          fontWeight = if (isSelected || isCurrentMonth) FontWeight.Bold else FontWeight.Medium,
                          color = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            isCurrentMonth -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                          }
                        )
                        if (isSelected) {
                          Spacer(modifier = Modifier.width(3.dp))
                          Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                          )
                        }
                      }
                      if (isCurrentMonth && !isSelected) {
                        Box(
                          modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Preview Label
        val previewKey = FormatUtils.toMonthKey(displayedYear, selectedMonthIndex)
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Bulan Terpilih:",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = FormatUtils.formatMonthLabel(previewKey),
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick DatePicker Alternative (Android DatePickerDialog)
        OutlinedButton(
          onClick = {
            val datePicker = DatePickerDialog(
              context,
              { _, y, m, _ ->
                displayedYear = y
                selectedMonthIndex = m
              },
              displayedYear,
              selectedMonthIndex,
              1
            )
            datePicker.setTitle("Pilih Bulan via Kalender")
            datePicker.show()
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("month_picker_native_calendar_btn"),
          shape = RoundedCornerShape(10.dp)
        ) {
          Icon(
            imageVector = Icons.Default.CalendarToday,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text("Buka Kalender Lengkap (DatePicker)", fontSize = 12.sp)
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val chosenMonthKey = FormatUtils.toMonthKey(displayedYear, selectedMonthIndex)
          onMonthSelected(chosenMonthKey)
          onDismiss()
        },
        modifier = Modifier.testTag("confirm_month_picker_button"),
        shape = RoundedCornerShape(10.dp)
      ) {
        Text("Pilih Bulan Ini", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Batal")
      }
    },
    shape = RoundedCornerShape(20.dp)
  )
}
