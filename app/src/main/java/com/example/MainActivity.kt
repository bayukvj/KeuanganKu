package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import com.example.data.repository.TransactionRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOut
import com.example.ui.finance.FinanceHomeScreen
import com.example.ui.finance.FinanceViewModel
import com.example.ui.theme.LocalThemePreferences
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.ThemePreferences
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val themePreferences = remember { ThemePreferences(this@MainActivity) }
      val currentThemeMode by themePreferences.themeMode.collectAsState()
      val isSystemDark = isSystemInDarkTheme()
      val isDarkTheme = when (currentThemeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
      }

      MyApplicationTheme(darkTheme = isDarkTheme) {
        CompositionLocalProvider(LocalThemePreferences provides themePreferences) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .safeDrawingPadding()
          ) {
            AppNavigation(themePreferences = themePreferences)
          }
        }
      }
    }
  }
}

@Composable
fun AppNavigation(themePreferences: ThemePreferences) {
  var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

  DisposableEffect(Unit) {
    val listener = FirebaseAuth.AuthStateListener { auth ->
      currentUser = auth.currentUser
    }
    Firebase.auth.addAuthStateListener(listener)
    onDispose {
      Firebase.auth.removeAuthStateListener(listener)
    }
  }

  val user = currentUser
  if (user == null) {
    AuthScreen(
      onAuthSuccess = {
        currentUser = Firebase.auth.currentUser
      }
    )
  } else {
    val context = LocalContext.current
    val repository = remember(user.uid) {
      TransactionRepository(context)
    }
    val viewModel = remember(user.uid) {
      FinanceViewModel(repository, user.uid)
    }
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    FinanceHomeScreen(
      viewModel = viewModel,
      themePreferences = themePreferences,
      onSignOut = {
        signOut(
          context = context,
          credentialManager = credentialManager,
          onSignOutComplete = {
            currentUser = null
          },
          scope = coroutineScope
        )
      }
    )
  }
}

