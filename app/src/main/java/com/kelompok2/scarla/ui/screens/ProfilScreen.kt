package com.kelompok2.scarla.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.kelompok2.scarla.ui.theme.AuthBackground
import com.kelompok2.scarla.ui.theme.Neutral300
import com.kelompok2.scarla.ui.theme.Neutral500
import com.kelompok2.scarla.ui.theme.Neutral600
import com.kelompok2.scarla.ui.theme.Neutral700
import com.kelompok2.scarla.ui.theme.Neutral800
import com.kelompok2.scarla.ui.theme.Neutral900
import com.kelompok2.scarla.ui.theme.PoppinsH5Bold
import com.kelompok2.scarla.ui.theme.PoppinsRegularSemibold
import com.kelompok2.scarla.ui.theme.PoppinsSmallMedium
import com.kelompok2.scarla.ui.theme.PoppinsSmallRegular
import com.kelompok2.scarla.ui.theme.PoppinsTinyRegular
import com.kelompok2.scarla.ui.theme.Primary500
import com.kelompok2.scarla.ui.theme.Secondary500
import com.kelompok2.scarla.ui.theme.Primary500
import com.kelompok2.scarla.ui.theme.Secondary500
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.navigation.NavController

private data class ProfileUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val email: String = "",
    val age: Int? = null,
    val city: String = "",
    val avatar: String = "avatar_default",
    val gender: String = "",
    val birthDateMillis: Long? = null,
    val statusMessage: String = "Belum menulis pesan apapun",
    val favoriteSubjects: List<String> = emptyList(),
    val hobbies: List<String> = emptyList(),
    val mbti: String = "-",
    val completedMaterials: Int = 0,
    val streak: Int = 0,
    val topSubjectFromProgress: String? = null,
    val achievements: List<AchievementUi> = emptyList(),
    val errorMessage: String? = null,
)

private data class AchievementUi(
    val title: String,
    val subtitle: String,
    val current: Int,
    val target: Int,
    val imageRes: Int = 0,
)

private data class MaterialProgress(
    val subject: String,
    val completedAtMillis: Long,
)

private val favoriteSubjectOptions = listOf(
    "Matematika", "Bahasa Indonesia", "Bahasa Inggris", "Fisika", "Kimia", "Biologi", "Sejarah", "Geografi", "Seni", "Informatika"
)

private val hobbyOptions = listOf(
    "Membaca", "Menulis", "Olahraga", "Musik", "Gaming", "Menggambar", "Memasak", "Fotografi"
)

private val mbtiOptions = listOf(
    "INFP", "INTP", "ISFJ", "INFJ", "ISFP", "ISTJ", "INTJ", "ISTP",
    "ENFP", "ENTP", "ENFJ", "ESFJ", "ESFP", "ESTJ", "ENTJ", "ESTP"
)

private val editableAvatarOptions = listOf(
    "avatar_1", "avatar_2", "avatar_3", "avatar_4", "avatar_5", "avatar_6",
    "avatar_7", "avatar_8", "avatar_9", "avatar_10", "avatar_11", "avatar_12"
)

private val genderOptions = listOf("Laki-laki", "Perempuan")

private const val FIELD_COMPLETED_AT = "completedAt"
private const val FIELD_SUBJECT = "subject"
private const val MILLIS_PER_DAY = 86_400_000L

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfilScreen(
    navController: NavController? = null,
    onOpenSettings: (() -> Unit)? = null,
    peerUid: String? = null
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val repository = remember { ProfileReactiveRepository(firestore) }
    val scope = rememberCoroutineScope()
    val myUid = auth.currentUser?.uid
    val targetUid = peerUid ?: myUid
    val isMyProfile = targetUid == myUid

    val profileFlow = remember(targetUid) {
        if (targetUid.isNullOrBlank()) {
            kotlinx.coroutines.flow.flowOf(
                ProfileUiState(
                    isLoading = false,
                    email = if (isMyProfile) auth.currentUser?.email.orEmpty() else "",
                    errorMessage = if (isMyProfile) "Silakan login ulang untuk melihat profil" else "Pengguna tidak ditemukan"
                )
            )
        } else {
            repository.observeProfileState(targetUid, if (isMyProfile) auth.currentUser?.email.orEmpty() else "")
        }
    }

    val uiState by profileFlow.collectAsState(
        initial = ProfileUiState(
            isLoading = targetUid != null,
            email = if (isMyProfile) auth.currentUser?.email.orEmpty() else ""
        )
    )

    var screenError by remember { mutableStateOf<String?>(null) }
    var showAllAchievements by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isMyProfile) Arrangement.SpaceBetween else Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isMyProfile) {
                IconButton(onClick = { navController?.popBackStack() }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(text = "Profile", style = MaterialTheme.typography.titleLarge, color = Neutral900)
            
            if (isMyProfile) {
                IconButton(
                    onClick = {
                        if (onOpenSettings != null) {
                            onOpenSettings()
                        } else if (navController != null) {
                            navController.navigate(com.kelompok2.scarla.navigation.Screen.Settings.route)
                        }
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Pengaturan", tint = Neutral800)
                }
            }
        }

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Secondary500)
                }
            }
            else -> {
                ProfileIdentityCard(
                    uiState = uiState,
                    onEdit = if (isMyProfile && navController != null) { 
                        { navController.navigate(com.kelompok2.scarla.navigation.Screen.EditProfile.route) }
                    } else null
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        iconTint = Secondary500,
                        value = uiState.completedMaterials.toString(),
                        label = "Materi"
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.LocalFireDepartment,
                        iconTint = Secondary500,
                        value = uiState.streak.toString(),
                        label = "Streak"
                    )
                }

                ProfileSection(
                    title = "Pelajaran Favorit",
                    trailingAction = { if (isMyProfile && navController != null) navController.navigate(com.kelompok2.scarla.navigation.Screen.EditInterests.route) },
                    showEdit = isMyProfile
                ) {
                    val values = uiState.favoriteSubjects.ifEmpty {
                        uiState.topSubjectFromProgress?.let { listOf(it) } ?: emptyList()
                    }
                    if (values.isEmpty()) {
                        Text(text = "Belum ada pelajaran favorit", style = PoppinsTinyRegular, color = Neutral600)
                    } else {
                        ProfileChipGroup(values) { subjectEmoji(it) }
                    }
                }

                ProfileSection(
                    title = "Hobi",
                    trailingAction = { if (isMyProfile && navController != null) navController.navigate(com.kelompok2.scarla.navigation.Screen.EditInterests.route) },
                    showEdit = isMyProfile
                ) {
                    if (uiState.hobbies.isEmpty()) {
                        Text(text = "Belum ada hobi", style = PoppinsTinyRegular, color = Neutral600)
                    } else {
                        ProfileChipGroup(uiState.hobbies) { hobbyEmoji(it) }
                    }
                }

                ProfileSection(
                    title = "MBTI",
                    trailingAction = { if (isMyProfile && navController != null) navController.navigate(com.kelompok2.scarla.navigation.Screen.EditMbti.route) },
                    showEdit = isMyProfile
                ) {
                    ProfileChipGroup(listOf(uiState.mbti)) { "🧠" }
                }

                val displayAchievements = if (isMyProfile) {
                    uiState.achievements
                } else {
                    uiState.achievements.filter { it.current >= it.target }
                }

                AchievementSection(
                    achievements = displayAchievements,
                    showAll = showAllAchievements,
                    isMyProfile = isMyProfile,
                    onToggleShowAll = { 
                        if (navController != null && isMyProfile) {
                            navController.navigate(com.kelompok2.scarla.navigation.Screen.Achievement.route)
                        }
                    }
                )
            }
        }

        (screenError ?: uiState.errorMessage)?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, style = PoppinsTinyRegular)
        }
    }
}

@Composable
private fun ProfileIdentityCard(
    uiState: ProfileUiState,
    onEdit: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val avatarRes = remember(uiState.avatar) {
        context.resources.getIdentifier(uiState.avatar, "drawable", context.packageName)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Neutral300, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .border(1.dp, Secondary500, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarRes != 0) {
                        Icon(
                            painter = painterResource(avatarRes),
                            contentDescription = "Avatar",
                            modifier = Modifier.size(52.dp),
                            tint = Color.Unspecified
                        )
                    } else {
                        Text(text = "🙂")
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = uiState.name.ifBlank { "Pengguna" }, style = PoppinsRegularSemibold, color = Neutral900)
                    Text(text = uiState.email.ifBlank { "-" }, style = PoppinsTinyRegular, color = Neutral700)
                    val ageCity = listOfNotNull(uiState.age?.let { "$it Tahun" }, uiState.city.ifBlank { null }).joinToString(" • ")
                    Text(text = ageCity.ifBlank { "Lengkapi data profil" }, style = PoppinsTinyRegular, color = Neutral700)
                }
            }

            if (onEdit != null) {
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit profil", tint = Neutral800)
                }
            }
        }

        Text(text = "Pesan", style = PoppinsRegularSemibold, color = Neutral900)
        Text(text = uiState.statusMessage, style = PoppinsTinyRegular, color = Neutral600)
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    value: String,
    label: String,
) {
    Row(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Neutral300, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = iconTint)
        Column {
            Text(text = value, style = PoppinsRegularSemibold, color = Neutral900)
            Text(text = label, style = PoppinsTinyRegular, color = Neutral600)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileChipGroup(
    items: List<String>,
    prefixEmoji: (String) -> String,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Primary500)
                    .border(1.dp, Secondary500, RoundedCornerShape(24.dp))
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text(text = "${prefixEmoji(item)}  $item", style = PoppinsTinyRegular, color = Neutral900)
            }
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    trailingAction: () -> Unit,
    showEdit: Boolean,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Neutral300, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, style = PoppinsRegularSemibold, color = Neutral900)
            if (showEdit) {
                IconButton(onClick = trailingAction, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit $title", tint = Neutral800)
                }
            }
        }
        content()
    }
}

@Composable
private fun AchievementSection(
    achievements: List<AchievementUi>,
    showAll: Boolean,
    isMyProfile: Boolean = true,
    onToggleShowAll: () -> Unit,
) {
    val visibleItems = if (isMyProfile && !showAll) achievements.take(2) else achievements

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Neutral300, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Achievement", style = PoppinsRegularSemibold, color = Neutral900)
            if (achievements.isNotEmpty() && isMyProfile) {
                Text(
                    text = "Lihat Semua",
                    style = PoppinsTinyRegular,
                    color = Secondary500,
                    modifier = Modifier.clickable(onClick = onToggleShowAll)
                )
            }
        }

        if (visibleItems.isEmpty()) {
            Text(text = "Belum ada achievement", style = PoppinsTinyRegular, color = Neutral600)
            return
        }

        visibleItems.forEach { achievement ->
            val boundedCurrent = achievement.current.coerceAtMost(achievement.target)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 4.dp, spotColor = Color(0x26000000), ambientColor = Color(0x26000000), shape = RoundedCornerShape(12.dp))
                    .background(color = Color.White, shape = RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                if (achievement.imageRes != 0) {
                    Icon(
                        painter = painterResource(achievement.imageRes),
                        contentDescription = achievement.title,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        tint = Color.Unspecified
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = achievement.title,
                        style = PoppinsRegularSemibold,
                        color = Neutral900,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = achievement.subtitle,
                        style = PoppinsTinyRegular,
                        color = Neutral600,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .weight(1f)
                                .background(color = Color(0xFFE0E0E0), shape = RoundedCornerShape(2.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(boundedCurrent.toFloat() / achievement.target.toFloat())
                                    .height(4.dp)
                                    .background(color = Secondary500, shape = RoundedCornerShape(2.dp))
                            )
                        }
                        Text(
                            text = "$boundedCurrent/${achievement.target}",
                            style = PoppinsTinyRegular,
                            color = Neutral700
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun EditProfileDialog(
    initialName: String,
    initialCity: String,
    initialStatus: String,
    initialAvatar: String,
    initialGender: String,
    initialBirthDateMillis: Long?,
    enabled: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, Long) -> Unit,
) {
    val context = LocalContext.current
    var name by remember(initialName) { mutableStateOf(initialName) }
    var city by remember(initialCity) { mutableStateOf(initialCity) }
    var status by remember(initialStatus) { mutableStateOf(initialStatus) }
    var selectedAvatar by remember(initialAvatar) { mutableStateOf(initialAvatar.ifBlank { "avatar_default" }) }
    var gender by remember(initialGender) { mutableStateOf(initialGender) }
    var birthDateMillis by remember(initialBirthDateMillis) { mutableStateOf(initialBirthDateMillis) }
    var datePickerVisible by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    if (datePickerVisible) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = birthDateMillis)

        DatePickerDialog(
            onDismissRequest = { datePickerVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedDateMillis = datePickerState.selectedDateMillis
                        if (selectedDateMillis == null || selectedDateMillis > System.currentTimeMillis()) {
                            validationError = "Tanggal lahir tidak valid"
                        } else {
                            birthDateMillis = selectedDateMillis
                            validationError = null
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Data Diri") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama") }, enabled = enabled)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = enabled) { datePickerVisible = true }
                ) {
                    OutlinedTextField(
                        value = birthDateMillis?.let { formatBirthDate(it) }.orEmpty(),
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Tanggal lahir") },
                        placeholder = { Text("Pilih tanggal lahir") },
                        readOnly = true,
                        enabled = false,
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
                            disabledPlaceholderColor = Neutral500,
                            disabledTextColor = Neutral800,
                            disabledTrailingIconColor = Neutral500,
                            disabledContainerColor = Color.White
                        )
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = genderExpanded,
                    onExpandedChange = { if (enabled) genderExpanded = !genderExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = gender,
                        onValueChange = {},
                        readOnly = true,
                        enabled = enabled,
                        label = { Text("Jenis kelamin") },
                        placeholder = { Text("Pilih jenis kelamin") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Pilih jenis kelamin"
                            )
                        },
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
                                text = { Text(option) },
                                onClick = {
                                    gender = option
                                    genderExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("Kota") }, enabled = enabled)
                OutlinedTextField(value = status, onValueChange = { status = it }, label = { Text("Pesan") }, enabled = enabled)

                Text(text = "Avatar", style = PoppinsSmallRegular, color = Neutral800)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    editableAvatarOptions.forEach { avatarName ->
                        val avatarRes = remember(avatarName) {
                            context.resources.getIdentifier(avatarName, "drawable", context.packageName)
                        }
                        val isSelected = avatarName == selectedAvatar
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Secondary500 else Neutral300,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable(enabled = enabled) {
                                    selectedAvatar = avatarName
                                    validationError = null
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (avatarRes != 0) {
                                Icon(
                                    painter = painterResource(avatarRes),
                                    contentDescription = avatarName,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(46.dp)
                                )
                            }
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(3.dp)
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(Primary500),
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

                validationError?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, style = PoppinsTinyRegular)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = enabled,
                onClick = {
                    val selectedBirthDate = birthDateMillis
                    when {
                        name.trim().isEmpty() -> validationError = "Nama tidak boleh kosong"
                        selectedBirthDate == null -> validationError = "Tanggal lahir wajib dipilih"
                        gender.isBlank() -> validationError = "Jenis kelamin wajib dipilih"
                        selectedAvatar.isBlank() -> validationError = "Pilih avatar terlebih dahulu"
                        else -> {
                            validationError = null
                            onSave(
                                name.trim(),
                                city.trim(),
                                status.trim(),
                                selectedAvatar,
                                gender,
                                selectedBirthDate
                            )
                        }
                    }
                }
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
private fun EditMbtiDialog(
    selectedMbti: String,
    options: List<String>,
    enabled: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var selected by remember(selectedMbti) { mutableStateOf(selectedMbti) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit MBTI") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                options.chunked(4).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        rowItems.forEach { option ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selected == option) Primary500 else Color.White)
                                    .border(1.dp, if (selected == option) Secondary500 else Neutral300, RoundedCornerShape(10.dp))
                                    .clickable(enabled = enabled) { selected = option }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(option, style = PoppinsSmallRegular)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = enabled && selected.isNotBlank(), onClick = { onSave(selected) }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditMultiSelectDialog(
    title: String,
    options: List<String>,
    initialSelected: List<String>,
    enabled: Boolean,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit,
) {
    val selected = remember(initialSelected) { mutableStateListOf<String>().also { it.addAll(initialSelected) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { option ->
                    val isSelected = selected.contains(option)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isSelected) Primary500 else Color.White)
                            .border(1.dp, if (isSelected) Secondary500 else Neutral300, RoundedCornerShape(24.dp))
                            .clickable(enabled = enabled) {
                                if (isSelected) selected.remove(option) else selected.add(option)
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(option, style = PoppinsTinyRegular, color = Neutral900)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = enabled, onClick = { onSave(selected.toList()) }) {
                Text("Simpan")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

private class ProfileReactiveRepository(
    private val firestore: FirebaseFirestore,
) {
    fun observeProfileState(uid: String, authEmail: String): Flow<ProfileUiState> {
        return combine(
            observeUserDocument(uid),
            observeMaterialProgress(uid),
            observeStreakData(uid)   // ← tambah stream streak langsung dari sub-koleksi
        ) { userDoc, materials, streakFromFirestore ->
            userDoc.toProfileUiState(materials, authEmail, streakFromFirestore)
        }
    }

    suspend fun updateBasicProfile(
        uid: String,
        name: String,
        city: String,
        statusMessage: String,
        avatar: String,
        gender: String,
        birthDateMillis: Long,
    ) {
        firestore.collection("users")
            .document(uid)
            .set(
                mapOf(
                    "name" to name,
                    "city" to city,
                    "statusMessage" to statusMessage,
                    "avatar" to avatar,
                    "gender" to gender,
                    "birthDate" to Timestamp(Date(birthDateMillis))
                ),
                SetOptions.merge()
            )
            .await()
    }

    suspend fun updateFavoriteSubjects(uid: String, subjects: List<String>) {
        firestore.collection("users")
            .document(uid)
            .set(mapOf("favoriteSubjects" to subjects), SetOptions.merge())
            .await()
    }

    suspend fun updateHobbies(uid: String, hobbies: List<String>) {
        firestore.collection("users")
            .document(uid)
            .set(mapOf("hobbies" to hobbies), SetOptions.merge())
            .await()
    }

    suspend fun updateMbti(uid: String, mbti: String) {
        firestore.collection("users")
            .document(uid)
            .set(mapOf("mbti" to mbti), SetOptions.merge())
            .await()
    }

    private fun observeUserDocument(uid: String): Flow<DocumentSnapshot?> = callbackFlow {
        val listener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("ProfileReactiveRepository", "Failed to observe user profile", error)
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(snapshot)
            }
        awaitClose { listener.remove() }
    }

    private fun observeMaterialProgress(uid: String): Flow<List<MaterialProgress>> = callbackFlow {
        val registration: ListenerRegistration = firestore.collection("users")
            .document(uid)
            .collection("materials")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    error?.let { Log.w("ProfileReactiveRepository", "Failed to observe materials", it) }
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val data = snapshot.documents.mapNotNull { doc ->
                    val completedAt = doc.getTimestamp(FIELD_COMPLETED_AT)?.toDate()?.time ?: return@mapNotNull null
                    val subject = doc.getString(FIELD_SUBJECT).orEmpty().ifBlank { "Materi" }
                    MaterialProgress(subject = subject, completedAtMillis = completedAt)
                }
                trySend(data)
            }
        awaitClose { registration.remove() }
    }

    // Observe sub-koleksi streaks/{uid} secara real-time
    // Sumber data utama streak untuk ProfilScreen
    private fun observeStreakData(uid: String): Flow<Int> = callbackFlow {
        val streakRef = firestore.collection("users")
            .document(uid)
            .collection("streaks")
            .document(uid)

        val listener = streakRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("ProfileReactiveRepository", "Failed to observe streak", error)
                trySend(0)
                return@addSnapshotListener
            }
            val current = (snapshot?.getLong("currentStreak") ?: 0L).toInt()
            trySend(current)
        }
        awaitClose { listener.remove() }
    }
}

private fun DocumentSnapshot?.toProfileUiState(
    materials: List<MaterialProgress>,
    authEmail: String,
    streakFromFirestore: Int = 0,
): ProfileUiState {
    val doc = this
    val name = doc?.getString("name").orEmpty()
    val email = doc?.getString("email").orEmpty().ifBlank { authEmail }
    val city = doc?.getString("city").orEmpty()
    val avatar = doc?.getString("avatar").orEmpty().ifBlank { "avatar_default" }
    val gender = doc?.getString("gender").orEmpty()
    val birthDateMillis = doc?.getTimestamp("birthDate")?.toDate()?.time
    val statusMessage = doc?.getString("statusMessage").orEmpty().ifBlank { "Belum menulis pesan apapun" }
    val mbti = doc?.getString("mbti").orEmpty().ifBlank { "-" }
    val favoriteSubjects = (doc?.get("favoriteSubjects") as? List<*>)
        ?.mapNotNull { it as? String }
        .orEmpty()
    val hobbies = (doc?.get("hobbies") as? List<*>)
        ?.mapNotNull { it as? String }
        .orEmpty()

    val age = calculateAge(doc?.getTimestamp("birthDate"))

    val completionCountBySubject = materials.groupingBy { it.subject }.eachCount()
    val topSubject = completionCountBySubject.maxByOrNull { it.value }?.key

    val completedFromDoc = (doc?.getLong("materialsCompleted") ?: 0L).toInt()
    val completedMaterials = if (materials.isNotEmpty()) materials.size else completedFromDoc

    val streakFromDoc = (doc?.getLong("streakCount") ?: 0L).toInt()
    val streakFromMaterials = calculateStreak(materials.map { it.completedAtMillis })
    // Prioritas: streakFromFirestore (sub-koleksi) > streakFromMaterials > streakFromDoc
    val streak = when {
        streakFromFirestore > 0 -> streakFromFirestore
        materials.isNotEmpty() -> streakFromMaterials
        else -> streakFromDoc
    }

    val achievements = buildAchievements(completedMaterials, streak)

    return ProfileUiState(
        isLoading = false,
        name = name,
        email = email,
        age = age,
        city = city,
        avatar = avatar,
        gender = gender,
        birthDateMillis = birthDateMillis,
        statusMessage = statusMessage,
        favoriteSubjects = favoriteSubjects,
        hobbies = hobbies,
        mbti = mbti,
        completedMaterials = completedMaterials,
        streak = streak,
        topSubjectFromProgress = topSubject,
        achievements = achievements
    )
}

private fun formatBirthDate(millis: Long): String {
    return SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date(millis))
}

private fun buildAchievements(materialCount: Int, streak: Int): List<AchievementUi> {
    return listOf(
        AchievementUi(
            title = "Perjalanan Pemula",
            subtitle = "Selesaikan pelajaran pertama",
            current = materialCount,
            target = 1,
            imageRes = com.kelompok2.scarla.R.drawable.achievement_1
        ),
        AchievementUi(
            title = "Bola Api",
            subtitle = "Streak selama 3 hari",
            current = streak,
            target = 3,
            imageRes = com.kelompok2.scarla.R.drawable.achievement_2
        ),
        AchievementUi(
            title = "Kembang Api",
            subtitle = "Streak selama 14 hari",
            current = streak,
            target = 14,
            imageRes = com.kelompok2.scarla.R.drawable.achievement_3
        ),
        AchievementUi(
            title = "Komet",
            subtitle = "Streak selama 30 hari",
            current = streak,
            target = 30,
            imageRes = com.kelompok2.scarla.R.drawable.achievement_4
        ),
        AchievementUi(
            title = "Meteor",
            subtitle = "Streak selama 2 bulan",
            current = streak,
            target = 60,
            imageRes = com.kelompok2.scarla.R.drawable.achievement_5
        ),
        AchievementUi(
            title = "Si Paling Ambis",
            subtitle = "Menyelesaikan 10 pelajaran",
            current = materialCount,
            target = 10,
            imageRes = com.kelompok2.scarla.R.drawable.achievement_6
        )
    )
}

private fun calculateAge(birthDate: Timestamp?): Int? {
    val date = birthDate?.toDate() ?: return null
    val now = Calendar.getInstance()
    val birth = Calendar.getInstance().apply { time = date }
    var age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
    val hasBirthdayPassed = now.get(Calendar.MONTH) > birth.get(Calendar.MONTH) ||
        (now.get(Calendar.MONTH) == birth.get(Calendar.MONTH) &&
            now.get(Calendar.DAY_OF_MONTH) >= birth.get(Calendar.DAY_OF_MONTH))
    if (!hasBirthdayPassed) {
        age -= 1
    }
    return age.takeIf { it >= 0 }
}

private fun calculateStreak(completionMillis: List<Long>): Int {
    if (completionMillis.isEmpty()) return 0

    val completionDays = completionMillis
        .map { millis -> millis / MILLIS_PER_DAY }
        .toSet()

    val today = System.currentTimeMillis() / MILLIS_PER_DAY
    var cursor = when {
        completionDays.contains(today) -> today
        completionDays.contains(today - 1) -> today - 1
        else -> return 0
    }

    var streak = 0
    while (completionDays.contains(cursor)) {
        streak += 1
        cursor -= 1
    }
    return streak
}

private fun subjectEmoji(subject: String): String {
    return when (subject.lowercase()) {
        "matematika" -> "📐"
        "seni" -> "🎨"
        "informatika" -> "💻"
        "bahasa indonesia" -> "🇮🇩"
        "bahasa inggris" -> "🇬🇧"
        "fisika" -> "⚛️"
        "kimia" -> "🧪"
        "biologi" -> "🧬"
        else -> "📚"
    }
}

private fun hobbyEmoji(hobby: String): String {
    return when (hobby.lowercase()) {
        "musik" -> "🎵"
        "gaming" -> "🎮"
        "memasak" -> "🍳"
        "olahraga" -> "🏃"
        "membaca" -> "📖"
        "menulis" -> "✍️"
        "fotografi" -> "📷"
        "menggambar" -> "🖌️"
        else -> "⭐"
    }
}
