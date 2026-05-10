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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kelompok2.scarla.R
import com.kelompok2.scarla.ui.components.AppButton
import com.kelompok2.scarla.ui.components.AuthTextField
import com.kelompok2.scarla.ui.components.ButtonType
import com.kelompok2.scarla.ui.theme.AuthBackground
import com.kelompok2.scarla.ui.theme.Neutral300
import com.kelompok2.scarla.ui.theme.Neutral400
import com.kelompok2.scarla.ui.theme.Neutral500
import com.kelompok2.scarla.ui.theme.Neutral600
import com.kelompok2.scarla.ui.theme.Neutral800
import com.kelompok2.scarla.ui.theme.Neutral900
import com.kelompok2.scarla.ui.theme.PoppinsH5Bold
import com.kelompok2.scarla.ui.theme.PoppinsSmallRegular
import com.kelompok2.scarla.ui.theme.PoppinsTinyRegular
import com.kelompok2.scarla.ui.theme.Primary500
import com.kelompok2.scarla.ui.theme.Secondary500
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import java.util.Locale

private data class AvatarOption(
    val name: String,
    val resId: Int,
)

private const val INITIAL_PROFILE_COMPLETE_STATUS = false

private val avatarOptions = listOf(
    AvatarOption("avatar_1", R.drawable.avatar_1),
    AvatarOption("avatar_2", R.drawable.avatar_2),
    AvatarOption("avatar_3", R.drawable.avatar_3),
    AvatarOption("avatar_4", R.drawable.avatar_4),
    AvatarOption("avatar_5", R.drawable.avatar_5),
    AvatarOption("avatar_6", R.drawable.avatar_6),
    AvatarOption("avatar_7", R.drawable.avatar_7),
    AvatarOption("avatar_8", R.drawable.avatar_8),
    AvatarOption("avatar_9", R.drawable.avatar_9),
    AvatarOption("avatar_10", R.drawable.avatar_10),
    AvatarOption("avatar_11", R.drawable.avatar_11),
    AvatarOption("avatar_12", R.drawable.avatar_12),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(auth.currentUser?.displayName.orEmpty()) }
    var birthDateMillis by remember { mutableStateOf<Long?>(null) }
    var gender by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var selectedAvatar by remember { mutableStateOf<String?>(null) }
    var genderExpanded by remember { mutableStateOf(false) }
    var datePickerVisible by remember { mutableStateOf(false) }
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
                text = "Kenalan Dulu Yuk",
                style = PoppinsH5Bold,
                color = Neutral900
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Lengkapi data dirimu sebelum lanjut",
                style = PoppinsSmallRegular,
                color = Neutral600
            )

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
                    .semantics {
                        role = Role.Button
                        contentDescription = "Tanggal lahir, ketuk untuk memilih tanggal"
                    }
                    .clickable { datePickerVisible = true }
            ) {
                OutlinedTextField(
                    value = birthDateMillis?.let {
                        java.text.SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date(it))
                    }.orEmpty(),
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = PoppinsSmallRegular,
                    label = { Text("Tanggal lahir", style = PoppinsTinyRegular, color = Neutral600) },
                    placeholder = { Text("Pilih tanggal lahir", style = PoppinsSmallRegular, color = Neutral400) },
                    singleLine = true,
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
                        focusedBorderColor = Secondary500,
                        unfocusedBorderColor = Neutral300,
                        focusedLabelColor = Secondary500,
                        unfocusedLabelColor = Neutral500,
                        disabledBorderColor = Neutral300,
                        disabledLabelColor = Neutral500,
                        disabledPlaceholderColor = Neutral400,
                        disabledTextColor = Neutral800,
                        disabledTrailingIconColor = Neutral500,
                        disabledContainerColor = Color.White,
                        cursorColor = Secondary500,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
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
                        title = {
                            Text(
                                text = "Pilih tanggal lahir",
                                modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                            )
                        },
                        headline = null,
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
                    textStyle = PoppinsSmallRegular,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                DropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { genderExpanded = false }
                ) {
                    listOf("Laki-laki", "Perempuan").forEach { option ->
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

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Pilih Avatar",
                style = PoppinsSmallRegular,
                color = Neutral800,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(avatarOptions.size) { index ->
                    val avatar = avatarOptions[index]
                    val selected = selectedAvatar == avatar.name
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) Secondary500 else Neutral300,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedAvatar = avatar.name },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(avatar.resId),
                            contentDescription = avatar.name,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                        )

                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(3.dp)
                                    .size(18.dp)
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
                    val birthMillis = birthDateMillis
                    val avatar = selectedAvatar

                    errorMessage = null
                    when {
                        uid.isNullOrBlank() -> errorMessage = "Pengguna tidak valid. Silakan login ulang."
                        name.trim().isEmpty() -> errorMessage = "Nama tidak boleh kosong"
                        birthMillis == null -> errorMessage = "Tanggal lahir wajib dipilih"
                        gender.isBlank() -> errorMessage = "Jenis kelamin wajib dipilih"
                        city.trim().isEmpty() -> errorMessage = "Asal kota tidak boleh kosong"
                        avatar.isNullOrBlank() -> errorMessage = "Pilih avatar terlebih dahulu"
                        else -> {
                            isLoading = true
                            scope.launch {
                                try {
                                    val docRef = firestore.collection("users").document(uid)
                                    val snapshot = docRef.get().await()

                                    val profileData = mutableMapOf<String, Any>(
                                        "name" to name.trim(),
                                        "birthDate" to Timestamp(Date(birthMillis)),
                                        "gender" to gender,
                                        "city" to city.trim(),
                                        "avatar" to avatar,
                                        "profileComplete" to INITIAL_PROFILE_COMPLETE_STATUS,
                                    )
                                    if (!snapshot.exists()) {
                                        profileData["createdAt"] = FieldValue.serverTimestamp()
                                    }

                                    docRef.set(profileData, SetOptions.merge()).await()
                                    onContinue()
                                } catch (e: Exception) {
                                    errorMessage = e.localizedMessage ?: "Gagal menyimpan data profil."
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
