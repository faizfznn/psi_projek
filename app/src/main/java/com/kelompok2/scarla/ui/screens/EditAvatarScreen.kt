package com.kelompok2.scarla.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kelompok2.scarla.R
import com.kelompok2.scarla.ui.theme.AuthBackground
import com.kelompok2.scarla.ui.theme.Neutral300
import com.kelompok2.scarla.ui.theme.Neutral800
import com.kelompok2.scarla.ui.theme.Neutral900
import com.kelompok2.scarla.ui.theme.PoppinsH5Bold
import com.kelompok2.scarla.ui.theme.PoppinsSmallRegular
import com.kelompok2.scarla.ui.theme.Primary500
import com.kelompok2.scarla.ui.theme.Secondary500
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private val avatarOptions = listOf(
    "avatar_1" to R.drawable.avatar_1,
    "avatar_2" to R.drawable.avatar_2,
    "avatar_3" to R.drawable.avatar_3,
    "avatar_4" to R.drawable.avatar_4,
    "avatar_5" to R.drawable.avatar_5,
    "avatar_6" to R.drawable.avatar_6,
    "avatar_7" to R.drawable.avatar_7,
    "avatar_8" to R.drawable.avatar_8,
    "avatar_9" to R.drawable.avatar_9,
    "avatar_10" to R.drawable.avatar_10,
    "avatar_11" to R.drawable.avatar_11,
    "avatar_12" to R.drawable.avatar_12,
)

@Composable
fun EditAvatarScreen(
    onBack: () -> Unit,
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val uid = auth.currentUser?.uid
    val scope = rememberCoroutineScope()

    var selectedAvatar by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // Load current avatar
    LaunchedEffect(uid) {
        if (!uid.isNullOrBlank()) {
            try {
                val doc = firestore.collection("users").document(uid).get().await()
                selectedAvatar = doc.getString("avatar").orEmpty().ifBlank { "avatar_1" }
                isLoading = false
            } catch (e: Exception) {
                selectedAvatar = "avatar_1"
                isLoading = false
            }
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Secondary500)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(start = 16.dp, top = 48.dp)
                .size(40.dp)
                .border(1.dp, Neutral300, CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Kembali",
                tint = Neutral800
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Pilih Avatarmu",
                style = PoppinsH5Bold,
                color = Neutral900
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(avatarOptions.size) { index ->
                    val (avatarName, avatarResId) = avatarOptions[index]
                    val isSelected = selectedAvatar == avatarName
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) Primary500.copy(alpha = 0.2f) else Color.White
                            )
                            .border(
                                width = if (isSelected) 3.dp else 2.dp,
                                color = if (isSelected) Primary500 else Neutral300,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                selectedAvatar = avatarName
                                isSaving = true
                                scope.launch {
                                    try {
                                        firestore.collection("users").document(uid!!).set(
                                            mapOf("avatar" to avatarName),
                                            SetOptions.merge()
                                        ).await()
                                    } catch (e: Exception) {
                                        // Handle error silently or show toast
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(avatarResId),
                            contentDescription = avatarName,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                        )

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Primary500),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Terpilih",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
