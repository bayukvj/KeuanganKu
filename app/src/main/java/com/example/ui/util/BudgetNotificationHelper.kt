package com.example.ui.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity

enum class BudgetAlertLevel {
  SAFE,
  NEAR_LIMIT,
  OVER_BUDGET
}

object BudgetNotificationHelper {
  private const val TAG = "BudgetNotification"
  const val CHANNEL_ID = "budget_alerts_channel"
  private const val CHANNEL_NAME = "Peringatan Budget Keuangan"
  private const val CHANNEL_DESC = "Notifikasi peringatan jika pengeluaran mendekati atau melampaui budget"

  fun initChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val importance = NotificationManager.IMPORTANCE_HIGH
      val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
        description = CHANNEL_DESC
        enableVibration(true)
        enableLights(true)
      }
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      notificationManager?.createNotificationChannel(channel)
    }
  }

  fun sendBudgetAlert(
    context: Context,
    categoryName: String,
    alertLevel: BudgetAlertLevel,
    spentAmount: Double,
    allocatedBudget: Double,
    monthLabel: String
  ) {
    if (alertLevel == BudgetAlertLevel.SAFE) return

    // Ensure notification channel is created
    initChannel(context)

    // Check permission for Android 13+ (TIRAMISU)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      val hasPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
      ) == PackageManager.PERMISSION_GRANTED
      if (!hasPermission) {
        Log.w(TAG, "Izin POST_NOTIFICATIONS belum diberikan oleh pengguna.")
        return
      }
    }

    val (title, message) = when (alertLevel) {
      BudgetAlertLevel.OVER_BUDGET -> {
        val overAmount = spentAmount - allocatedBudget
        Pair(
          "🚨 Budget $categoryName Terlampaui!",
          "Total pengeluaran ${FormatUtils.formatRupiah(spentAmount)} telah melampaui limit ${FormatUtils.formatRupiah(allocatedBudget)} untuk $monthLabel. Overbudget: ${FormatUtils.formatRupiah(overAmount)}."
        )
      }
      BudgetAlertLevel.NEAR_LIMIT -> {
        val percentage = if (allocatedBudget > 0) ((spentAmount / allocatedBudget) * 100).toInt() else 80
        val remaining = (allocatedBudget - spentAmount).coerceAtLeast(0.0)
        Pair(
          "⚠️ Budget $categoryName Mendekati Limit!",
          "Pengeluaran telah mencapai $percentage% dari budget ($monthLabel). Sisa anggaran: ${FormatUtils.formatRupiah(remaining)}."
        )
      }
      BudgetAlertLevel.SAFE -> return
    }

    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
      context,
      categoryName.hashCode(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.ic_dialog_alert)
      .setContentTitle(title)
      .setContentText(message)
      .setStyle(NotificationCompat.BigTextStyle().bigText(message))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .setDefaults(NotificationCompat.DEFAULT_ALL)

    try {
      val notificationManager = NotificationManagerCompat.from(context)
      val notificationId = 1000 + (categoryName.hashCode() % 8999)
      notificationManager.notify(notificationId, builder.build())
    } catch (e: SecurityException) {
      Log.e(TAG, "SecurityException saat mengirim notifikasi", e)
    } catch (e: Exception) {
      Log.e(TAG, "Gagal mengirim notifikasi budget", e)
    }
  }
}
