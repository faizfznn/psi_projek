package com.kelompok2.scarla.ui.screens
 
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.kelompok2.scarla.R
import com.kelompok2.scarla.ui.theme.*
import kotlin.math.absoluteValue
import com.kelompok2.scarla.ui.components.AppButton
import com.kelompok2.scarla.ui.components.ButtonType
import com.kelompok2.scarla.ui.viewmodel.StreakViewModel
 
 
// --- DATA MODELS ---
data class StreakDay(
    val dayName: String,
    val isActive: Boolean
)
 
data class BeaItem(
    val title: String,
    val subtitle: String,
    val imageRes: Int,
    val url: String = ""       
)
 
data class StudyModule(
    val title: String,
    val imageRes: Int,
    val route: String = ""      
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController? = null,
    onNavigateToHtml: () -> Unit = {},
    streakViewModel: StreakViewModel = viewModel()   // REAKTIF: inject ViewModel
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // REAKTIF: streakState diperbarui otomatis dari Firestore snapshot listener
    val streakState by streakViewModel.streakState.collectAsStateWithLifecycle()

    // Nama user dari FirebaseAuth (reactif saat login)
    val userName = remember { FirebaseAuth.getInstance().currentUser?.displayName ?: "Pengguna" }

    // weeklyDays dari Firestore (bukan hardcoded)
    val dayLabels = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
    val streakDays = dayLabels.mapIndexed { index, label ->
        StreakDay(label, streakState.weeklyDays.getOrElse(index) { false })
    }

    val studyModules = listOf(
        StudyModule("Belajar Dasar HTML", R.drawable.ic_html, "html_screen"),
        StudyModule("Belajar Dasar HTML", R.drawable.ic_html, "html_screen")
    )

    val scholarshipList = listOf(
        BeaItem(
            title = "Glow & Lovely Bintang Beasiswa",
            subtitle = "Bantuan edukasi bagi perempuan Indonesia untuk melanjutkan pendidikan ke jenjang tinggi",
            imageRes = R.drawable.bea_1,
            url = "https://www.glowandlovely.com/id-id/beasiswa"

        ),
        BeaItem(
            title = "Beasiswa SEMESTA",
            subtitle = "Jenjang pendidikan S1 & S2 di Perguruan Tinggi dengan sistem Pendidikan Jarak Jauh (PJJ)",
            imageRes = R.drawable.bea_2,
            url = "https://www.beasiswasemesta.id"

        ),
        BeaItem(
            title = "Tanoto Foundation",
            subtitle = "Program kepemimpinan dan beasiswa terstruktur untuk mencetak pemimpin masa depan",
            imageRes = R.drawable.bea_3,
            url = "https://www.tanotofoundation.org/id/program-beasiswa/"

        )
    )

    val pagerState = rememberPagerState(
        initialPage = 1,
        pageCount = { scholarshipList.size }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFCFCFC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(bottom = 80.dp)
        ) {
            // Header Logo
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.font_logo),
                    contentDescription = "Logo SCARLA",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .width(160.dp)
                        .height(40.dp)
                )
            }

            // Wrapper Konten
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
            ) {

                // --- 1. Bagian Hello & Notifikasi ---
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                ) {
                    Text(
                        // REAKTIF: nama dari Firebase Auth
                        text = "Hello, $userName",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontFamily = FontFamily(Font(R.font.poppins_bold)),
                            fontWeight = FontWeight(700),
                            color = Neutral900
                        )
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_notifications_none_24),
                        contentDescription = "Notifications",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // --- 2. Container Streak Mingguan ---
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)   // FIX 3: padding hanya di sini
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
                        .background(color = Color.White, shape = RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .wrapContentHeight()           // FIX 2: tinggi menyesuaikan konten
                ) {
                    streakDays.forEach { item ->
                        StreakItem(day = item)
                    }
                }

                // --- 3. Bagian Lanjut Belajar ---
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                ) {
                    Text(
                        text = "Lanjut Belajar",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontFamily = FontFamily(Font(R.font.poppins_bold)),
                            fontWeight = FontWeight(700),
                            color = Neutral900
                        ),
                        modifier = Modifier.padding(horizontal = 20.dp)  // FIX 3: padding hanya di title

                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()    // FIX 2: tinggi menyesuaikan konten
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp)  // FIX 3: padding sebagai content padding scroll
                    ) {
                        studyModules.forEach { module ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.width(135.dp).wrapContentHeight()
                            ) {
                                // Card Modul Belajar
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(16.dp))
                                        .background(Color.White, shape = RoundedCornerShape(16.dp))
                                        .padding(12.dp)
                                        .size(115.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = module.imageRes),
                                        contentDescription = module.title,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Text(
                                        text = module.title,
                                        style = TextStyle(
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily(Font(R.font.poppins_bold)),
                                            fontWeight = FontWeight(700),
                                            color = Color(0xFF333333)
                                        ),
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                AppButton(
                                    text = "Continue",
                                    buttonType = ButtonType.PRIMARY,
                                    onClick = {
                                        onNavigateToHtml()
                                    },
                                    modifier = Modifier

                                )
                            }
                        }

                        // Tombol Bulat Continue untuk geser ke kanan
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .width(70.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(Color(0xFFFFD100), shape = CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Continue",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily(Font(R.font.poppins_medium)),
                                    fontWeight = FontWeight(500),
                                    color = Color(0xFF555555)
                                )
                            )
                        }
                    }
                }

                // --- 4. Bagian Informasi Beasiswa (Carousel) ---
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                ) {
                    Text(
                        text = "Informasi Beasiswa",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontFamily = FontFamily(Font(R.font.poppins_bold)),
                            fontWeight = FontWeight(700),
                            color = Neutral900
                        ),
                        modifier = Modifier.padding(horizontal = 20.dp)  // FIX 3: padding hanya di title

                    )

                    HorizontalPager(
                        state = pagerState,
                        contentPadding = PaddingValues(horizontal = 96.dp),
                        pageSpacing = 12.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) { page ->
                        val pageOffset = (
                                (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                                ).absoluteValue

                        val scale = lerp(
                            start = 0.82f,
                            stop = 1.0f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )

                        val alpha = lerp(
                            start = 0.5f,
                            stop = 1.0f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )

                        val shadowElevation = lerp(
                            start = 0f,
                            stop = 6f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )

                        BeasiswaCard(
                            item = scholarshipList[page],
                            onCardClick = { url ->
                                // FIX 5: buka URL di browser ketika card diklik
                                if (url.isNotEmpty()) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                }
                            },
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    this.alpha = alpha
                                    // Memperbaiki properti kedalaman visual di GraphicsLayerScope
                                    this.shadowElevation = shadowElevation
                                }
                                .shadow(
                                    elevation = shadowElevation.dp,
                                    shape = RoundedCornerShape(24.dp),
                                    clip = false
                                )
                        )
                    }
                }
            }
        }
    }
}

// Card Beasiswa (Fokus Layout Mirip Mockup)
@Composable
fun BeasiswaCard(
    item: BeaItem,
    onCardClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(215.dp)
            .wrapContentHeight()
            .background(color = Color.White, shape = RoundedCornerShape(size = 24.dp))
            .clickable { onCardClick(item.url) }
            .padding(10.dp)
    ) {
        Image(
            painter = painterResource(id = item.imageRes),
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(155.dp)
                .clip(RoundedCornerShape(20.dp))
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Text(
                text = item.title,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontFamily = FontFamily(Font(R.font.poppins_bold)),
                    fontWeight = FontWeight(700),
                    color = Color(0xFF222222),
                    textAlign = TextAlign.Center,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = item.subtitle,
                style = TextStyle(
                    fontSize = 10.sp,
                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center,
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
fun StreakItem(day: StreakDay) {
    val imageRes = if (day.isActive) R.drawable.fire_streak else R.drawable.fire_grey
    val imageWidth = if (day.isActive) 44.dp else 34.dp
    val imageHeight = if (day.isActive) 44.dp else 34.dp

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(46.dp)
            .height(76.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(46.dp)
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "Streak status untuk ${day.dayName}",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .width(imageWidth)
                    .height(imageHeight)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = day.dayName,
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.poppins_bold)),
                fontWeight = FontWeight(700),
                color = if (day.isActive) Primary500 else Color(0xFF9E9E9E),
                textAlign = TextAlign.Center,
            )
        )
    }
}

fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + fraction * (stop - start)
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}