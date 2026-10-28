package com.example.ui.finance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.model.CategoryRegistry
import com.example.ui.theme.ExpenseRed
import com.example.ui.util.BudgetAlertLevel
import com.example.ui.util.FormatUtils

/**
 * Banner Peringatan Budget pada Dashboard jika ada pengeluaran mendekati atau melampaui limit.
 */
@Composable
fun BudgetAlertBanner(
  warnings: List<CategoryBudgetStatus>,
  selectedMonthLabel: String,
  onManageBudgets: () -> Unit,
  onOpenNotificationCenter: () -> Unit,
  onDismiss: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val hasOverBudget = warnings.any { it.isOverBudget }
  val primaryColor = if (hasOverBudget) ExpenseRed else Color(0xFFD97706) // Amber
  val containerColor = if (hasOverBudget) ExpenseRed.copy(alpha = 0.12f) else Color(0xFFFEF3C7)

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("budget_alert_banner"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(primaryColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (hasOverBudget) Icons.Default.Warning else Icons.Default.NotificationsActive,
              contentDescription = null,
              tint = primaryColor,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = if (hasOverBudget) "🚨 Peringatan Budget Terlampaui!" else "⚠️ Peringatan Budget Mendekati Limit!",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = primaryColor
            )
            Text(
              text = "${warnings.size} kategori pengeluaran butuh perhatian ($selectedMonthLabel)",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        if (onDismiss != null) {
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Tutup Peringatan",
              modifier = Modifier.size(16.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Highlight the first warning
      val topWarning = warnings.firstOrNull()
      if (topWarning != null) {
        val cat = CategoryRegistry.getCategory(topWarning.categoryName)
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onOpenNotificationCenter),
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.surface
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
              Column {
                Text(
                  text = topWarning.categoryName,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = if (topWarning.isOverBudget) {
                    "Overbudget ${FormatUtils.formatRupiah(-topWarning.remainingAmount)}"
                  } else {
                    "Sisa ${FormatUtils.formatRupiah(topWarning.remainingAmount)} (${(topWarning.progress * 100).toInt()}% terpakai)"
                  },
                  style = MaterialTheme.typography.labelSmall,
                  color = if (topWarning.isOverBudget) ExpenseRed else primaryColor,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }

            Text(
              text = "Lihat Detail →",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = primaryColor
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedButton(
          onClick = onOpenNotificationCenter,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Semua Peringatan (${warnings.size})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }

        Button(
          onClick = onManageBudgets,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
        ) {
          Text("Atur Budget", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

/**
 * Dialog Pusat Notifikasi & Peringatan Budget lengkap
 */
@Composable
fun BudgetNotificationCenterDialog(
  warnings: List<CategoryBudgetStatus>,
  selectedMonthLabel: String,
  onDismiss: () -> Unit,
  onManageBudgets: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(ExpenseRed.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.NotificationsActive,
            contentDescription = null,
            tint = ExpenseRed,
            modifier = Modifier.size(20.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "Pusat Peringatan Budget",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Bulan: $selectedMonthLabel (${warnings.size} Peringatan)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        if (warnings.isEmpty()) {
          Text(
            text = "Semua pengeluaran kategori masih dalam batas aman (di bawah 80% dari limit budget).",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        } else {
          Text(
            text = "Kategori berikut telah mencapai atau melebihi batas anggaran yang telah Anda tentukan:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(12.dp))

          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .height(300.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            items(warnings) { item ->
              val cat = CategoryRegistry.getCategory(item.categoryName)
              val isOver = item.isOverBudget
              val alertColor = if (isOver) ExpenseRed else Color(0xFFD97706)

              Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                  containerColor = alertColor.copy(alpha = 0.08f)
                )
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Box(
                        modifier = Modifier
                          .size(32.dp)
                          .clip(CircleShape)
                          .background(cat.color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                      ) {
                        Icon(
                          imageVector = cat.icon,
                          contentDescription = null,
                          tint = cat.color,
                          modifier = Modifier.size(18.dp)
                        )
                      }
                      Spacer(modifier = Modifier.width(8.dp))
                      Text(
                        text = item.categoryName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                      )
                    }

                    Surface(
                      shape = RoundedCornerShape(6.dp),
                      color = alertColor.copy(alpha = 0.2f)
                    ) {
                      Text(
                        text = if (isOver) "Terlampaui" else "${(item.progress * 100).toInt()}% Terpakai",
                        color = alertColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(8.dp))

                  LinearProgressIndicator(
                    progress = { item.progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(6.dp)
                      .clip(RoundedCornerShape(3.dp)),
                    color = alertColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                  )

                  Spacer(modifier = Modifier.height(6.dp))

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(
                      text = "Terpakai: ${FormatUtils.formatRupiah(item.spentAmount)}",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                      text = "Limit: ${FormatUtils.formatRupiah(item.allocatedBudget)}",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.SemiBold
                    )
                  }

                  Text(
                    text = if (isOver) {
                      "Melebihi budget sebesar ${FormatUtils.formatRupiah(-item.remainingAmount)}"
                    } else {
                      "Sisa budget hanya ${FormatUtils.formatRupiah(item.remainingAmount)}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = alertColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                  )
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onDismiss()
          onManageBudgets()
        },
        shape = RoundedCornerShape(10.dp)
      ) {
        Text("Kelola Budget")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Tutup")
      }
    },
    shape = RoundedCornerShape(20.dp)
  )
}
