package com.example.ui.theme

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode(
  val label: String,
  val subtitle: String
) {
  SYSTEM("Ikuti Sistem", "Mengikuti tema otomatis dari pengaturan perangkat"),
  LIGHT("Mode Terang", "Tampilan cerah dengan latar putih bersih"),
  DARK("Mode Gelap", "Tampilan gelap ramah mata untuk penggunaan malam hari")
}

class ThemePreferences(context: Context) {
  private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  private val _themeMode = MutableStateFlow(loadThemeMode())
  val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

  private fun loadThemeMode(): ThemeMode {
    val saved = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
    return try {
      ThemeMode.valueOf(saved)
    } catch (e: Exception) {
      ThemeMode.SYSTEM
    }
  }

  fun setThemeMode(mode: ThemeMode) {
    prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    _themeMode.value = mode
  }

  companion object {
    private const val PREFS_NAME = "keuanganku_theme_prefs"
    private const val KEY_THEME_MODE = "key_theme_mode"
  }
}

val LocalThemePreferences = compositionLocalOf<ThemePreferences> {
  error("ThemePreferences not provided")
}
