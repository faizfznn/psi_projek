package com.kelompok2.scarla.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.kelompok2.scarla.R
import com.kelompok2.scarla.ui.components.AppButton
import com.kelompok2.scarla.ui.components.ButtonType
import kotlinx.coroutines.launch
import com.kelompok2.scarla.ui.theme.*
import com.kelompok2.scarla.ui.components.*

// Data class untuk menampung isi brief
data class OnboardingData(
    val image: Int,
    val title: String,
    val description: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // List konten berdasarkan brief
    val pages = listOf(
        OnboardingData(R.drawable.onboarding_1, "CARI TEMAN", "Temukan teman yang sefrekuensi"),
        OnboardingData(R.drawable.onboarding_2, "Diskusi dan Gabung Grup", "Diskusikan apa yang kamu pelajari dan apa yang kamu ingin pelajari"),
        OnboardingData(R.drawable.onboarding_3, "Belajar Ilmu Baru", "Explore ilmu pengetahuan kamu!"),
        OnboardingData(R.drawable.onboarding_4, "Informasi Beasiswa", "Dapatkan informasi tentang beasiswa dan raih mimpi-mu!")
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })

    // Konfigurasi ImageLoader khusus untuk GIF
    val imageLoader = coil.ImageLoader.Builder(context)
        .components { add(GifDecoder.Factory()) }
        .build()

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.TopEnd
            ) {

                if (pagerState.currentPage < pages.size - 1) {

                    Text(
                        text = "Skip",
                        color = Secondary500,
                        style = MaterialTheme.typography.labelLarge,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            onFinished()
                        }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val data = pages[page]
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(data.image).build(),
                        imageLoader = imageLoader,
                        contentDescription = null,
                        modifier = Modifier.size(280.dp)
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(
                        text = data.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = data.description,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Bottom Navigation (Back & Next/Finish)
            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tombol Back
                if (pagerState.currentPage > 0) {
                    AppButton(
                        text = "Back",
                        buttonType = ButtonType.SECONDARY,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                        }
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Tombol Next atau Finish
                AppButton(
                    text = if (pagerState.currentPage == pages.size - 1) "Mulai" else "Next",
                    buttonType = ButtonType.PRIMARY,
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else {
                            onFinished()
                        }
                    }
                )
            }
        }
    }
}