package com.kelompok2.scarla.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kelompok2.scarla.R
import androidx.compose.ui.text.style.TextAlign
import com.kelompok2.scarla.ui.theme.*
import com.kelompok2.scarla.ui.components.*

data class SubjectItem(
    val title: String,
    val subtitle: String,
    val icon: Int,
    val route: String? = null
)

@Composable
fun BelajarScreen(navController: NavController) {

    val subjects = listOf(
        SubjectItem(
            "Informatika",
            "Belajar informatika",
            R.drawable.ic_informatika,
            "informatika_screen"
        ),
        SubjectItem(
            "Bahasa",
            "Belajar banyak bahasa",
            R.drawable.ic_bahasa
        ),
        SubjectItem(
            "Math",
            "Belajar matematika",
            R.drawable.ic_matematika
        ),
        SubjectItem(
            "Fisika",
            "Belajar fisika",
            R.drawable.ic_fisika
        ),
        SubjectItem(
            "Kimia",
            "Belajar kimia",
            R.drawable.ic_kimia
        ),
        SubjectItem(
            "Biologi",
            "Belajar biologi",
            R.drawable.ic_biologi
        ),
        SubjectItem(
            "Sosiologi",
            "Belajar sosiologi",
            R.drawable.ic_sosiologi
        ),
        SubjectItem(
            "Ekonomi",
            "Belajar ekonomi",
            R.drawable.ic_ekonomi
        ),
        SubjectItem(
            "Geografi",
            "Belajar geografi",
            R.drawable.ic_geografi
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {

        // HEADER
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .shadow(6.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFEAEAEA)
            )
        ) {

            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "Mau belajar apa hari ini?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "“Setiap detik belajar, mendekatkanmu pada impian”",
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral700
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {

            items(subjects) { item ->

                SubjectCard(
                    item = item,
                    onClick = {
                        item.route?.let {
                            navController.navigate(it)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SubjectCard(
    item: SubjectItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .height(200.dp) // Sedikit ditambah tingginya agar tidak terlalu sesak
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Neutral50)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween // Mengatur jarak antar elemen secara merata
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center, // Memastikan teks di tengah secara internal
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Image(
                    painter = painterResource(id = item.icon),
                    contentDescription = item.title,
                    modifier = Modifier.size(55.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral700,
                    textAlign = TextAlign.Center, // Memastikan subjudul juga center
                    modifier = Modifier.fillMaxWidth(),
                    lineHeight = TextUnit.Unspecified // Mencegah spasi baris yang aneh
                )
            }

            AppButton(
                text = "Mulai",
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                buttonType = ButtonType.PRIMARY
            )
        }
    }
}