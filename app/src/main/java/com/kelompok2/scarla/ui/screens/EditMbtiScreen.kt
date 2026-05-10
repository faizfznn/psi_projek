package com.kelompok2.scarla.ui.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kelompok2.scarla.ui.components.AppButton
import com.kelompok2.scarla.ui.components.ButtonType
import com.kelompok2.scarla.ui.theme.AuthBackground
import com.kelompok2.scarla.ui.theme.Neutral300
import com.kelompok2.scarla.ui.theme.Neutral600
import com.kelompok2.scarla.ui.theme.Neutral800
import com.kelompok2.scarla.ui.theme.Neutral900
import com.kelompok2.scarla.ui.theme.PoppinsH5Bold
import com.kelompok2.scarla.ui.theme.PoppinsSmallRegular
import com.kelompok2.scarla.ui.theme.PoppinsTinyRegular
import com.kelompok2.scarla.ui.theme.Primary50
import com.kelompok2.scarla.ui.theme.Primary500
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private val mbtiOptions = listOf(
    "INFP", "INTP", "ISFJ", "INFJ", "ISFP", "ISTJ", "INTJ", "ISTP",
    "ENFP", "ENTP", "ENFJ", "ESFJ", "ESFP", "ESTJ", "ENTJ", "ESTP"
)

private val selectedBorderColor = Color(0xFF22A45D)

@Composable
fun EditMbtiScreen(
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit,
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()
    val uid = auth.currentUser?.uid

    var selectedMbti by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load current MBTI
    LaunchedEffect(uid) {
        if (!uid.isNullOrBlank()) {
            try {
                val doc = firestore.collection("users").document(uid).get().await()
                val mbti = doc.getString("mbti").orEmpty().ifBlank { "-" }
                selectedMbti = if (mbti == "-") null else mbti
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Gagal memuat data MBTI"
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
            CircularProgressIndicator(color = Primary500)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
                text = "Edit Tipe MBTI",
                style = PoppinsH5Bold,
                color = Neutral900
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pilih tipe kepribadian kamu",
                style = PoppinsSmallRegular,
                color = Neutral600
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(2.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(mbtiOptions.size) { index ->
                    val option = mbtiOptions[index]
                    val selected = selectedMbti == option

                    Box(
                        modifier = Modifier
                            .height(64.dp)
                            .background(
                                color = if (selected) Primary50 else Color.White,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) selectedBorderColor else Neutral300,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedMbti = option },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            style = PoppinsTinyRegular,
                            color = Neutral900
                        )

                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(18.dp)
                                    .background(selectedBorderColor, CircleShape),
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

            errorMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = it,
                    style = PoppinsTinyRegular,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            AppButton(
                text = if (isSaving) "Menyimpan..." else "Simpan Perubahan",
                buttonType = ButtonType.PRIMARY,
                enabled = !isSaving && selectedMbti != null,
                onClick = {
                    errorMessage = null
                    if (selectedMbti.isNullOrBlank()) {
                        errorMessage = "Pilih tipe MBTI terlebih dahulu"
                        return@AppButton
                    }
                    isSaving = true
                    scope.launch {
                        try {
                            firestore.collection("users").document(uid!!).set(
                                mapOf("mbti" to selectedMbti!!),
                                SetOptions.merge()
                            ).await()
                            onSaveSuccess()
                        } catch (e: Exception) {
                            errorMessage = e.localizedMessage ?: "Gagal menyimpan tipe MBTI"
                        } finally {
                            isSaving = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
