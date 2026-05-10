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
import com.google.firebase.auth.userProfileChangeRequest
import com.kelompok2.scarla.R
import com.kelompok2.scarla.ui.components.AppButton
import com.kelompok2.scarla.ui.components.AuthTextField
import com.kelompok2.scarla.ui.components.ButtonType
import com.kelompok2.scarla.ui.theme.*

@Composable
fun SignupScreen(
    onBack: () -> Unit,
    onSignupSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
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
                text = "Daftar Akun",
                style = PoppinsH5Bold,
                color = Neutral900
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Buat akun untuk mulai perjalanan belajarmu",
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
                value = name,
                onValueChange = { name = it },
                label = "Nama",
                placeholder = "Masukkan nama lengkap",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                label = "Password",
                placeholder = "Masukkan password",
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            AuthTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Konfirmasi Password",
                placeholder = "Ulangi password",
                isPassword = true,
                passwordVisible = confirmPasswordVisible,
                onTogglePasswordVisibility = { confirmPasswordVisible = !confirmPasswordVisible },
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
                text = if (isLoading) "Memproses..." else "Continue",
                buttonType = ButtonType.PRIMARY,
                enabled = !isLoading,
                onClick = {
                    val trimmedName = name.trim()
                    val trimmedEmail = email.trim()
                    errorMessage = null
                    when {
                        trimmedName.isEmpty() -> errorMessage = "Nama tidak boleh kosong"
                        trimmedEmail.isEmpty() -> errorMessage = "Email tidak boleh kosong"
                        password.length < 6 -> errorMessage = "Password minimal 6 karakter"
                        password != confirmPassword -> errorMessage = "Konfirmasi password tidak sama"
                        else -> {
                            isLoading = true
                            auth.createUserWithEmailAndPassword(trimmedEmail, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val firebaseUser = task.result.user
                                        if (firebaseUser == null) {
                                            isLoading = false
                                            errorMessage = "Registrasi gagal. Coba lagi."
                                            return@addOnCompleteListener
                                        }
                                        firebaseUser.updateProfile(
                                            userProfileChangeRequest {
                                                this.displayName = trimmedName
                                            }
                                        ).addOnCompleteListener { profileTask ->
                                            isLoading = false
                                            if (profileTask.isSuccessful) {
                                                onSignupSuccess()
                                            } else {
                                                errorMessage = profileTask.exception
                                                    ?.localizedMessage
                                                    ?: "Gagal menyimpan profil. Coba lagi."
                                            }
                                        }
                                    } else {
                                        isLoading = false
                                        errorMessage = task.exception?.localizedMessage
                                            ?: "Registrasi gagal. Coba lagi."
                                    }
                                }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Sudah punya akun? ",
                    style = PoppinsSmallRegular,
                    color = Neutral700
                )
                Text(
                    text = "Masuk",
                    style = PoppinsSmallMedium,
                    color = Secondary500,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}