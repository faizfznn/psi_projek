package com.kelompok2.scarla.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kelompok2.scarla.firebase.FirestoreInitializer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StreakUiState(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val weeklyDays: List<Boolean> = List(7) { false },  // Sen=0 ... Min=6
    val isLoading: Boolean = true
)

class StreakViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "StreakViewModel"

    /**
     * REAKTIF: StateFlow yang otomatis diperbarui setiap kali dokumen
     * streaks/{uid} berubah di Firestore (real-time snapshot listener).
     * Di-collect oleh HomeScreen & ScreenStreak via collectAsStateWithLifecycle().
     */
    val streakState: StateFlow<StreakUiState> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(StreakUiState(isLoading = false))
            awaitClose {}
            return@callbackFlow
        }

        // addSnapshotListener = subscribe REAKTIF ke Firestore
        val listener = db.collection("users")
            .document(uid)
            .collection("streaks")
            .document(uid)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    Log.e(TAG, "Streak listener error: ${error.message}")
                    trySend(StreakUiState(isLoading = false))
                    return@addSnapshotListener
                }

                val currentStreak = (snap?.getLong("currentStreak") ?: 0).toInt()
                val longestStreak = (snap?.getLong("longestStreak") ?: 0).toInt()
                @Suppress("UNCHECKED_CAST")
                val weeklyDays = (snap?.get("weeklyDays") as? List<Boolean>)
                    ?: List(7) { false }

                trySend(
                    StreakUiState(
                        currentStreak = currentStreak,
                        longestStreak = longestStreak,
                        weeklyDays = weeklyDays,
                        isLoading = false
                    )
                )
            }

        awaitClose { listener.remove() }   // cleanup saat scope dibatalkan
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StreakUiState(isLoading = true)
    )

    /**
     * ASINKRONUS: Catat aktivitas harian user (buka app / selesai belajar).
     * Dipanggil dari ScreenStreak atau HomeScreen.
     * Hasilnya otomatis ter-reflect ke streakState karena snapshot listener.
     */
    fun recordDailyActivity() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                FirestoreInitializer.updateDailyStreak(uid)
                Log.d(TAG, "Daily activity recorded")
            } catch (e: Exception) {
                Log.e(TAG, "recordDailyActivity error: ${e.message}", e)
            }
        }
    }
}
