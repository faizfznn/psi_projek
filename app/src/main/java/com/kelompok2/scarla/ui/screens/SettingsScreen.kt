package com.kelompok2.scarla.ui.screens

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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.kelompok2.scarla.ui.components.AppButton
import com.kelompok2.scarla.ui.components.ButtonType
import com.kelompok2.scarla.ui.theme.AuthBackground
import com.kelompok2.scarla.ui.theme.Neutral300
import com.kelompok2.scarla.ui.theme.Neutral600
import com.kelompok2.scarla.ui.theme.Neutral700
import com.kelompok2.scarla.ui.theme.Neutral800
import com.kelompok2.scarla.ui.theme.Neutral900
import com.kelompok2.scarla.ui.theme.PoppinsH5Bold
import com.kelompok2.scarla.ui.theme.PoppinsSmallMedium
import com.kelompok2.scarla.ui.theme.PoppinsSmallRegular
import com.kelompok2.scarla.ui.theme.PoppinsTinyRegular

private data class SettingItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController? = null,
    onBack: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    var showLogoutConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .border(1.dp, Neutral300, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Neutral800
                )
            }
            Text(
                text = "Pengaturan",
                style = PoppinsH5Bold,
                color = Neutral900
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Settings Items
        SettingMenuItem(
            title = "Notifikasi",
            icon = Icons.Default.Notifications,
            onClick = { /* Handle notification settings */ }
        )

        SettingMenuItem(
            title = "Setelah Obrokan",
            icon = Icons.Default.Settings,
            onClick = { /* Handle other settings */ }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Log Out Button
        AppButton(
            text = "Log Out",
            buttonType = ButtonType.PRIMARY,
            onClick = { showLogoutConfirmation = true },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Logout Confirmation Bottom Sheet
    if (showLogoutConfirmation) {
        ModalBottomSheet(
            onDismissRequest = { showLogoutConfirmation = false },
            containerColor = Color.White,
            scrimColor = Color.Black.copy(alpha = 0.32f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Keluar",
                    style = PoppinsH5Bold,
                    color = Neutral900
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Yakin mau keluar?",
                    style = PoppinsSmallRegular,
                    color = Neutral600,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Batal button
                    TextButton(
                        onClick = { showLogoutConfirmation = false },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .border(1.dp, Neutral300, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Batal",
                            style = PoppinsSmallMedium,
                            color = Neutral900
                        )
                    }

                    // Keluar button
                    TextButton(
                        onClick = {
                            showLogoutConfirmation = false
                            auth.signOut()
                            navController?.navigate(com.kelompok2.scarla.navigation.Screen.AuthChoice.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .background(
                                color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Keluar",
                            style = PoppinsSmallMedium,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SettingMenuItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Neutral300, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Neutral700,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = title,
            style = PoppinsTinyRegular,
            color = Neutral900
        )
    }
}
