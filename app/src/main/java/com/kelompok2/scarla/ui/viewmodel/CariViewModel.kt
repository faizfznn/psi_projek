package com.kelompok2.scarla.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kelompok2.scarla.firebase.FirestoreInitializer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.tasks.await

// Models
data class FriendProfile(
    val id: String,
    val name: String,
    val age: Int,
    val educationStatus: String,
    val origin: String,
    val interests: List<String>,
    val matchCount: Int,
    val avatarResId: Int = 0,
    val avatarString: String = "",
    val isFriend: Boolean = false
)

enum class CardSwipeDirection {
    LEFT,
    RIGHT
}

data class CariUiState(
    val friendRequests: List<FriendProfile> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<FriendProfile> = emptyList(),
    val selectedFriend: FriendProfile? = null,
    val requestMessage: String = "",
    val showRequestPopup: Boolean = false,
    val showRequestSubmittedPopup: Boolean = false,
    val showFriendAcceptedPopup: Boolean = false,
    val swipeDirection: CardSwipeDirection? = null
)

class CariViewModel(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {
    companion object {
        internal const val CARD_ANIMATION_DURATION_MS = 320L
        private const val TAG = "CariViewModel"
    }

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(CariUiState())
    val uiState: StateFlow<CariUiState> = _uiState.asStateFlow()

    init {
        observeFriendRequests()
    }

    private var fetchJob: kotlinx.coroutines.Job? = null

    private fun observeFriendRequests() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("friendRequests")
            .whereEqualTo("toUid", uid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Failed to observe friend requests", error)
                    return@addSnapshotListener
                }

                fetchJob?.cancel()
                fetchJob = viewModelScope.launch(Dispatchers.IO) {
                    val requests = snapshot?.documents ?: emptyList()
                    val profiles = mutableListOf<FriendProfile>()
                    
                    for (doc in requests) {
                        val fromUid = doc.getString("fromUid") ?: continue
                        val requestId = doc.id
                        
                        try {
                            val userSnap = db.collection("users").document(fromUid).get().await()
                            if (userSnap.exists()) {
                                val name = userSnap.getString("name") ?: "Pengguna"
                                val age = calculateAge(userSnap.getTimestamp("birthDate")) ?: 20
                                val origin = userSnap.getString("city") ?: ""
                                val education = userSnap.getString("educationStatus") ?: ""
                                val avatarStr = userSnap.getString("avatar") ?: "avatar_default"
                                val interests = (userSnap.get("favoriteSubjects") as? List<*>)?.map { it.toString() } ?: emptyList()

                                profiles.add(
                                    FriendProfile(
                                        id = requestId, // Penting: id = requestId agar bisa di-accept/reject
                                        name = name,
                                        age = age,
                                        educationStatus = education,
                                        origin = origin,
                                        interests = interests,
                                        matchCount = 0,
                                        avatarString = avatarStr,
                                        avatarResId = 0
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to fetch user $fromUid", e)
                        }
                    }

                    // Hanya update kalau job ini belum di-cancel oleh snapshot berikutnya
                    if (this.isActive) {
                        _uiState.update { it.copy(friendRequests = profiles) }
                    }
                }
            }
    }

    private fun calculateAge(birthDate: com.google.firebase.Timestamp?): Int? {
        val date = birthDate?.toDate() ?: return null
        val now = java.util.Calendar.getInstance()
        val birth = java.util.Calendar.getInstance().apply { time = date }
        var age = now.get(java.util.Calendar.YEAR) - birth.get(java.util.Calendar.YEAR)
        if (now.get(java.util.Calendar.DAY_OF_YEAR) < birth.get(java.util.Calendar.DAY_OF_YEAR)) {
            age -= 1
        }
        return age.takeIf { it >= 0 }
    }

    fun onSearchQueryChange(query: String) {
        val normalizedQuery = query.trim()
        
        if (normalizedQuery.isBlank()) {
            _uiState.update {
                it.copy(
                    searchQuery = normalizedQuery,
                    searchResults = emptyList(),
                    selectedFriend = null,
                    showRequestPopup = false,
                    requestMessage = ""
                )
            }
            return
        }

        val currentUid = auth.currentUser?.uid ?: return
        
        _uiState.update { it.copy(searchQuery = normalizedQuery) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Dapatkan daftar teman saat ini
                val myFriendsSnap = db.collection("users").document(currentUid).collection("friends").get().await()
                val myFriendsUids = myFriendsSnap.documents.map { it.id }.toSet()

                // 2. Fetch all users and filter locally
                val usersSnap = db.collection("users").get().await()
                
                val results = usersSnap.documents.mapNotNull { doc ->
                    if (doc.id == currentUid) return@mapNotNull null // jangan munculkan diri sendiri
                    
                    val name = doc.getString("name") ?: ""
                    val email = doc.getString("email") ?: ""
                    
                    if (name.contains(normalizedQuery, ignoreCase = true) || 
                        email.contains(normalizedQuery, ignoreCase = true)) {
                        
                        val age = calculateAge(doc.getTimestamp("birthDate")) ?: 20
                        val origin = doc.getString("city") ?: ""
                        val education = doc.getString("educationStatus") ?: ""
                        val avatarStr = doc.getString("avatar") ?: "avatar_default"
                        val interests = (doc.get("favoriteSubjects") as? List<*>)?.map { it.toString() } ?: emptyList()
                        
                        FriendProfile(
                            id = doc.id, // target UID untuk di-add
                            name = name,
                            age = age,
                            educationStatus = education,
                            origin = origin,
                            interests = interests,
                            matchCount = 0,
                            avatarString = avatarStr,
                            avatarResId = 0,
                            isFriend = myFriendsUids.contains(doc.id) // Tandai jika sudah berteman
                        )
                    } else null
                }

                _uiState.update {
                    it.copy(
                        searchResults = results,
                        selectedFriend = null,
                        showRequestPopup = false,
                        requestMessage = ""
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Search failed", e)
            }
        }
    }

    fun acceptCurrentRequest() {
        if (_uiState.value.friendRequests.isEmpty()) return
        val currentRequest = _uiState.value.friendRequests.first()
        viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(swipeDirection = CardSwipeDirection.RIGHT) }
            
            // Tunggu animasi swipe selesai sebelum menghapus card dari UI
            delay(CARD_ANIMATION_DURATION_MS)

            _uiState.update { state -> 
                state.copy(
                    friendRequests = state.friendRequests.filterNot { it.id == currentRequest.id },
                    swipeDirection = null,
                    showFriendAcceptedPopup = true
                )
            }

            // Panggil backend secara asinkron agar UI tidak ter-block
            launch(Dispatchers.IO) {
                FirestoreInitializer.acceptFriendRequest(currentRequest.id)
            }
        }
    }

    fun rejectCurrentRequest() {
        if (_uiState.value.friendRequests.isEmpty()) return
        val currentRequest = _uiState.value.friendRequests.first()
        viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(swipeDirection = CardSwipeDirection.LEFT) }
            
            // Tunggu animasi swipe selesai
            delay(CARD_ANIMATION_DURATION_MS)

            _uiState.update { state -> 
                state.copy(
                    friendRequests = state.friendRequests.filterNot { it.id == currentRequest.id },
                    swipeDirection = null
                )
            }

            // Panggil backend secara asinkron
            launch(Dispatchers.IO) {
                FirestoreInitializer.rejectFriendRequest(currentRequest.id)
            }
        }
    }

    fun dismissFriendAcceptedPopup() {
        _uiState.update { it.copy(showFriendAcceptedPopup = false) }
    }

    fun openFriendRequestPopup(friend: FriendProfile) {
        _uiState.update {
            it.copy(
                selectedFriend = friend,
                showRequestPopup = true,
                requestMessage = ""
            )
        }
    }

    fun onRequestMessageChange(message: String) {
        _uiState.update { it.copy(requestMessage = message) }
    }

    fun dismissRequestPopup() {
        _uiState.update {
            it.copy(
                showRequestPopup = false,
                selectedFriend = null,
                requestMessage = ""
            )
        }
    }

    fun submitFriendRequest() {
        val selectedFriend = _uiState.value.selectedFriend ?: return
        val message = _uiState.value.requestMessage
        
        viewModelScope.launch(Dispatchers.IO) {
            FirestoreInitializer.sendFriendRequest(selectedFriend.id, message)
        }

        _uiState.update {
            it.copy(
                showRequestPopup = false,
                selectedFriend = null,
                requestMessage = "",
                showRequestSubmittedPopup = true
            )
        }
    }

    fun dismissRequestSubmittedPopup() {
        _uiState.update { it.copy(showRequestSubmittedPopup = false) }
    }
}
