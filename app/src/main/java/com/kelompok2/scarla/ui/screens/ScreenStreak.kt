package com.kelompok2.scarla.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kelompok2.scarla.R
import com.kelompok2.scarla.ui.theme.Primary500
import com.kelompok2.scarla.ui.theme.Secondary500
import com.kelompok2.scarla.ui.viewmodel.StreakViewModel

/**
 * ScreenStreak — REAKTIF via StreakViewModel
 *
 * - Otomatis mencatat aktivitas harian saat screen dibuka (ASINKRONUS)
 * - Streak count di-observe dari Firestore real-time (REAKTIF)
 * - weeklyDays langsung reflect data Firebase, bukan data statis
 */
@Composable
fun ScreenStreak(
    onContinue: () -> Unit,
    viewModel: StreakViewModel = viewModel()
) {
    // REAKTIF: collectAsStateWithLifecycle akan trigger rekomposisi otomatis
    // setiap kali streakState berubah (dipicu oleh Firestore snapshot listener)
    val streakState by viewModel.streakState.collectAsStateWithLifecycle()

    // ASINKRONUS: Catat aktivitas hari ini saat screen pertama kali tampil
    LaunchedEffect(Unit) {
        viewModel.recordDailyActivity()
    }

    // Animasi angka streak (scale in saat streak naik)
    val streakScale by animateFloatAsState(
        targetValue = if (streakState.isLoading) 0.7f else 1f,
        animationSpec = tween(durationMillis = 500),
        label = "streakScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFCFCFC))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // ── Judul ────────────────────────────────────────────────────────
        Text(
            text = "🔥 Streak Harian",
            style = TextStyle(
                fontSize = 24.sp,
                fontFamily = FontFamily(Font(R.font.poppins_bold)),
                fontWeight = FontWeight(700),
                color = Color(0xFF303030)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Terus aktif setiap hari untuk menjaga semangat belajarmu!",
            style = TextStyle(
                fontSize = 13.sp,
                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                color = Color(0xFF707070)
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ── Streak Card (Data dari Firestore — REAKTIF) ─────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(20.dp))
                .padding(vertical = 32.dp, horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (streakState.isLoading) {
                CircularProgressIndicator(color = Secondary500)
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Angka streak dengan animasi scale
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.scale(streakScale)
                    ) {
                        StreakIconDisplay(drawableName = "fire_streak")
                        Text(
                            text = streakState.currentStreak.toString(),
                            style = TextStyle(
                                fontSize = 56.sp,
                                fontFamily = FontFamily(Font(R.font.poppins_bold)),
                                fontWeight = FontWeight(700),
                                color = Color(0xFFFFC300)
                            )
                        )
                    }

                    Text(
                        text = if (streakState.currentStreak == 0)
                            "Mulai hari ini! 💪"
                        else
                            "${streakState.currentStreak} hari berturut-turut!",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = FontFamily(Font(R.font.poppins_bold)),
                            fontWeight = FontWeight(600),
                            color = Color(0xFF505050)
                        )
                    )

                    // Streak terpanjang
                    if (streakState.longestStreak > 0) {
                        Text(
                            text = "Streak terpanjang: ${streakState.longestStreak} hari 🏆",
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                color = Color(0xFF909090)
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Streak Mingguan dari Firestore (REAKTIF) ────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Aktivitas Minggu Ini",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontFamily = FontFamily(Font(R.font.poppins_bold)),
                        fontWeight = FontWeight(600),
                        color = Color(0xFF303030)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val dayLabels = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
                    // REAKTIF: weeklyDays berasal dari Firestore — otomatis update
                    dayLabels.forEachIndexed { index, label ->
                        val isActive = streakState.weeklyDays.getOrElse(index) { false }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.width(40.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isActive) {
                                    StreakIconDisplay(drawableName = "fire_streak")
                                } else {
                                    StreakIconDisplay(drawableName = "fire_grey")
                                }
                            }
                            Text(
                                text = label,
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily(Font(R.font.poppins_bold)),
                                    fontWeight = FontWeight(700),
                                    color = if (isActive) Primary500 else Color(0xFF9E9E9E)
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Secondary500)
        ) {
            Text(
                text = "Lanjutkan Belajar",
                style = TextStyle(
                    fontSize = 15.sp,
                    fontFamily = FontFamily(Font(R.font.poppins_bold)),
                    fontWeight = FontWeight(700),
                    color = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StreakIconDisplay(drawableName: String) {
    val context = LocalContext.current
    val iconRes = remember(drawableName) {
        context.resources.getIdentifier(drawableName, "drawable", context.packageName)
    }

    if (iconRes != 0) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = "Streak",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(38.dp)
        )
    } else {
        Icon(
            imageVector = Icons.Filled.LocalFireDepartment,
            contentDescription = "Streak",
            modifier = Modifier.size(38.dp),
            tint = Color(0xFFFFC300)
        )
    }
}
