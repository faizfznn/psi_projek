package com.kelompok2.scarla.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kelompok2.scarla.R
import com.kelompok2.scarla.data.remote.ScarlaApi
import com.kelompok2.scarla.navigation.Screen
import com.kelompok2.scarla.ui.theme.*
import com.kelompok2.scarla.ui.components.*

data class InformatikaItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: Int
)

@Composable
fun InformatikaScreen(navController: NavController) {

    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val uid = auth.currentUser?.uid

    val completedMaterialIds by produceState(initialValue = emptySet<String>(), uid) {
        if (uid.isNullOrBlank()) {
            value = emptySet()
            return@produceState
        }

        val listener = firestore.collection("users")
            .document(uid)
            .collection("materials")
            .addSnapshotListener { snap, _ ->
                value = snap?.documents?.map { it.id }?.toSet().orEmpty()
            }

        awaitDispose {
            listener.remove()
        }
    }

    var materiList by remember { mutableStateOf<List<InformatikaItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val materials = ScarlaApi.service.getMaterials().data.orEmpty()
            materiList = materials.map { material ->
                InformatikaItem(
                    id = material.id,
                    title = material.title,
                    subtitle = material.description ?: "Materi dasar",
                    icon = materialIcon(material.id, material.title)
                )
            }
        } catch (e: Exception) {
            errorMessage = "Backend belum terhubung, tampilkan materi default"
            materiList = fallbackInformatikaItems()
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        // HEADER
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .shadow(6.dp, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Neutral50
            )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = {
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .background(
                            Primary500,
                            CircleShape
                        )
                        .size(38.dp)
                ) {

                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Neutral900
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Informatika",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary500)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = Neutral700,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
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

                    items(materiList, key = { it.id }) { item ->

                        InformatikaCard(
                            item = item,
                            isCompleted = item.id in completedMaterialIds,
                            onClick = {
                                navController.navigate(
                                    Screen.MaterialDetail.createRoute(item.id)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InformatikaCard(
    item: InformatikaItem,
    isCompleted: Boolean = false,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .height(190.dp)
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) Success.copy(alpha = 0.08f) else Neutral50
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            if (isCompleted) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFFE8F8EE),
                                shape = RoundedCornerShape(999.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = Success,
                                shape = RoundedCornerShape(999.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Success,
                                modifier = Modifier.size(14.dp)
                            )

                            Text(
                                text = "Selesai",
                                color = Success,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

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
                    color = Neutral700
                )
            }

            AppButton(
                text = if (isCompleted) "Ulangi" else "Mulai",
                onClick = onClick,

                modifier = Modifier.fillMaxWidth(),

                buttonType = ButtonType.PRIMARY
            )
        }
    }
}

private fun fallbackInformatikaItems(): List<InformatikaItem> {
    return listOf(
        InformatikaItem("html", "HTML", "HTML dasar", R.drawable.ic_html),
        InformatikaItem("css", "CSS", "CSS dasar", R.drawable.ic_css),
        InformatikaItem("javascript", "Javascript", "Javascript dasar", R.drawable.ic_javascript),
        InformatikaItem("java", "Java", "Java dasar", R.drawable.ic_java),
        InformatikaItem("python", "Python", "Python dasar", R.drawable.ic_python),
        InformatikaItem("csharp", "C#", "C# dasar", R.drawable.ic_csharp)
    )
}

private fun materialIcon(id: String, title: String): Int {
    val key = "$id $title".lowercase()
    return when {
        "html" in key -> R.drawable.ic_html
        "css" in key -> R.drawable.ic_css
        "javascript" in key || "js" in key -> R.drawable.ic_javascript
        "java" in key -> R.drawable.ic_java
        "python" in key -> R.drawable.ic_python
        "c#" in key || "csharp" in key || "c-sharp" in key -> R.drawable.ic_csharp
        else -> R.drawable.ic_html
    }
}