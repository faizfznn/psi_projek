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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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

private val selectedBorderColor = Color(0xFF22A45D)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestsScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()

    val selectedSubjects = remember { mutableStateListOf<String>() }
    val selectedHobbies = remember { mutableStateListOf<String>() }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                text = "Minat Kamu",
                style = PoppinsH5Bold,
                color = Neutral900
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pilih pelajaran favorit dan hobimu",
                style = PoppinsSmallRegular,
                color = Neutral600
            )

            Spacer(modifier = Modifier.height(24.dp))

            InterestSection(
                title = "Pelajaran Favorit",
                options = favoriteSubjectOptions,
                selectedItems = selectedSubjects
            )

            Spacer(modifier = Modifier.height(20.dp))

            InterestSection(
                title = "Hobi",
                options = hobbyOptions,
                selectedItems = selectedHobbies
            )

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
                text = if (isLoading) "Menyimpan..." else "Lanjutkan",
                buttonType = ButtonType.PRIMARY,
                enabled = !isLoading,
                onClick = {
                    val uid = auth.currentUser?.uid

                    errorMessage = null
                    when {
                        uid.isNullOrBlank() -> errorMessage = "Sesi login tidak valid. Silakan login kembali."
                        selectedSubjects.isEmpty() -> errorMessage = "Pilih minimal satu pelajaran favorit"
                        selectedHobbies.isEmpty() -> errorMessage = "Pilih minimal satu hobi"
                        else -> {
                            isLoading = true
                            scope.launch {
                                try {
                                    firestore.collection("users")
                                        .document(uid)
                                        .set(
                                            mapOf(
                                                "favoriteSubjects" to selectedSubjects.toList(),
                                                "hobbies" to selectedHobbies.toList(),
                                                "profileComplete" to true
                                            ),
                                            SetOptions.merge()
                                        )
                                        .await()
                                    onContinue()
                                } catch (e: Exception) {
                                    errorMessage = e.localizedMessage
                                        ?: "Gagal menyimpan minat. Periksa koneksi internet Anda dan coba lagi."
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InterestSection(
    title: String,
    options: List<String>,
    selectedItems: MutableList<String>,
) {
    Text(
        text = title,
        style = PoppinsSmallMedium,
        color = Neutral900,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(10.dp))

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        options.forEach { option ->
            val selected = selectedItems.contains(option)
            Box(
                modifier = Modifier
                    .background(
                        color = Primary500,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .border(
                        width = if (selected) 2.dp else 1.dp,
                        color = if (selected) selectedBorderColor else Primary500,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .clickable {
                        if (selected) {
                            selectedItems.remove(option)
                        } else {
                            selectedItems.add(option)
                        }
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    style = PoppinsSmallRegular,
                    color = Neutral900
                )

                if (selected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(16.dp)
                            .background(selectedBorderColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Terpilih",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
        }
    }
}
