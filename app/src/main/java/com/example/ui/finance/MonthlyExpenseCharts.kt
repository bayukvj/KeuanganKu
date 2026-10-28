package com.example.ui.finance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.model.CategoryRegistry
import com.example.ui.util.FormatUtils
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

enum class ChartType(val label: String) {
  DONUT("Grafik Lingkaran"),
  BAR("Grafik Batang")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MonthlyExpenseChartsSection(
  summaries: List<CategorySummary>,
  totalExpense: Double,
  selectedCategory: String?,
  onCategorySelected: (String?) -> Unit
) {
  var selectedChartType by remember { mutableStateOf(ChartType.DONUT) }
  var highlightedCategory by remember { mutableStateOf<String?>(null) }

  // Sync with parent selected category
  LaunchedEffect(selectedCategory) {
    if (selectedCategory != null) {
      highlightedCategory = selectedCategory
    }
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 6.dp)
      .testTag("monthly_expense_chart_card"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.5.dp)
  ) {
    Column(
      modifier = Modifier.padding(18.dp)
    ) {
      // Header with title and Chart Type Switcher
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
              imageVector = if (selectedChartType == ChartType.DONUT) Icons.Default.PieChart else Icons.Default.BarChart,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Visualisasi Pengeluaran",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Berdasarkan kategori bulan ini",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Toggle Buttons (Donut vs Bar)
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(2.dp)
        ) {
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .clickable { selectedChartType = ChartType.DONUT },
            color = if (selectedChartType == ChartType.DONUT) MaterialTheme.colorScheme.primary else Color.Transparent,
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.PieChart,
              contentDescription = "Donut Chart",
              tint = if (selectedChartType == ChartType.DONUT) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier
                .padding(6.dp)
                .size(18.dp)
            )
          }
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .clickable { selectedChartType = ChartType.BAR },
            color = if (selectedChartType == ChartType.BAR) MaterialTheme.colorScheme.primary else Color.Transparent,
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.BarChart,
              contentDescription = "Bar Chart",
              tint = if (selectedChartType == ChartType.BAR) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier
                .padding(6.dp)
                .size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (summaries.isEmpty() || totalExpense <= 0.0) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Belum ada data pengeluaran untuk divisualisasikan pada periode ini.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
          )
        }
      } else {
        // Active Displayed Category Information (Center hole or Header info)
        val activeSummary = summaries.firstOrNull { it.categoryName == highlightedCategory }
          ?: summaries.first()
        val activeCatItem = CategoryRegistry.getCategory(activeSummary.categoryName)

        when (selectedChartType) {
          ChartType.DONUT -> {
            DonutChartComponent(
              summaries = summaries,
              totalExpense = totalExpense,
              activeSummary = activeSummary,
              onSliceTapped = { catName ->
                highlightedCategory = catName
                onCategorySelected(catName)
              }
            )
          }
          ChartType.BAR -> {
            BarChartComponent(
              summaries = summaries,
              highlightedCategory = highlightedCategory,
              onBarTapped = { catName ->
                highlightedCategory = catName
                onCategorySelected(catName)
              }
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interactive Category Legend Chips
        Text(
          text = "Pilih Kategori:",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          summaries.forEach { summary ->
            val cat = CategoryRegistry.getCategory(summary.categoryName)
            val isSelected = highlightedCategory == summary.categoryName

            FilterChip(
              selected = isSelected,
              onClick = {
                if (highlightedCategory == summary.categoryName) {
                  highlightedCategory = null
                  onCategorySelected(null)
                } else {
                  highlightedCategory = summary.categoryName
                  onCategorySelected(summary.categoryName)
                }
              },
              label = {
                Text(
                  text = "${summary.categoryName} (${(summary.percentage * 100).toInt()}%)",
                  style = MaterialTheme.typography.labelSmall
                )
              },
              leadingIcon = {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(cat.color)
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = cat.color.copy(alpha = 0.2f),
                selectedLabelColor = MaterialTheme.colorScheme.onSurface
              ),
              shape = RoundedCornerShape(8.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun DonutChartComponent(
  summaries: List<CategorySummary>,
  totalExpense: Double,
  activeSummary: CategorySummary,
  onSliceTapped: (String) -> Unit
) {
  val animationProgress = remember { Animatable(0f) }

  LaunchedEffect(summaries) {
    animationProgress.snapTo(0f)
    animationProgress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
    )
  }

  val activeCatItem = CategoryRegistry.getCategory(activeSummary.categoryName)

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(240.dp),
    contentAlignment = Alignment.Center
  ) {
    Canvas(
      modifier = Modifier
        .size(220.dp)
        .pointerInput(summaries) {
          detectTapGestures { tapOffset ->
            val center = Offset(size.width / 2f, size.height / 2f)
            val dx = tapOffset.x - center.x
            val dy = tapOffset.y - center.y
            val distance = kotlin.math.sqrt(dx * dx + dy * dy)
            val outerRadius = size.width / 2f
            val innerRadius = outerRadius - 42.dp.toPx()

            if (distance in innerRadius..outerRadius) {
              var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
              if (angle < 0) angle += 360f

              // Map angle to category slice
              var currentStartAngle = 0f
              for (summary in summaries) {
                val sweep = summary.percentage * 360f
                if (angle >= currentStartAngle && angle < currentStartAngle + sweep) {
                  onSliceTapped(summary.categoryName)
                  break
                }
                currentStartAngle += sweep
              }
            }
          }
        }
    ) {
      val strokeWidthDefault = 36.dp.toPx()
      val strokeWidthActive = 44.dp.toPx()
      val radius = (size.minDimension - strokeWidthActive) / 2f
      val center = Offset(size.width / 2f, size.height / 2f)

      var startAngle = -90f // Start from 12 o'clock

      summaries.forEach { summary ->
        val cat = CategoryRegistry.getCategory(summary.categoryName)
        val isSelected = summary.categoryName == activeSummary.categoryName
        val sweepAngle = summary.percentage * 360f * animationProgress.value
        val stroke = Stroke(
          width = if (isSelected) strokeWidthActive else strokeWidthDefault,
          cap = StrokeCap.Butt
        )

        // Draw slice with a subtle 1.5 degree gap for clean distinction
        val gapAngle = if (summaries.size > 1) 1.5f else 0f
        val adjustedSweep = (sweepAngle - gapAngle).coerceAtLeast(0.5f)

        drawArc(
          color = if (isSelected) cat.color else cat.color.copy(alpha = 0.85f),
          startAngle = startAngle + (gapAngle / 2f),
          sweepAngle = adjustedSweep,
          useCenter = false,
          topLeft = Offset(center.x - radius, center.y - radius),
          size = Size(radius * 2, radius * 2),
          style = stroke
        )

        startAngle += sweepAngle
      }
    }

    // Centered Detail Information inside Donut Hole
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier
        .size(130.dp)
        .padding(6.dp)
    ) {
      Box(
        modifier = Modifier
          .size(24.dp)
          .clip(CircleShape)
          .background(activeCatItem.color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = activeCatItem.icon,
          contentDescription = null,
          tint = activeCatItem.color,
          modifier = Modifier.size(14.dp)
        )
      }
      Spacer(modifier = Modifier.height(3.dp))
      Text(
        text = activeSummary.categoryName,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Text(
        text = FormatUtils.formatRupiah(activeSummary.totalAmount),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.ExtraBold,
        color = activeCatItem.color,
        maxLines = 1
      )
      Text(
        text = "${(activeSummary.percentage * 100).toInt()}% total",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun BarChartComponent(
  summaries: List<CategorySummary>,
  highlightedCategory: String?,
  onBarTapped: (String) -> Unit
) {
  val maxAmount = summaries.maxOfOrNull { it.totalAmount } ?: 1.0

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    summaries.forEach { summary ->
      val cat = CategoryRegistry.getCategory(summary.categoryName)
      val isSelected = highlightedCategory == summary.categoryName
      val relativeWidth = (summary.totalAmount / maxAmount).toFloat().coerceIn(0.05f, 1f)

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(if (isSelected) cat.color.copy(alpha = 0.08f) else Color.Transparent)
          .clickable { onBarTapped(summary.categoryName) }
          .padding(vertical = 4.dp, horizontal = 6.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(cat.color.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = cat.icon,
                contentDescription = null,
                tint = cat.color,
                modifier = Modifier.size(14.dp)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = summary.categoryName,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
          }

          Text(
            text = "${FormatUtils.formatRupiah(summary.totalAmount)} (${(summary.percentage * 100).toInt()}%)",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Animated Bar
        LinearProgressIndicator(
          progress = { relativeWidth },
          modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp)),
          color = cat.color,
          trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
      }
    }
  }
}
