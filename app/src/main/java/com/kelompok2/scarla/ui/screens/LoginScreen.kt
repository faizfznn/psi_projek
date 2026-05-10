package com.kelompok2.scarla.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.kelompok2.scarla.R
import com.kelompok2.scarla.ui.components.AppButton
import com.kelompok2.scarla.ui.components.AuthTextField
import com.kelompok2.scarla.ui.components.ButtonType
import com.kelompok2.scarla.ui.theme.*

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onLoginSuccess: (String) -> Unit,
    onNavigateToSignup: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val auth = remember { FirebaseAuth.getInstance() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Masuk",
                style = PoppinsH5Bold,
                color = Neutral900
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Selamat datang kembali!",
                style = PoppinsSmallRegular,
                color = Neutral600
            )

            Spacer(modifier = Modifier.height(20.dp))

            Image(
                painter = painterResource(R.drawable.maskot_splash),
                contentDescription = "Maskot burung hantu",
                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            AuthTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "Masukkan email",
                keyboardType = KeyboardType.Email,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            AuthTextField(
                value = password,
                onValueChange = { password = it },
                label = "Kata Sandi",
                placeholder = "Masukkan kata sandi",
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
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
                text = if (isLoading) "Masuk..." else "Masuk",
                buttonType = ButtonType.PRIMARY,
                enabled = !isLoading,
                onClick = {
                    errorMessage = null
                    when {
                        email.isBlank() -> errorMessage = "Email tidak boleh kosong"
                        password.isBlank() -> errorMessage = "Kata sandi tidak boleh kosong"
                        else -> {
                            isLoading = true
                            auth.signInWithEmailAndPassword(email.trim(), password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        val uid = auth.currentUser?.uid
                                        if (uid != null) {
                                            onLoginSuccess(uid)
                                        } else {
                                            errorMessage = "Login gagal. UID pengguna tidak ditemukan."
                                        }
                                    } else {
                                        errorMessage = task.exception?.localizedMessage
                                            ?: "Login gagal. Periksa email dan kata sandi."
                                    }
                                }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            AppButton(
                text = "Masuk User 1",
                buttonType = ButtonType.PRIMARY,
                enabled = !isLoading,
                onClick = {
                    isLoading = true
                    email = "tes@tes.com"
                    password = "121212"
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                val uid = auth.currentUser?.uid
                                if (uid != null) {
                                    onLoginSuccess(uid)
                                } else {
                                    errorMessage = "Login gagal. UID pengguna tidak ditemukan."
                                }
                            } else {
                                errorMessage = task.exception?.localizedMessage
                                    ?: "Login gagal. Periksa email dan kata sandi."
                            }  
                        }
                }
            )

            AppButton(
                text = "Masuk User 2",
                buttonType = ButtonType.PRIMARY,
                enabled = !isLoading,
                onClick = {
                    isLoading = true
                    email = "tis@tis.com"
                    password = "121212"
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                val uid = auth.currentUser?.uid
                                if (uid != null) {
                                    onLoginSuccess(uid)
                                } else {
                                    errorMessage = "Login gagal. UID pengguna tidak ditemukan."
                                }
                            } else {
                                errorMessage = task.exception?.localizedMessage
                                    ?: "Login gagal. Periksa email dan kata sandi."
                            }  
                        }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Belum punya akun? ",
                    style = PoppinsSmallRegular,
                    color = Neutral700
                )
                Text(
                    text = "Daftar",
                    style = PoppinsSmallMedium,
                    color = Secondary500,
                    modifier = Modifier.clickable { onNavigateToSignup() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
