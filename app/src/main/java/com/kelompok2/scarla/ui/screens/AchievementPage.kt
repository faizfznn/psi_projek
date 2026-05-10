package com.kelompok2.scarla.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kelompok2.scarla.R
import com.kelompok2.scarla.ui.theme.Neutral600
import com.kelompok2.scarla.ui.theme.Neutral900
import com.kelompok2.scarla.ui.theme.Secondary500
import com.kelompok2.scarla.ui.viewmodel.AchievementFirestore
import com.kelompok2.scarla.ui.viewmodel.AchievementViewModel
import kotlinx.coroutines.delay

/**
 * AchievementPage — REAKTIF via AchievementViewModel
 *
 * - Achievement list diambil dari Firestore real-time (bukan statis)
 * - Progress bar update otomatis saat user menyelesaikan materi / streak
 * - Popup "Achievement Unlocked!" muncul otomatis saat achievement baru terbuka
 */
@Composable
fun AchievementPage(
    navController: NavController? = null,
    onBack: (() -> Unit)? = null,
    viewModel: AchievementViewModel = viewModel()
) {
    val scrollState = rememberScrollState()

    // REAKTIF: collectAsStateWithLifecycle — rekomposisi otomatis saat data Firestore berubah
    val state by viewModel.achievementState.collectAsStateWithLifecycle()
    val showUnlockBanner by viewModel.showUnlockBanner.collectAsStateWithLifecycle()

    // Pisah unlocked dan locked untuk tampilan berbeda
    val unlockedList = state.achievements.filter { it.isUnlocked }
    val lockedList = state.achievements.filter { !it.isUnlocked }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(scrollState)
        ) {
            // ── Header ──────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
            ) {
                IconButton(
                    onClick = {
                        if (navController != null) navController.popBackStack()
                        else onBack?.invoke()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(color = Color(0xFFFFC300), shape = RoundedCornerShape(size = 44.dp))
                        .border(width = 1.dp, color = Color(0xFF303030), shape = RoundedCornerShape(size = 44.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Neutral900,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "Achievement",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontFamily = FontFamily(Font(R.font.poppins_bold)),
                        fontWeight = FontWeight(700),
                        color = Neutral900
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                // Badge jumlah terbuka
                if (!state.isLoading) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Secondary500)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${unlockedList.size}/${state.achievements.size}",
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontFamily = FontFamily(Font(R.font.poppins_bold)),
                                fontWeight = FontWeight(700),
                                color = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Loading State ────────────────────────────────────────────
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Secondary500)
                }
            } else {
                // ── Unlocked Achievements ────────────────────────────────
                if (unlockedList.isNotEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "✅ Sudah Diraih (${unlockedList.size})",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.poppins_bold)),
                                fontWeight = FontWeight(600),
                                color = Neutral900
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            unlockedList.forEach { item ->
                                FirestoreAchievementCard(item = item, isUnlocked = true)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // ── Locked Achievements ──────────────────────────────────
                if (lockedList.isNotEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "🔒 Belum Diraih (${lockedList.size})",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.poppins_bold)),
                                fontWeight = FontWeight(600),
                                color = Neutral900
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            lockedList.forEach { item ->
                                FirestoreAchievementCard(item = item, isUnlocked = false)
                            }
                        }
                    }
                }

                // ── Empty State ──────────────────────────────────────────
                if (state.achievements.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = Color(0xFFE0E0E0)
                        )
                        Text(
                            text = "Achievement belum tersedia",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.poppins_bold)),
                                color = Neutral600
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // ── POPUP Achievement Unlock (REAKTIF — muncul otomatis saat unlock) ──
        // Dipicu oleh showUnlockBanner di AchievementViewModel
        showUnlockBanner?.let { newAchieve ->
            AchievementUnlockBanner(
                achievement = newAchieve,
                onDismiss = { viewModel.dismissUnlockBanner() }
            )
        }
    }
}

/**
 * Card achievement dari Firestore.
 * Progress bar bergerak dari data real Firestore (REAKTIF).
 */
@Composable
fun FirestoreAchievementCard(
    item: AchievementFirestore,
    isUnlocked: Boolean
) {
    val context = LocalContext.current
    val imageRes = remember(item.imageRes) {
        if (item.imageRes.isNotBlank())
            context.resources.getIdentifier(item.imageRes, "drawable", context.packageName)
        else 0
    }

    val progress = (item.progress.toFloat() / item.target.toFloat()).coerceIn(0f, 1f)

    // Animasi progress bar
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800),
        label = "progressAnim"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(108.dp)
            .shadow(elevation = 8.dp, spotColor = Color(0x26000000), ambientColor = Color(0x26000000), shape = RoundedCornerShape(16.dp))
            .background(
                color = if (isUnlocked) Color(0xFFFFF8E1) else Color.White,
                shape = RoundedCornerShape(size = 16.dp)
            )
            .border(
                width = if (isUnlocked) 1.5.dp else 0.dp,
                color = if (isUnlocked) Color(0xFFFFC300) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(start = 12.dp, end = 12.dp)
    ) {
        // Gambar achievement
        Box(
            modifier = Modifier.size(76.dp),
            contentAlignment = Alignment.Center
        ) {
            if (imageRes != 0) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFFBDBDBD)
                    )
                }
            }
        }

        // Teks + Progress Bar
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.Top),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.weight(1f)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.title,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.poppins_bold)),
                        fontWeight = FontWeight(600),
                        color = Neutral900
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (isUnlocked) {
                    Text(
                        text = "🏆",
                        fontSize = 16.sp
                    )
                }
            }

            Text(
                text = item.subtitle,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF707070)
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Progress Bar — REAKTIF dari Firestore
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .height(6.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(3.dp))
                        .background(color = Color(0xFFE0E0E0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)   // ANIMASI REAKTIF
                            .height(6.dp)
                            .background(
                                color = if (isUnlocked) Color(0xFFFFC300) else Secondary500,
                                shape = RoundedCornerShape(3.dp)
                            )
                    )
                }

                Text(
                    text = "${item.progress.coerceAtMost(item.target)}/${item.target}",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF808080)
                    )
                )
            }
        }
    }
}

/**
 * Banner notifikasi saat achievement baru terbuka.
 * Muncul dari bawah layar, auto-dismiss setelah 3 detik.
 */
@Composable
private fun AchievementUnlockBanner(
    achievement: AchievementFirestore,
    onDismiss: () -> Unit
) {
    // Auto dismiss setelah 3.5 detik
    LaunchedEffect(achievement.id) {
        delay(3500)
        onDismiss()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E1E1E), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "🏆", fontSize = 32.sp)

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Achievement Terbuka!",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            color = Color(0xFFFFC300)
                        )
                    )
                    Text(
                        text = achievement.title,
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontFamily = FontFamily(Font(R.font.poppins_bold)),
                            fontWeight = FontWeight(700),
                            color = Color.White
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = achievement.subtitle,
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            color = Color(0xFFAAAAAA)
                        )
                    )
                }
            }
        }
    }
}