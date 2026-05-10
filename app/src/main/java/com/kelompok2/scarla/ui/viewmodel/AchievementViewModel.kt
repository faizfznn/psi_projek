package com.kelompok2.scarla.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class AchievementFirestore(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val imageRes: String = "",          // nama drawable string, e.g. "achievement_1"
    val isUnlocked: Boolean = false,
    val progress: Int = 0,
    val target: Int = 1,
    val type: String = ""               // "streak" | "lesson" | "friend"
)

data class AchievementPageUiState(
    val achievements: List<AchievementFirestore> = emptyList(),
    val isLoading: Boolean = true,
    val newlyUnlocked: AchievementFirestore? = null  // trigger popup notifikasi
)

class AchievementViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "AchievementViewModel"

    /**
     * REAKTIF: StateFlow achievements diperbarui otomatis setiap kali
     * sub-koleksi achievements/{uid} berubah di Firestore.
     * Ini mencakup: progress, isUnlocked, unlockedAt.
     */
    val achievementState: StateFlow<AchievementPageUiState> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(AchievementPageUiState(isLoading = false))
            awaitClose {}
            return@callbackFlow
        }

        var previousUnlockedIds = emptySet<String>()

        // REAKTIF: snapshot listener di-subscribe ke koleksi achievements
        val listener = db.collection("users")
            .document(uid)
            .collection("achievements")
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    Log.e(TAG, "Achievement listener error: ${error.message}")
                    trySend(AchievementPageUiState(isLoading = false))
                    return@addSnapshotListener
                }

                val list = snap?.documents?.map { doc ->
                    AchievementFirestore(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        subtitle = doc.getString("subtitle") ?: "",
                        imageRes = doc.getString("imageRes") ?: "",
                        isUnlocked = doc.getBoolean("isUnlocked") ?: false,
                        progress = (doc.getLong("progress") ?: 0).toInt(),
                        target = (doc.getLong("target") ?: 1).toInt(),
                        type = doc.getString("type") ?: ""
                    )
                } ?: emptyList()

                // Deteksi achievement yang baru saja di-unlock (reaktif notification)
                val currentUnlockedIds = list.filter { it.isUnlocked }.map { it.id }.toSet()
                val newlyUnlockedId = (currentUnlockedIds - previousUnlockedIds).firstOrNull()
                val newlyUnlocked = if (newlyUnlockedId != null)
                    list.firstOrNull { it.id == newlyUnlockedId } else null

                previousUnlockedIds = currentUnlockedIds

                if (newlyUnlocked != null) {
                    _showUnlockBanner.update { newlyUnlocked }
                }

                trySend(
                    AchievementPageUiState(
                        achievements = list,
                        isLoading = false,
                        newlyUnlocked = null // Tidak lagi digunakan, dialihkan ke _showUnlockBanner
                    )
                )
            }

        awaitClose { listener.remove() }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AchievementPageUiState(isLoading = true)
    )

    /** Hapus notifikasi popup setelah user mengakui achievement baru */
    private val _showUnlockBanner = MutableStateFlow<AchievementFirestore?>(null)
    val showUnlockBanner: StateFlow<AchievementFirestore?> = _showUnlockBanner.asStateFlow()

    fun dismissUnlockBanner() {
        _showUnlockBanner.update { null }
    }
}
