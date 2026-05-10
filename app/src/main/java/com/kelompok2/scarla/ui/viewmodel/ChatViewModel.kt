package com.kelompok2.scarla.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.kelompok2.scarla.firebase.FirestoreInitializer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ── Model ──────────────────────────────────────────────────────────────────

data class ChatMessage(
    val id: String = "",
    val senderUid: String = "",
    val text: String = "",
    val timestampMillis: Long = 0L,
    val isRead: Boolean = false
)

data class ChatContact(
    val uid: String = "",
    val name: String = "",
    val avatarUrl: String = "",
    val lastMessage: String = "",
    val lastMessageMillis: Long = 0L,
    val unreadCount: Int = 0
)

data class ChatUiState(
    val contacts: List<ChatContact> = emptyList(),
    val messages: List<ChatMessage> = emptyList(),
    val currentMessage: String = "",
    val isLoadingContacts: Boolean = true,
    val isLoadingMessages: Boolean = false,
    val activeChatId: String? = null,
    val activePeerName: String = "",
    val activePeerAvatar: String = "",
    val activePeerIsOnline: Boolean = false
)

// ── ViewModel ──────────────────────────────────────────────────────────────

class ChatViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "ChatViewModel"

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    /**
     * REAKTIF: Flow daftar percakapan (contacts) — otomatis update
     * saat ada pesan masuk dari siapa pun.
     * Digunakan di halaman daftar chat.
     */
    val contactsFlow: StateFlow<List<ChatContact>> = callbackFlow {
        val myUid = auth.currentUser?.uid
        if (myUid == null) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }

        // Query semua chat yang melibatkan user ini
        val listener = db.collection("messages")
            .whereArrayContains("participants", myUid)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    Log.e(TAG, "Contacts listener error: ${error.message}")
                    return@addSnapshotListener
                }

                val contacts = snap?.documents?.mapNotNull { doc ->
                    @Suppress("UNCHECKED_CAST")
                    val participants = doc.get("participants") as? List<String> ?: return@mapNotNull null
                    val peerUid = participants.firstOrNull { it != myUid } ?: return@mapNotNull null
                    val lastMsg = doc.getString("lastMessage") ?: ""
                    val lastTs = doc.getTimestamp("lastMessageAt")?.toDate()?.time ?: 0L
                    val peerName = doc.getString("peerName_$peerUid") ?: ""
                    val peerAvatar = doc.getString("peerAvatar_$peerUid") ?: ""

                    ChatContact(
                        uid = peerUid,
                        name = peerName,
                        avatarUrl = peerAvatar,
                        lastMessage = lastMsg,
                        lastMessageMillis = lastTs
                    )
                } ?: emptyList()

                trySend(contacts.sortedByDescending { it.lastMessageMillis })
            }

        awaitClose { listener.remove() }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    // Listener aktif untuk chat yang sedang dibuka
    private var activeMessageListener: ListenerRegistration? = null
    private var activeUserListener: ListenerRegistration? = null

    /**
     * REAKTIF: Mulai mendengarkan pesan dari chat spesifik.
     * Dipanggil saat user membuka halaman chat dengan seseorang.
     * Pesan baru otomatis muncul tanpa refresh manual.
     */
    fun openChat(peerUid: String, peerName: String, peerAvatar: String) {
        val myUid = auth.currentUser?.uid ?: return
        val chatId = buildChatId(myUid, peerUid)

        // Hentikan listener sebelumnya jika ada
        activeMessageListener?.remove()
        activeUserListener?.remove()

        _uiState.update {
            it.copy(
                isLoadingMessages = true,
                activeChatId = chatId,
                activePeerName = peerName,
                activePeerAvatar = peerAvatar,
                activePeerIsOnline = false,
                messages = emptyList()
            )
        }

        // Ambil avatar, nama, dan status online secara real-time
        activeUserListener = db.collection("users").document(peerUid).addSnapshotListener { snap, _ ->
            if (snap != null && snap.exists()) {
                val avatar = snap.getString("avatarUrl") ?: snap.getString("avatar") ?: ""
                val name = snap.getString("name") ?: peerName
                val isOnline = snap.getBoolean("isOnline") ?: false
                _uiState.update {
                    it.copy(
                        activePeerName = name,
                        activePeerAvatar = avatar.ifBlank { it.activePeerAvatar },
                        activePeerIsOnline = isOnline
                    )
                }
            }
        }

        // REAKTIF: subscribe ke sub-koleksi chats dengan ordering timestamp
        activeMessageListener = db.collection("messages")
            .document(chatId)
            .collection("chats")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    Log.e(TAG, "Message listener error: ${error.message}")
                    _uiState.update { it.copy(isLoadingMessages = false) }
                    return@addSnapshotListener
                }

                val messages = snap?.documents?.map { doc ->
                    ChatMessage(
                        id = doc.id,
                        senderUid = doc.getString("senderUid") ?: "",
                        text = doc.getString("text") ?: "",
                        timestampMillis = doc.getTimestamp("timestamp")?.toDate()?.time ?: 0L,
                        isRead = doc.getBoolean("isRead") ?: false
                    )
                } ?: emptyList()

                _uiState.update {
                    it.copy(
                        messages = messages,
                        isLoadingMessages = false
                    )
                }
            }
    }

    /** Tutup chat dan bersihkan listener */
    fun closeChat() {
        activeMessageListener?.remove()
        activeUserListener?.remove()
        activeMessageListener = null
        activeUserListener = null
        _uiState.update {
            it.copy(
                activeChatId = null,
                activePeerName = "",
                activePeerAvatar = "",
                messages = emptyList(),
                currentMessage = ""
            )
        }
    }

    /** Update field input pesan */
    fun onMessageChange(text: String) {
        _uiState.update { it.copy(currentMessage = text) }
    }

    /**
     * ASINKRONUS: Kirim pesan — suspend fun dipanggil dari coroutine scope.
     * Setelah kirim, snapshot listener otomatis mengambil pesan baru (reaktif).
     */
    fun sendMessage() {
        val toUid = _uiState.value.activeChatId
            ?.split("_")
            ?.firstOrNull { it != auth.currentUser?.uid }
            ?: return
        val text = _uiState.value.currentMessage.trim()
        if (text.isBlank()) return

        // Optimistik: kosongkan input segera
        _uiState.update { it.copy(currentMessage = "") }

        viewModelScope.launch {
            try {
                FirestoreInitializer.sendMessage(toUid, text)
            } catch (e: Exception) {
                Log.e(TAG, "sendMessage error: ${e.message}", e)
                // Kembalikan teks jika gagal
                _uiState.update { it.copy(currentMessage = text) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        activeMessageListener?.remove()
    }

    private fun buildChatId(uid1: String, uid2: String): String =
        if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
}
