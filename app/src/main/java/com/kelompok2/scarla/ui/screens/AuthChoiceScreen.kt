package com.kelompok2.scarla.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kelompok2.scarla.R
import com.kelompok2.scarla.ui.theme.*

@Composable
fun AuthChoiceScreen(
    onBack: () -> Unit,
    onDaftarSekarang: () -> Unit,
    onMasuk: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Dekorasi Bubble di Background
        BubbleDecoration(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 24.dp, end = 8.dp)
        )

        // Konten Utama
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Tombol Kembali
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(top = 48.dp)
                    .size(40.dp)
                    .border(1.dp, Neutral300, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Neutral800
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Header: Logo dan Slogan
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.font_logo),
                    contentDescription = "Logo",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .width(297.dp)
                        .height(48.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Solusi Cari Teman Belajar",
                    style = TextStyle(
                        fontSize = 24.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily(Font(R.font.bubblegum_sans_regular)),
                        fontWeight = FontWeight(400),
                        color = Primary500,
                        textAlign = TextAlign.Center,
                    ),
                    modifier = Modifier.width(232.dp)
                )
            }

            // Spacer Weight(1f) mendorong konten di bawahnya ke posisi bawah layar
            Spacer(modifier = Modifier.weight(1f))

            // Bagian Teks "Begin Your Journey"
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("Begin ")
                        withStyle(SpanStyle(background = Primary500, color = Neutral900)) {
                            append("Your ")
                        }
                        withStyle(SpanStyle(background = Primary500, color = Neutral900)) {
                            append("\nJourney")
                        }
                        append(" to ")
                        append("Smart")
                    },
                    style = PoppinsH5Bold,
                    color = Neutral900,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Baris Tombol Utama
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Primary500)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ikon Sosial (Google & Apple)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tombol Google
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable { /* Implementasi Google Sign-In */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_google),
                                contentDescription = "Google",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Tombol Apple
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable { /* Implementasi Apple Sign-In */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_apple),
                                contentDescription = "Apple",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Mendorong Tombol Daftar ke sisi kanan
                    Spacer(modifier = Modifier.weight(1f))

                    // Tombol Daftar Sekarang
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .width(235.dp)
                            .height(48.dp)
                            .background(color = Color(0xFFF5F5F5), shape = RoundedCornerShape(12.dp))
                            .clickable { onDaftarSekarang() }
                    ) {
                        Text(
                            text = "Daftar Sekarang",
                            style = TextStyle(
                                fontSize = 13.sp,
                                lineHeight = 24.sp,
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                fontWeight = FontWeight(600),
                                color = Color(0xFF303030),
                                textAlign = TextAlign.Center,
                                letterSpacing = 0.65.sp,
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer: Navigasi Masuk
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Sudah punya akun? ",
                    style = PoppinsSmallRegular,
                    color = Neutral700
                )
                Text(
                    text = "Masuk",
                    style = PoppinsSmallMedium,
                    color = Secondary500,
                    modifier = Modifier.clickable { onMasuk() }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun BubbleDecoration(modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(160.dp)) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .offset(x = 40.dp, y = (-10).dp)
                .clip(CircleShape)
                .background(Primary500)
        )
        Box(
            modifier = Modifier
                .size(50.dp)
                .offset(x = 10.dp, y = 40.dp)
                .clip(CircleShape)
                .background(Primary500)
        )
        Box(
            modifier = Modifier
                .size(30.dp)
                .offset(x = 70.dp, y = 50.dp)
                .clip(CircleShape)
                .background(Secondary500)
        )
    }
}