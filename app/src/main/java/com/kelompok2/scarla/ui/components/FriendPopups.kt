package com.kelompok2.scarla.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kelompok2.scarla.ui.viewmodel.FriendProfile

@Composable
fun FriendAcceptedPopup(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Berhasil") },
        text = { Text("Selamat, Anda sudah berteman") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Composable
fun SendFriendRequestPopup(
    friend: FriendProfile,
    message: String,
    onMessageChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajukan Pertemanan") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Kirim permintaan pertemanan ke ${friend.name}")
                OutlinedTextField(
                    value = message,
                    onValueChange = onMessageChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .heightIn(min = 100.dp),
                    label = { Text("Pesan") },
                    placeholder = { Text("Tulis pesan (opsional)") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSubmit) {
                Text("Kirim")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun FriendRequestSubmittedPopup(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Berhasil") },
        text = { Text("Pengajuan pertemanan berhasil dikirim") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}
