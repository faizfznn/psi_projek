package com.kelompok2.scarla.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.kelompok2.scarla.R
import com.kelompok2.scarla.firebase.FirestoreInitializer
import kotlinx.coroutines.launch

private data class BottomTab(
    val title: String,
    val drawableName: String,
    val fallbackIcon: ImageVector,
    val badgeCount: Int = 0
)

private val tabs = listOf(
    BottomTab("HOME", "home", Icons.Filled.Home), // Menggunakan huruf kapital untuk judul aktif sesuai mockup
    BottomTab("PESAN", "pesan", Icons.Filled.Message, badgeCount = 7),
    BottomTab("CARI", "cari", Icons.Filled.Search),
    BottomTab("BELAJAR", "belajar", Icons.Filled.MenuBook),
    BottomTab("PROFIL", "profil", Icons.Filled.Person)
)

@Composable
fun MainScreen(navController: NavController? = null) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var showSettingsFromProfile by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // ── INISIALISASI OTOMATIS ────────────────────────────────────────────
    // Dipanggil sekali saat MainScreen pertama kali tampil.
    // Memastikan sub-koleksi streaks & achievements ada untuk user ini,
    // baik user baru maupun user lama yang belum ter-inisialisasi.
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            scope.launch {
                FirestoreInitializer.ensureUserInitialized(uid)
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFFFFEE0), // Warna kuning pucat sesuai mockup
                tonalElevation = 0.dp, // Menghilangkan overlay warna gelap bawaan Material 3
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .background(
                        color = Color(0xFFFFFEE0),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            selectedTab = index
                            if (index != 4) showSettingsFromProfile = false
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF2B2B2B),
                            unselectedIconColor = Color(0xFF2B2B2B),
                            selectedTextColor = Color(0xFF2B2B2B),
                            unselectedTextColor = Color(0xFF2B2B2B),
                            indicatorColor = Color.Transparent // Menghilangkan kapsul pil bawaan Material 3
                        ),
                        icon = {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = if (isSelected) {
                                    // Efek lingkaran kuning untuk item aktif
                                    Modifier
                                        .size(42.dp)
                                        .background(Color(0xFFFFD100), shape = CircleShape)
                                } else {
                                    Modifier.size(42.dp)
                                }
                            ) {
                                Box {
                                    BottomNavIcon(
                                        drawableName = tab.drawableName,
                                        fallbackIcon = tab.fallbackIcon,
                                        modifier = Modifier.size(if (isSelected) 24.dp else 28.dp)
                                    )
                                    if (tab.badgeCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .offset(x = 10.dp, y = (-4).dp)
                                                .size(16.dp)
                                                .background(
                                                    color = Color(0xFFFFD100), // Warna lencana mengikuti tema mockup
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = tab.badgeCount.toString(),
                                                style = TextStyle(
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        label = {
                            // Label teks hanya ditampilkan jika tab sedang aktif (sesuai gambar mockup)
                            if (isSelected) {
                                Text(
                                    text = tab.title,
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily(Font(R.font.poppins_bold)),
                                        fontWeight = FontWeight(700)
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(
                    navController = navController,
                    onNavigateToHtml = {
                        navController?.navigate("html_screen")
                    }
                )
                1 -> ChatPage(navController = navController)
                2 -> CariScreen(navController = navController)
                3 -> {
                    navController?.let {
                        BelajarScreen(navController = it)
                    } ?: Text("Loading Navigation...", modifier = Modifier.align(Alignment.Center))
                }
                4 -> {
                    if (showSettingsFromProfile) {
                        SettingsScreen(
                            navController = navController,
                            onBack = { showSettingsFromProfile = false }
                        )
                    } else {
                        ProfilScreen(
                            navController = navController,
                            onOpenSettings = { showSettingsFromProfile = true }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNavIcon(
    drawableName: String,
    fallbackIcon: ImageVector,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val iconRes = remember(drawableName) {
        context.resources.getIdentifier(drawableName, "drawable", context.packageName)
    }

    if (iconRes != 0) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = drawableName,
            modifier = modifier
        )
    } else {
        Icon(
            imageVector = fallbackIcon,
            contentDescription = drawableName,
            modifier = modifier
        )
    }
}