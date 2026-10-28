package com.example.ui.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtils {
  private val idLocale = Locale("id", "ID")

  val indonesianMonths = listOf(
    "Januari", "Februari", "Maret", "April", "Mei", "Juni",
    "Juli", "Agustus", "September", "Oktober", "November", "Desember"
  )

  fun formatRupiah(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(idLocale)
    formatter.maximumFractionDigits = 0
    return formatter.format(amount)
  }

  fun formatDate(timeMillis: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", idLocale)
    return sdf.format(Date(timeMillis))
  }

  fun formatDateWithDay(timeMillis: Long): String {
    val sdf = SimpleDateFormat("EEEE, dd MMM yyyy", idLocale)
    return sdf.format(Date(timeMillis))
  }

  fun formatDateTime(timeMillis: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", idLocale)
    return sdf.format(Date(timeMillis))
  }

  fun formatTimeOnly(timeMillis: Long): String {
    val sdf = SimpleDateFormat("HH:mm", idLocale)
    return sdf.format(Date(timeMillis))
  }

  fun toMonthKey(year: Int, monthIndexZeroBased: Int): String {
    return String.format(Locale.US, "%04d-%02d", year, monthIndexZeroBased + 1)
  }

  fun currentMonthKey(): String {
    val cal = java.util.Calendar.getInstance()
    return toMonthKey(cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH))
  }

  fun parseMonthKey(monthKey: String): Pair<Int, Int> {
    val parts = monthKey.split("-")
    val cal = java.util.Calendar.getInstance()
    val year = parts.getOrNull(0)?.toIntOrNull() ?: cal.get(java.util.Calendar.YEAR)
    val monthIndex = ((parts.getOrNull(1)?.toIntOrNull() ?: (cal.get(java.util.Calendar.MONTH) + 1)) - 1).coerceIn(0, 11)
    return Pair(year, monthIndex)
  }

  fun addMonths(monthKey: String, offsetMonths: Int): String {
    val (y, m) = parseMonthKey(monthKey)
    val cal = java.util.Calendar.getInstance().apply {
      set(java.util.Calendar.YEAR, y)
      set(java.util.Calendar.MONTH, m)
      add(java.util.Calendar.MONTH, offsetMonths)
    }
    return toMonthKey(cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH))
  }

  fun formatMonthLabel(monthKey: String): String {
    val parts = monthKey.split("-")
    if (parts.size == 2) {
      val year = parts[0].toIntOrNull() ?: 2026
      val mIndex = (parts[1].toIntOrNull() ?: 1) - 1
      if (mIndex in indonesianMonths.indices) {
        return "${indonesianMonths[mIndex]} $year"
      }
    }
    return monthKey
  }

  fun utcMillisToLocalDayStart(utcMillis: Long): Long {
    val utcCal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
      timeInMillis = utcMillis
    }
    val localCal = java.util.Calendar.getInstance().apply {
      set(java.util.Calendar.YEAR, utcCal.get(java.util.Calendar.YEAR))
      set(java.util.Calendar.MONTH, utcCal.get(java.util.Calendar.MONTH))
      set(java.util.Calendar.DAY_OF_MONTH, utcCal.get(java.util.Calendar.DAY_OF_MONTH))
      set(java.util.Calendar.HOUR_OF_DAY, 0)
      set(java.util.Calendar.MINUTE, 0)
      set(java.util.Calendar.SECOND, 0)
      set(java.util.Calendar.MILLISECOND, 0)
    }
    return localCal.timeInMillis
  }

  fun utcMillisToLocalDayEnd(utcMillis: Long): Long {
    val utcCal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
      timeInMillis = utcMillis
    }
    val localCal = java.util.Calendar.getInstance().apply {
      set(java.util.Calendar.YEAR, utcCal.get(java.util.Calendar.YEAR))
      set(java.util.Calendar.MONTH, utcCal.get(java.util.Calendar.MONTH))
      set(java.util.Calendar.DAY_OF_MONTH, utcCal.get(java.util.Calendar.DAY_OF_MONTH))
      set(java.util.Calendar.HOUR_OF_DAY, 23)
      set(java.util.Calendar.MINUTE, 59)
      set(java.util.Calendar.SECOND, 59)
      set(java.util.Calendar.MILLISECOND, 999)
    }
    return localCal.timeInMillis
  }

  fun formatDateRange(startMillis: Long, endMillis: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", idLocale)
    return "${sdf.format(Date(startMillis))} – ${sdf.format(Date(endMillis))}"
  }
}

