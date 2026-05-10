package com.kelompok2.scarla.firebase

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * FirestoreInitializer
 *
 * Tanggung jawab:
 *  - Membuat dokumen user pertama kali saat register/login
 *  - Menyiapkan sub-koleksi: streaks, achievements, friends, friendRequests
 *  - Menyiapkan koleksi global: messages, communityFeed
 *
 * Dipanggil sekali saja dari ViewModel setelah autentikasi berhasil.
 *
 * ──────────────────────────────────────────────
 * STRUKTUR FIRESTORE:
 *
 * users/{uid}
 *   ├── name, email, avatarUrl, educationStatus, origin,
 *   │   interests[], mbti, friendCount, lessonsCompleted,
 *   │   createdAt, updatedAt
 *   │
 *   ├── streaks/{uid}   (dokumen tunggal)
 *   │     currentStreak, longestStreak, lastActiveDate, weeklyDays[7 bool]
 *   │
 *   ├── achievements/{achievementId}
 *   │     id, title, subtitle, imageRes, isUnlocked, unlockedAt, progress, target
 *   │
 *   └── friends/{friendUid}
 *         uid, name, avatarUrl, addedAt
 *
 * friendRequests/{requestId}
 *   fromUid, toUid, message, status (pending/accepted/rejected), sentAt
 *
 * messages/{chatId}
 *   participants[uid1, uid2]
 *   └── chats/{messageId}
 *         senderUid, text, timestamp, isRead
 *
 * communityFeed/{feedId}
 *   uid, userName, avatarUrl, type (achievement/lesson/friend), text, timestamp
 *
 * ──────────────────────────────────────────────
 */
object FirestoreInitializer {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private const val TAG = "FirestoreInitializer"

    // ─── Definisi achievement awal ───────────────────────────────────────
    private val DEFAULT_ACHIEVEMENTS = listOf(
        mapOf(
            "id" to "perjalanan_pemula",
            "title" to "Perjalanan Pemula",
            "subtitle" to "Selesaikan pelajaran pertama",
            "imageRes" to "achievement_1",
            "isUnlocked" to false,
            "unlockedAt" to null,
            "progress" to 0,
            "target" to 1,
            "type" to "lesson"
        ),
        mapOf(
            "id" to "bola_api",
            "title" to "Bola Api",
            "subtitle" to "Streak selama 3 hari",
            "imageRes" to "achievement_2",
            "isUnlocked" to false,
            "unlockedAt" to null,
            "progress" to 0,
            "target" to 3,
            "type" to "streak"
        ),
        mapOf(
            "id" to "kembang_api",
            "title" to "Kembang Api",
            "subtitle" to "Streak selama 14 hari",
            "imageRes" to "achievement_3",
            "isUnlocked" to false,
            "unlockedAt" to null,
            "progress" to 0,
            "target" to 14,
            "type" to "streak"
        ),
        mapOf(
            "id" to "komet",
            "title" to "Komet",
            "subtitle" to "Streak selama 30 hari",
            "imageRes" to "achievement_4",
            "isUnlocked" to false,
            "unlockedAt" to null,
            "progress" to 0,
            "target" to 30,
            "type" to "streak"
        ),
        mapOf(
            "id" to "meteor",
            "title" to "Meteor",
            "subtitle" to "Streak selama 2 bulan",
            "imageRes" to "achievement_5",
            "isUnlocked" to false,
            "unlockedAt" to null,
            "progress" to 0,
            "target" to 60,
            "type" to "streak"
        ),
        mapOf(
            "id" to "si_paling_ambis",
            "title" to "Si Paling Ambis",
            "subtitle" to "Menyelesaikan 10 pelajaran",
            "imageRes" to "achievement_6",
            "isUnlocked" to false,
            "unlockedAt" to null,
            "progress" to 0,
            "target" to 10,
            "type" to "lesson"
        ),
        mapOf(
            "id" to "si_paling_friendly",
            "title" to "Si Paling Friendly",
            "subtitle" to "Memiliki 10 Teman",
            "imageRes" to "achievement_7",
            "isUnlocked" to false,
            "unlockedAt" to null,
            "progress" to 0,
            "target" to 10,
            "type" to "friend"
        )
    )

    // ─── Dokumen streak awal ──────────────────────────────────────────────
    private fun defaultStreak(): Map<String, Any?> = mapOf(
        "currentStreak" to 0,
        "longestStreak" to 0,
        "lastActiveDate" to null,
        "weeklyDays" to listOf(false, false, false, false, false, false, false), // Sen-Min
        "updatedAt" to FieldValue.serverTimestamp()
    )

    /**
     * Pastikan semua sub-koleksi user sudah ada.
     * AMAN dipanggil berulang kali (idempotent via SetOptions.merge).
     *
     * Dipanggil setiap kali user LOGIN — untuk menangani:
     *  1. User baru yang belum punya sub-koleksi
     *  2. User lama yang daftar sebelum FirestoreInitializer dibuat
     *
     * Tidak menimpa data yang sudah ada karena pakai SetOptions.merge().
     */
    suspend fun ensureUserInitialized(uid: String) = withContext(Dispatchers.IO) {
        try {
            // 1. Cek/buat streak document
            val streakRef = db.collection("users")
                .document(uid)
                .collection("streaks")
                .document(uid)

            val streakSnap = streakRef.get().await()
            if (!streakSnap.exists()) {
                Log.d(TAG, "⚙️ Streak belum ada untuk uid=$uid, membuat...")
                initStreak(uid)
            } else {
                // Streak sudah ada — sync streakCount ke user document untuk ProfilScreen
                val currentStreak = (streakSnap.getLong("currentStreak") ?: 0).toInt()
                db.collection("users").document(uid).set(
                    mapOf("streakCount" to currentStreak),
                    SetOptions.merge()
                ).await()
                Log.d(TAG, "✅ streakCount=$currentStreak di-sync ke user document")

                // Sync progress achievement streak dengan nilai streak saat ini
                syncStreakAchievements(uid, currentStreak)
            }

            // 2. Cek/buat achievements document
            val achieveSnap = db.collection("users")
                .document(uid)
                .collection("achievements")
                .limit(1)
                .get()
                .await()

            if (achieveSnap.isEmpty) {
                Log.d(TAG, "⚙️ Achievements belum ada untuk uid=$uid, membuat...")
                initAchievements(uid)
            }

            Log.d(TAG, "✅ ensureUserInitialized selesai untuk uid=$uid")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ensureUserInitialized gagal: ${e.message}", e)
        }
    }

    /**
     * Sync progress achievement streak dengan nilai currentStreak.
     * Dipanggil saat login untuk memperbaiki data stale.
     * Jika streak >= target achievement, juga unlock achievement-nya.
     */
    private suspend fun syncStreakAchievements(uid: String, currentStreak: Int) {
        val streakAchievements = listOf(
            "bola_api" to 3,
            "kembang_api" to 14,
            "komet" to 30,
            "meteor" to 60
        )
        val userSnap = db.collection("users").document(uid).get().await()
        val name = userSnap.getString("name") ?: "Pengguna"
        val avatarUrl = userSnap.getString("avatarUrl") ?: ""

        streakAchievements.forEach { (id, target) ->
            try {
                val achievRef = db.collection("users")
                    .document(uid)
                    .collection("achievements")
                    .document(id)

                val snap = achievRef.get().await()
                if (!snap.exists()) return@forEach

                val isAlreadyUnlocked = snap.getBoolean("isUnlocked") ?: false

                // Update progress ke streak saat ini
                val updateMap = mutableMapOf<String, Any>("progress" to currentStreak)

                // Jika sudah memenuhi target tapi belum unlock → unlock sekarang
                if (!isAlreadyUnlocked && currentStreak >= target) {
                    updateMap["isUnlocked"] = true
                    updateMap["unlockedAt"] = com.google.firebase.firestore.FieldValue.serverTimestamp()
                    Log.d(TAG, "🏆 Achievement $id di-unlock (streak=$currentStreak >= target=$target)")
                    postToFeed(uid, name, avatarUrl, "achievement",
                        "$name membuka achievement \"${snap.getString("title") ?: id}\" 🏆")
                }

                achievRef.set(updateMap, SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ syncStreakAchievements gagal untuk $id: ${e.message}")
            }
        }
        Log.d(TAG, "✅ syncStreakAchievements selesai untuk uid=$uid (streak=$currentStreak)")
    }

    /**
     * Inisialisasi profil user baru di Firestore.
     * Dipanggil setelah register berhasil.
     *
     * @param name      Nama display user
     * @param email     Email user
     */
    suspend fun initNewUser(
        name: String,
        email: String,
        avatarUrl: String = "",
        educationStatus: String = "",
        origin: String = "",
        interests: List<String> = emptyList(),
        mbti: String = ""
    ) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: run {
            Log.e(TAG, "initNewUser dipanggil tapi user belum login")
            return@withContext
        }

        try {
            val userRef = db.collection("users").document(uid)

            // 1. Buat / merge dokumen user
            val userDoc = mapOf(
                "uid" to uid,
                "name" to name,
                "email" to email,
                "avatarUrl" to avatarUrl,
                "educationStatus" to educationStatus,
                "origin" to origin,
                "interests" to interests,
                "mbti" to mbti,
                "friendCount" to 0,
                "lessonsCompleted" to 0,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            userRef.set(userDoc, SetOptions.merge()).await()
            Log.d(TAG, "✅ Dokumen user/$uid berhasil dibuat")

            // 2. Buat dokumen streak awal
            initStreak(uid)

            // 3. Buat sub-koleksi achievements
            initAchievements(uid)

            Log.d(TAG, "✅ Inisialisasi user selesai untuk uid=$uid")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Gagal inisialisasi user: ${e.message}", e)
        }
    }

    /**
     * Buat / reset dokumen streak milik user.
     * Aman dipanggil berulang kali (idempotent via merge).
     */
    suspend fun initStreak(uid: String) = withContext(Dispatchers.IO) {
        try {
            db.collection("users")
                .document(uid)
                .collection("streaks")
                .document(uid)               // satu dokumen per user
                .set(defaultStreak(), SetOptions.merge())
                .await()
            Log.d(TAG, "✅ Streak untuk uid=$uid berhasil dibuat")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Gagal inisialisasi streak: ${e.message}", e)
        }
    }

    /**
     * Buat semua achievement awal untuk user.
     * Menggunakan batch write agar atomik.
     */
    suspend fun initAchievements(uid: String) = withContext(Dispatchers.IO) {
        try {
            val batch = db.batch()
            val achievRef = db.collection("users").document(uid).collection("achievements")

            DEFAULT_ACHIEVEMENTS.forEach { achievement ->
                val docId = achievement["id"] as String
                batch.set(achievRef.document(docId), achievement, SetOptions.merge())
            }

            batch.commit().await()
            Log.d(TAG, "✅ ${DEFAULT_ACHIEVEMENTS.size} achievements berhasil dibuat untuk uid=$uid")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Gagal inisialisasi achievements: ${e.message}", e)
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // REACTIVE OPERATIONS — dipanggil dari ViewModel dengan Flow/StateFlow
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Catat sesi belajar selesai.
     * Secara reaktif akan memperbarui:
     *  - lessonsCompleted (+1)
     *  - streak hari ini
     *  - cek & unlock achievement terkait
     *  - post ke communityFeed
     */
    suspend fun recordLessonCompleted(lessonTitle: String) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext
        try {
            val userRef = db.collection("users").document(uid)

            // 1. Increment materialsCompleted di dokumen user (sync ProfilScreen)
            // Gunakan set+merge karena update() gagal jika field belum ada
            userRef.set(
                mapOf(
                    "materialsCompleted" to FieldValue.increment(1),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            ).await()

            // 2. Tulis ke sub-koleksi materials/{title} agar ProfilScreen
            //    bisa menghitung streak dari completedAt tiap materi
            val safeTitle = lessonTitle.replace("/", "-").take(50)
            userRef.collection("materials").document(safeTitle).set(
                mapOf(
                    "subject" to lessonTitle,
                    "completedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            ).await()

            // 3. Update streak harian
            updateDailyStreak(uid)

            // 4. Ambil data terbaru lalu cek achievement
            val snap = userRef.get().await()
            val lessons = (snap.getLong("materialsCompleted") ?: 0).toInt()
            val name = snap.getString("name") ?: "Pengguna"
            val avatarUrl = snap.getString("avatarUrl") ?: ""

            // 5. Cek achievement lesson
            checkAndUnlockAchievement(uid, "perjalanan_pemula", "lesson", lessons, name, avatarUrl)
            checkAndUnlockAchievement(uid, "si_paling_ambis", "lesson", lessons, name, avatarUrl)

            // 6. Post ke community feed
            postToFeed(
                uid = uid, userName = name, avatarUrl = avatarUrl,
                type = "lesson",
                text = "$name menyelesaikan materi \"$lessonTitle\" 🎓"
            )

            Log.d(TAG, "✅ Lesson completed dicatat untuk uid=$uid (total: $lessons)")
        } catch (e: Exception) {
            Log.e(TAG, "❌ recordLessonCompleted gagal: ${e.message}", e)
        }
    }

    /**
     * Update streak harian user.
     * Dipanggil setiap kali user aktif (buka app / selesai belajar).
     */
    suspend fun updateDailyStreak(uid: String) = withContext(Dispatchers.IO) {
        try {
            val streakRef = db.collection("users")
                .document(uid)
                .collection("streaks")
                .document(uid)

            val snap = streakRef.get().await()
            val today = todayDateString()
            val lastActive = snap.getString("lastActiveDate") ?: ""

            if (lastActive == today) return@withContext  // Sudah tercatat hari ini

            val currentStreak = (snap.getLong("currentStreak") ?: 0).toInt()
            val longestStreak = (snap.getLong("longestStreak") ?: 0).toInt()
            val yesterday = yesterdayDateString()

            val newStreak = if (lastActive == yesterday) currentStreak + 1 else 1
            val newLongest = maxOf(newStreak, longestStreak)

            // weeklyDays: index 0=Senin ... 6=Minggu
            val weeklyDays = (snap.get("weeklyDays") as? List<*>)
                ?.map { it as? Boolean ?: false }
                ?.toMutableList()
                ?: MutableList(7) { false }
            val dayIndex = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)
                .let { if (it == java.util.Calendar.SUNDAY) 6 else it - 2 }
            weeklyDays[dayIndex] = true

            // BUGFIX: pakai .set() + merge() agar dokumen dibuat jika belum ada
            // (menggantikan .update() yang gagal jika dokumen tidak ada)
            streakRef.set(
                mapOf(
                    "currentStreak" to newStreak,
                    "longestStreak" to newLongest,
                    "lastActiveDate" to today,
                    "weeklyDays" to weeklyDays,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            ).await()

            // Cek achievement streak
            val userSnap = db.collection("users").document(uid).get().await()
            val name = userSnap.getString("name") ?: "Pengguna"
            val avatarUrl = userSnap.getString("avatarUrl") ?: ""

            listOf(
                "bola_api" to 3,
                "kembang_api" to 14,
                "komet" to 30,
                "meteor" to 60
            ).forEach { (id, _) ->
                checkAndUnlockAchievement(uid, id, "streak", newStreak, name, avatarUrl)
            }

            Log.d(TAG, "✅ Streak uid=$uid → $newStreak hari")
        } catch (e: Exception) {
            Log.e(TAG, "❌ updateDailyStreak gagal: ${e.message}", e)
        }
    }

    /**
     * Kirim permintaan pertemanan.
     * Membuat dokumen di koleksi global friendRequests.
     *
     * @return requestId yang baru dibuat, atau null jika gagal
     */
    suspend fun sendFriendRequest(
        toUid: String,
        message: String = ""
    ): String? = withContext(Dispatchers.IO) {
        val fromUid = auth.currentUser?.uid ?: return@withContext null
        try {
            val requestId = "${fromUid}_${toUid}"   // idempotent: satu request per pasangan
            val data = mapOf(
                "id" to requestId,
                "fromUid" to fromUid,
                "toUid" to toUid,
                "message" to message,
                "status" to "pending",
                "sentAt" to FieldValue.serverTimestamp()
            )
            db.collection("friendRequests").document(requestId).set(data).await()
            Log.d(TAG, "✅ Friend request $fromUid → $toUid dikirim")
            requestId
        } catch (e: Exception) {
            Log.e(TAG, "❌ sendFriendRequest gagal: ${e.message}", e)
            null
        }
    }

    /**
     * Terima permintaan pertemanan.
     * Secara atomik (batch):
     *  1. Update status request → accepted
     *  2. Tambah ke friends sub-koleksi kedua user
     *  3. Increment friendCount kedua user
     *  4. Cek achievement si_paling_friendly
     *  5. Post ke communityFeed
     */
    suspend fun acceptFriendRequest(requestId: String) = withContext(Dispatchers.IO) {
        val currentUid = auth.currentUser?.uid ?: return@withContext
        try {
            val requestSnap = db.collection("friendRequests").document(requestId).get().await()
            val fromUid = requestSnap.getString("fromUid") ?: return@withContext
            val toUid = requestSnap.getString("toUid") ?: return@withContext

            // Ambil data kedua user
            val fromSnap = db.collection("users").document(fromUid).get().await()
            val toSnap = db.collection("users").document(toUid).get().await()

            val batch = db.batch()

            // 1. Update status request
            batch.update(
                db.collection("friendRequests").document(requestId),
                "status", "accepted",
                "acceptedAt", FieldValue.serverTimestamp()
            )

            // 2. Tambah ke friends masing-masing
            batch.set(
                db.collection("users").document(fromUid).collection("friends").document(toUid),
                mapOf(
                    "uid" to toUid,
                    "name" to (toSnap.getString("name") ?: ""),
                    "avatarUrl" to (toSnap.getString("avatarUrl") ?: ""),
                    "addedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                db.collection("users").document(toUid).collection("friends").document(fromUid),
                mapOf(
                    "uid" to fromUid,
                    "name" to (fromSnap.getString("name") ?: ""),
                    "avatarUrl" to (fromSnap.getString("avatarUrl") ?: ""),
                    "addedAt" to FieldValue.serverTimestamp()
                )
            )

            // 3. Increment friendCount
            batch.update(
                db.collection("users").document(fromUid),
                "friendCount", FieldValue.increment(1)
            )
            batch.update(
                db.collection("users").document(toUid),
                "friendCount", FieldValue.increment(1)
            )

            batch.commit().await()

            // 4. Cek achievement si_paling_friendly (setelah batch commit)
            listOf(fromUid, toUid).forEach { uid ->
                val snap = db.collection("users").document(uid).get().await()
                val friendCount = (snap.getLong("friendCount") ?: 0).toInt()
                val name = snap.getString("name") ?: "Pengguna"
                val avatarUrl = snap.getString("avatarUrl") ?: ""
                checkAndUnlockAchievement(uid, "si_paling_friendly", "friend", friendCount, name, avatarUrl)
            }

            // 5. Post community feed
            val toName = toSnap.getString("name") ?: "Pengguna"
            val toAvatar = toSnap.getString("avatarUrl") ?: ""
            postToFeed(
                uid = toUid, userName = toName, avatarUrl = toAvatar,
                type = "friend",
                text = "$toName dan ${fromSnap.getString("name")} kini berteman 🤝"
            )

            Log.d(TAG, "✅ Friend request $requestId diterima")
        } catch (e: Exception) {
            Log.e(TAG, "❌ acceptFriendRequest gagal: ${e.message}", e)
        }
    }

    /**
     * Tolak permintaan pertemanan.
     */
    suspend fun rejectFriendRequest(requestId: String) = withContext(Dispatchers.IO) {
        try {
            db.collection("friendRequests").document(requestId)
                .update("status", "rejected", "rejectedAt", FieldValue.serverTimestamp())
                .await()
            Log.d(TAG, "✅ Friend request $requestId ditolak")
        } catch (e: Exception) {
            Log.e(TAG, "❌ rejectFriendRequest gagal: ${e.message}", e)
        }
    }

    /**
     * Kirim pesan chat.
     * chatId = uid1_uid2 (uid kecil duluan agar deterministik).
     */
    suspend fun sendMessage(
        toUid: String,
        text: String
    ) = withContext(Dispatchers.IO) {
        val fromUid = auth.currentUser?.uid ?: return@withContext
        try {
            val chatId = buildChatId(fromUid, toUid)
            val chatRef = db.collection("messages").document(chatId)

            // Ambil data user dari server untuk metadata chat
            val fromSnap = db.collection("users").document(fromUid).get().await()
            val toSnap = db.collection("users").document(toUid).get().await()

            val fromName = fromSnap.getString("name") ?: ""
            val fromAvatar = fromSnap.getString("avatarUrl") ?: fromSnap.getString("avatar") ?: ""
            
            val toName = toSnap.getString("name") ?: ""
            val toAvatar = toSnap.getString("avatarUrl") ?: toSnap.getString("avatar") ?: ""

            // Pastikan dokumen chat root ada dan update info terakhir
            chatRef.set(
                mapOf(
                    "participants" to listOf(fromUid, toUid),
                    "lastMessage" to text,
                    "lastMessageAt" to FieldValue.serverTimestamp(),
                    "peerName_$fromUid" to fromName,
                    "peerAvatar_$fromUid" to fromAvatar,
                    "peerName_$toUid" to toName,
                    "peerAvatar_$toUid" to toAvatar
                ),
                SetOptions.merge()
            ).await()

            // Tambah pesan ke sub-koleksi chats
            chatRef.collection("chats").add(
                mapOf(
                    "senderUid" to fromUid,
                    "text" to text,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "isRead" to false
                )
            ).await()

            Log.d(TAG, "✅ Pesan dikirim ke chat $chatId")
        } catch (e: Exception) {
            Log.e(TAG, "❌ sendMessage gagal: ${e.message}", e)
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────

    /**
     * Cek dan unlock achievement jika progress >= target.
     * Juga memperbarui progress achievement.
     */
    private suspend fun checkAndUnlockAchievement(
        uid: String,
        achievementId: String,
        type: String,
        currentValue: Int,
        userName: String,
        avatarUrl: String
    ) {
        try {
            val achievRef = db.collection("users")
                .document(uid)
                .collection("achievements")
                .document(achievementId)

            val snap = achievRef.get().await()

            // Jika dokumen achievement belum ada, skip (akan dibuat oleh ensureUserInitialized)
            if (!snap.exists()) {
                Log.w(TAG, "⚠️ Achievement $achievementId belum ada untuk uid=$uid, skip cek")
                return
            }

            val isAlreadyUnlocked = snap.getBoolean("isUnlocked") ?: false
            if (isAlreadyUnlocked) return

            val target = (snap.getLong("target") ?: Long.MAX_VALUE).toInt()

            // BUGFIX: pakai .set() + merge() agar tidak gagal jika dokumen kosong
            achievRef.set(mapOf("progress" to currentValue), SetOptions.merge()).await()

            if (currentValue >= target) {
                achievRef.set(
                    mapOf(
                        "isUnlocked" to true,
                        "unlockedAt" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                ).await()

                val title = snap.getString("title") ?: achievementId
                Log.d(TAG, "🏆 Achievement UNLOCK: $title untuk uid=$uid")

                // Post ke feed
                postToFeed(
                    uid = uid, userName = userName, avatarUrl = avatarUrl,
                    type = "achievement",
                    text = "$userName membuka achievement \"$title\" 🏆"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ checkAndUnlockAchievement ($achievementId) gagal: ${e.message}", e)
        }
    }

    /**
     * Post aktivitas ke communityFeed global.
     * Digunakan untuk achievement unlock, lesson selesai, dan pertemanan baru.
     */
    private suspend fun postToFeed(
        uid: String,
        userName: String,
        avatarUrl: String,
        type: String,   // "achievement" | "lesson" | "friend"
        text: String
    ) {
        try {
            db.collection("communityFeed").add(
                mapOf(
                    "uid" to uid,
                    "userName" to userName,
                    "avatarUrl" to avatarUrl,
                    "type" to type,
                    "text" to text,
                    "timestamp" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            Log.e(TAG, "❌ postToFeed gagal: ${e.message}", e)
        }
    }

    /** Buat chatId deterministik dari dua uid (uid terkecil selalu duluan). */
    private fun buildChatId(uid1: String, uid2: String): String =
        if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"

    /** Format tanggal hari ini: yyyy-MM-dd */
    private fun todayDateString(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    /** Format tanggal kemarin: yyyy-MM-dd */
    private fun yesterdayDateString(): String {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(cal.time)
    }
}
