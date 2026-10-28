package com.example.base

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
@LooperMode(LooperMode.Mode.INSTRUMENTATION_TEST)
abstract class FirestoreEmulatorTestBase {

  protected lateinit var firestore: FirebaseFirestore
  protected lateinit var auth: FirebaseAuth
  protected lateinit var databaseId: String

  @Before
  open fun setUpFirebase() {
    val isEmulatorReachable = try {
      java.net.Socket().use { socket ->
        socket.connect(java.net.InetSocketAddress(EMULATOR_HOST, FIRESTORE_PORT), 500)
        true
      }
    } catch (_: Exception) {
      false
    }
    val isAuthReachable = try {
      java.net.Socket().use { socket ->
        socket.connect(java.net.InetSocketAddress(EMULATOR_HOST, AUTH_PORT), 500)
        true
      }
    } catch (_: Exception) {
      false
    }
    org.junit.Assume.assumeTrue("Firebase Firestore/Auth emulators not running", isEmulatorReachable && isAuthReachable)

    val context = ApplicationProvider.getApplicationContext<Context>()
    val resId = context.resources.getIdentifier("firestore_database_id", "string", context.packageName)
    databaseId = if (resId != 0) context.getString(resId) else "ai-studio-android-keuangan-14b16ee2-6bd2-46b6-b6fd-55b5cd016624"

    val app = if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(
        context,
        FirebaseOptions.Builder()
          .setApplicationId("com.aistudio.keuangan.qnvbsp")
          .setProjectId(PROJECT_ID)
          .setApiKey("fake-api-key-for-emulator")
          .build()
      )
    } else {
      FirebaseApp.getInstance()
    }

    firestore = FirebaseFirestore.getInstance(app, databaseId)
    try {
      firestore.useEmulator(EMULATOR_HOST, FIRESTORE_PORT)
      firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
        .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
        .build()
    } catch (_: Exception) {}

    auth = FirebaseAuth.getInstance(app)
    try {
      auth.useEmulator(EMULATOR_HOST, AUTH_PORT)
    } catch (_: Exception) {}
  }

  @After
  open fun tearDownFirebase() {
    if (::auth.isInitialized) {
      try {
        auth.signOut()
      } catch (_: Exception) {}
    }
  }

  protected suspend fun signInTestUser(email: String): String = withContext(Dispatchers.IO) {
    withTimeout(AUTH_TIMEOUT_MS) {
      val currentUser = auth.currentUser
      if (currentUser != null && currentUser.email == email) {
        return@withTimeout currentUser.uid
      }
      try {
        val res = auth.signInWithEmailAndPassword(email, DEFAULT_PASSWORD).await()
        checkNotNull(res.user?.uid) { "User auth failed" }
      } catch (e: Exception) {
        try {
          val res = auth.createUserWithEmailAndPassword(email, DEFAULT_PASSWORD).await()
          checkNotNull(res.user?.uid) { "User creation failed" }
        } catch (_: Exception) {
          val res = auth.signInWithEmailAndPassword(email, DEFAULT_PASSWORD).await()
          checkNotNull(res.user?.uid) { "User auth retry failed" }
        }
      }
    }
  }

  protected fun Throwable.unwrapFirestoreException(): FirebaseFirestoreException? {
    var current: Throwable? = this
    while (current != null) {
      if (current is FirebaseFirestoreException) return current
      current = current.cause
    }
    return null
  }

  private companion object {
    const val EMULATOR_HOST = "127.0.0.1"
    const val FIRESTORE_PORT = 8085
    const val AUTH_PORT = 9099
    const val PROJECT_ID = "demo-no-project"
    const val DEFAULT_PASSWORD = "password123"
    const val AUTH_TIMEOUT_MS = 5000L
  }
}
