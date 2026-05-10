package com.kelompok2.scarla.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.kelompok2.scarla.ui.theme.PoppinsSmallMedium
import com.kelompok2.scarla.ui.theme.PoppinsSmallRegular
import com.kelompok2.scarla.ui.theme.PoppinsTinyRegular
import com.kelompok2.scarla.ui.theme.Primary500
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private val favoriteSubjectOptions = listOf(
    "Matematika", "Bahasa Indonesia", "Bahasa Inggris", "Fisika", "Kimia", "Biologi", "Sejarah", "Geografi", "Seni", "Informatika"
)

private val hobbyOptions = listOf(
    "Membaca", "Menulis", "Olahraga", "Musik", "Gaming", "Menggambar", "Memasak", "Fotografi"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditInterestsScreen(
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit,
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()
    val uid = auth.currentUser?.uid

    val selectedSubjects = remember { mutableStateListOf<String>() }
    val selectedHobbies = remember { mutableStateListOf<String>() }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load current interests
    LaunchedEffect(uid) {
        if (!uid.isNullOrBlank()) {
            try {
                val doc = firestore.collection("users").document(uid).get().await()
                val subjects = (doc.get("favoriteSubjects") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                val hobbies = (doc.get("hobbies") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                selectedSubjects.addAll(subjects)
                selectedHobbies.addAll(hobbies)
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Gagal memuat data minat"
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
                text = "Edit Minat & Hobi",
                style = PoppinsH5Bold,
                color = Neutral900
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Favorite Subjects Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Pelajaran Favorit",
                    style = PoppinsSmallMedium,
                    color = Neutral900
                )
                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    favoriteSubjectOptions.forEach { option ->
                        val isSelected = selectedSubjects.contains(option)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .background(if (isSelected) Primary500 else Color.White)
                                .border(
                                    1.dp,
                                    if (isSelected) Primary500 else Neutral300,
                                    RoundedCornerShape(24.dp)
                                )
                                .clickable {
                                    if (isSelected) selectedSubjects.remove(option) else selectedSubjects.add(option)
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                option,
                                style = PoppinsTinyRegular,
                                color = if (isSelected) Color.White else Neutral900
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Hobbies Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Hobi",
                    style = PoppinsSmallMedium,
                    color = Neutral900
                )
                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    hobbyOptions.forEach { option ->
                        val isSelected = selectedHobbies.contains(option)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .background(if (isSelected) Primary500 else Color.White)
                                .border(
                                    1.dp,
                                    if (isSelected) Primary500 else Neutral300,
                                    RoundedCornerShape(24.dp)
                                )
                                .clickable {
                                    if (isSelected) selectedHobbies.remove(option) else selectedHobbies.add(option)
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                option,
                                style = PoppinsTinyRegular,
                                color = if (isSelected) Color.White else Neutral900
                            )
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

            Spacer(modifier = Modifier.height(28.dp))
            AppButton(
                text = if (isSaving) "Menyimpan..." else "Simpan Perubahan",
                buttonType = ButtonType.PRIMARY,
                enabled = !isSaving,
                onClick = {
                    errorMessage = null
                    isSaving = true
                    scope.launch {
                        try {
                            firestore.collection("users").document(uid!!).set(
                                mapOf(
                                    "favoriteSubjects" to selectedSubjects.toList(),
                                    "hobbies" to selectedHobbies.toList()
                                ),
                                SetOptions.merge()
                            ).await()
                            onSaveSuccess()
                        } catch (e: Exception) {
                            errorMessage = e.localizedMessage ?: "Gagal menyimpan data minat"
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
