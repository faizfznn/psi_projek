package com.kelompok2.scarla.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kelompok2.scarla.ui.components.FriendAcceptedPopup
import com.kelompok2.scarla.ui.components.FriendCard
import com.kelompok2.scarla.ui.viewmodel.CardSwipeDirection
import com.kelompok2.scarla.ui.viewmodel.CariUiState
import com.kelompok2.scarla.ui.viewmodel.CariViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendRequestsScreen(
    navController: NavController? = null,
    viewModel: CariViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val cardOffsetX by animateDpAsState(
        targetValue = when (uiState.swipeDirection) {
            CardSwipeDirection.LEFT -> (-360).dp
            CardSwipeDirection.RIGHT -> 360.dp
            null -> 0.dp
        },
        animationSpec = tween(durationMillis = CariViewModel.CARD_ANIMATION_DURATION_MS.toInt()),
        label = "request_card_offset"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Permintaan Pertemanan") },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.friendRequests.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tidak ada permintaan pertemanan",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val currentRequest = uiState.friendRequests.first()

                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FriendCard(
                            profile = currentRequest,
                            modifier = Modifier.offset(x = cardOffsetX),
                            showMatchCount = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.rejectCurrentRequest() },
                            enabled = uiState.swipeDirection == null,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                        ) {
                            Text("Tolak")
                        }
                        Button(
                            onClick = { viewModel.acceptCurrentRequest() },
                            enabled = uiState.swipeDirection == null,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Text("Terima")
                        }
                    }
                }
            }

            if (uiState.showFriendAcceptedPopup) {
                FriendAcceptedPopup(onDismiss = { viewModel.dismissFriendAcceptedPopup() })
            }
        }
    }
}
