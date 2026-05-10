package com.kelompok2.scarla.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.zIndex
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kelompok2.scarla.ui.components.AppButton
import com.kelompok2.scarla.ui.components.ButtonType
import com.kelompok2.scarla.ui.components.AuthTextField
import com.kelompok2.scarla.ui.theme.AuthBackground
import com.kelompok2.scarla.ui.theme.Neutral300
import com.kelompok2.scarla.ui.theme.Neutral400
import com.kelompok2.scarla.ui.theme.Neutral500
import com.kelompok2.scarla.ui.theme.Neutral600
import com.kelompok2.scarla.ui.theme.Neutral700
import com.kelompok2.scarla.ui.theme.Neutral800
import com.kelompok2.scarla.ui.theme.Neutral900
import com.kelompok2.scarla.ui.theme.PoppinsH5Bold
import com.kelompok2.scarla.ui.theme.PoppinsSmallRegular
import com.kelompok2.scarla.ui.theme.PoppinsTinyRegular
import com.kelompok2.scarla.ui.theme.Primary500
import com.kelompok2.scarla.ui.theme.Secondary500
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val genderOptions = listOf("Laki-laki", "Perempuan")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onAvatarSelect: () -> Unit,
    onSaveSuccess: () -> Unit,
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()
    val uid = auth.currentUser?.uid

    var name by remember { mutableStateOf("") }
    var birthDateMillis by remember { mutableStateOf<Long?>(null) }
    var gender by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    var avatar by remember { mutableStateOf("avatar_default") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var datePickerVisible by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }

    // Load current profile data
    LaunchedEffect(uid) {
        if (!uid.isNullOrBlank()) {
            try {
                val doc = firestore.collection("users").document(uid).get().await()
                name = doc.getString("name").orEmpty()
                city = doc.getString("city").orEmpty()
                statusMessage = doc.getString("statusMessage").orEmpty()
                avatar = doc.getString("avatar").orEmpty().ifBlank { "avatar_default" }
                gender = doc.getString("gender").orEmpty()
                birthDateMillis = doc.getTimestamp("birthDate")?.toDate()?.time
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Gagal memuat data profil"
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
                text = "Kenalan Dulu Yuk",
                style = PoppinsH5Bold,
                color = Neutral900
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Edit data dirimu",
                style = PoppinsSmallRegular,
                color = Neutral600
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Avatar Preview + Edit Button
            // Gunakan Box tanpa clip sebagai pembungkus utama agar tombol edit tidak terpotong
            Box(
                modifier = Modifier.size(140.dp) // Ukuran total area avatar
            ) {
                // 1. Lingkaran Abu-abu & Foto Avatar
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape) // Hanya clip area ini saja
                        .background(Neutral300)
                        .clickable { onAvatarSelect() },
                    contentAlignment = Alignment.Center
                ) {
                    val context = LocalContext.current
                    val avatarRes = remember(avatar) { 
                        context.resources.getIdentifier(avatar, "drawable", context.packageName) 
                    }
                    
                    if (avatarRes != 0) {
                        Icon(
                            painter = painterResource(avatarRes),
                            contentDescription = "Avatar",
                            modifier = Modifier.size(120.dp),
                            tint = Color.Unspecified
                        )
                    }
                }

                // 2. Tombol Edit (Sekarang aman di luar clipping lingkaran abu-abu)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        // Hilangkan atau kurangi padding agar tombol lebih keluar/menonjol
                        .padding(4.dp) 
                        .zIndex(3f)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Secondary500)
                        .clickable { onAvatarSelect() }, // Tambahkan klik di sini juga jika perlu
                    contentAlignment = Alignment.Center
                ) {
                    Text("✏️", fontSize = MaterialTheme.typography.bodyLarge.fontSize)
                }
            }


            Spacer(modifier = Modifier.height(24.dp))

            AuthTextField(
                value = name,
                onValueChange = { name = it },
                label = "Nama",
                placeholder = "Masukkan nama",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerVisible = true }
            ) {
                OutlinedTextField(
                    value = birthDateMillis?.let { formatBirthDate(it) }.orEmpty(),
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Tanggal lahir", style = PoppinsTinyRegular, color = Neutral600) },
                    placeholder = { Text("Pilih tanggal lahir", style = PoppinsSmallRegular, color = Neutral400) },
                    readOnly = true,
                    enabled = false,
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Pilih tanggal",
                            tint = Neutral500
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = Neutral300,
                        disabledLabelColor = Neutral500,
                        disabledPlaceholderColor = Neutral400,
                        disabledTextColor = Neutral800,
                        disabledTrailingIconColor = Neutral500,
                        disabledContainerColor = Color.White
                    )
                )
            }

            if (datePickerVisible) {
                val datePickerState = rememberDatePickerState(initialSelectedDateMillis = birthDateMillis)

                DatePickerDialog(
                    onDismissRequest = { datePickerVisible = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val selectedDateMillis = datePickerState.selectedDateMillis
                                if (selectedDateMillis == null || selectedDateMillis > System.currentTimeMillis()) {
                                    errorMessage = "Tanggal lahir tidak valid"
                                } else {
                                    birthDateMillis = selectedDateMillis
                                    errorMessage = null
                                    datePickerVisible = false
                                }
                            }
                        ) {
                            Text("Pilih")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { datePickerVisible = false }) {
                            Text("Batal")
                        }
                    }
                ) {
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = { genderExpanded = !genderExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = gender,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Jenis kelamin", style = PoppinsTinyRegular, color = Neutral600) },
                    placeholder = { Text("Pilih jenis kelamin", style = PoppinsSmallRegular, color = Neutral400) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Pilih jenis kelamin",
                            tint = Neutral500
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Secondary500,
                        unfocusedBorderColor = Neutral300,
                        focusedLabelColor = Secondary500,
                        unfocusedLabelColor = Neutral500,
                        cursorColor = Secondary500,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                DropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { genderExpanded = false }
                ) {
                    genderOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, style = PoppinsSmallRegular) },
                            onClick = {
                                gender = option
                                genderExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AuthTextField(
                value = city,
                onValueChange = { city = it },
                label = "Asal kota",
                placeholder = "Masukkan asal kota",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            AuthTextField(
                value = statusMessage,
                onValueChange = { statusMessage = it },
                label = "Pesan",
                placeholder = "Tulis pesan profilmu",
                modifier = Modifier.fillMaxWidth()
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
                text = if (isSaving) "Menyimpan..." else "Simpan Perubahan",
                buttonType = ButtonType.PRIMARY,
                enabled = !isSaving,
                onClick = {
                    errorMessage = null
                    when {
                        name.trim().isEmpty() -> errorMessage = "Nama tidak boleh kosong"
                        birthDateMillis == null -> errorMessage = "Tanggal lahir wajib dipilih"
                        gender.isBlank() -> errorMessage = "Jenis kelamin wajib dipilih"
                        city.trim().isEmpty() -> errorMessage = "Asal kota tidak boleh kosong"
                        avatar.isBlank() -> errorMessage = "Pilih avatar terlebih dahulu"
                        else -> {
                            isSaving = true
                            scope.launch {
                                try {
                                    firestore.collection("users").document(uid!!).set(
                                        mapOf(
                                            "name" to name.trim(),
                                            "birthDate" to Timestamp(Date(birthDateMillis!!)),
                                            "gender" to gender,
                                            "city" to city.trim(),
                                            "statusMessage" to statusMessage.trim(),
                                            "avatar" to avatar
                                        ),
                                        SetOptions.merge()
                                    ).await()
                                    onSaveSuccess()
                                } catch (e: Exception) {
                                    errorMessage = e.localizedMessage ?: "Gagal menyimpan data profil"
                                } finally {
                                    isSaving = false
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

private fun formatBirthDate(millis: Long): String {
    return SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date(millis))
}
