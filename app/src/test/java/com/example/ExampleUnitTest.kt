package com.example

import com.example.ui.finance.CategoryBudgetStatus
import com.example.ui.util.BudgetAlertLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun categoryBudgetStatus_safeLevel_whenUnder80Percent() {
    val status = CategoryBudgetStatus(
      budgetId = "b1",
      categoryName = "Belanja",
      allocatedBudget = 1000000.0,
      spentAmount = 500000.0,
      remainingAmount = 500000.0,
      progress = 0.5f,
      isOverBudget = false
    )
    assertFalse(status.isOverBudget)
    assertFalse(status.isNearLimit)
    assertEquals(BudgetAlertLevel.SAFE, status.alertLevel)
  }

  @Test
  fun categoryBudgetStatus_nearLimit_when80PercentOrMore() {
    val status = CategoryBudgetStatus(
      budgetId = "b2",
      categoryName = "Pendidikan",
      allocatedBudget = 1000000.0,
      spentAmount = 850000.0,
      remainingAmount = 150000.0,
      progress = 0.85f,
      isOverBudget = false
    )
    assertFalse(status.isOverBudget)
    assertTrue(status.isNearLimit)
    assertEquals(BudgetAlertLevel.NEAR_LIMIT, status.alertLevel)
  }

  @Test
  fun categoryBudgetStatus_overBudget_whenExceedsAllocatedAmount() {
    val status = CategoryBudgetStatus(
      budgetId = "b3",
      categoryName = "Belanja",
      allocatedBudget = 1000000.0,
      spentAmount = 1200000.0,
      remainingAmount = -200000.0,
      progress = 1.2f,
      isOverBudget = true
    )
    assertTrue(status.isOverBudget)
    assertFalse(status.isNearLimit)
    assertEquals(BudgetAlertLevel.OVER_BUDGET, status.alertLevel)
  }

  @Test
  fun filterPeriod_customMonth_hasCorrectLabel() {
    assertEquals("Pilih Bulan", com.example.ui.finance.FilterPeriod.CUSTOM_MONTH.label)
  }

  @Test
  fun formatUtils_formatDateRange_producesExpectedString() {
    val startCal = java.util.Calendar.getInstance().apply {
      set(2026, java.util.Calendar.OCTOBER, 1, 10, 0, 0)
    }
    val endCal = java.util.Calendar.getInstance().apply {
      set(2026, java.util.Calendar.OCTOBER, 28, 18, 0, 0)
    }
    val formatted = com.example.ui.util.FormatUtils.formatDateRange(startCal.timeInMillis, endCal.timeInMillis)
    assertTrue(formatted.contains("01 Okt 2026") || formatted.contains("1 Okt 2026"))
    assertTrue(formatted.contains("28 Okt 2026"))
  }

  @Test
  fun formatUtils_utcMillisToLocalDayStartAndEnd_alignsProperly() {
    val nowUtc = 1793145600000L // arbitrary utc timestamp
    val start = com.example.ui.util.FormatUtils.utcMillisToLocalDayStart(nowUtc)
    val end = com.example.ui.util.FormatUtils.utcMillisToLocalDayEnd(nowUtc)
    assertTrue(end > start)
    val calStart = java.util.Calendar.getInstance().apply { timeInMillis = start }
    val calEnd = java.util.Calendar.getInstance().apply { timeInMillis = end }
    assertEquals(0, calStart.get(java.util.Calendar.HOUR_OF_DAY))
    assertEquals(0, calStart.get(java.util.Calendar.MINUTE))
    assertEquals(23, calEnd.get(java.util.Calendar.HOUR_OF_DAY))
    assertEquals(59, calEnd.get(java.util.Calendar.MINUTE))
  }
}



