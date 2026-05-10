package com.kelompok2.scarla.ui.screens.cari

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kelompok2.scarla.navigation.Screen
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import com.kelompok2.scarla.ui.components.FriendCard
import com.kelompok2.scarla.ui.components.FriendRequestSubmittedPopup
import com.kelompok2.scarla.ui.components.FriendSearchBar
import com.kelompok2.scarla.ui.components.SendFriendRequestPopup
import com.kelompok2.scarla.ui.viewmodel.CariUiState
import com.kelompok2.scarla.ui.viewmodel.FriendProfile
import com.kelompok2.scarla.ui.viewmodel.CariViewModel

@Composable
fun CariFriendRoute(
    navController: NavController? = null,
    viewModel: CariViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CariFriendScreen(
        navController = navController,
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onSearchCardClick = { friend ->
            if (friend.isFriend) {
                navController?.navigate(Screen.ChatRoom.createRoute(friend.id, friend.name))
            } else {
                viewModel.openFriendRequestPopup(friend)
            }
        },
        onRequestMessageChange = viewModel::onRequestMessageChange,
        onDismissRequestPopup = viewModel::dismissRequestPopup,
        onSubmitRequest = viewModel::submitFriendRequest,
        onDismissRequestSubmittedPopup = viewModel::dismissRequestSubmittedPopup
    )
}

@Composable
private fun CariFriendScreen(
    navController: NavController? = null,
    uiState: CariUiState,
    onSearchQueryChange: (String) -> Unit,
    onSearchCardClick: (FriendProfile) -> Unit,
    onRequestMessageChange: (String) -> Unit,
    onDismissRequestPopup: () -> Unit,
    onSubmitRequest: () -> Unit,
    onDismissRequestSubmittedPopup: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tambahkan Teman",
                    style = MaterialTheme.typography.headlineSmall
                )
                if (uiState.friendRequests.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            navController?.navigate(Screen.FriendRequests.route)
                        }
                    ) {
                        Text(
                            text = "Permintaan (${uiState.friendRequests.size})",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            FriendSearchBar(
                query = uiState.searchQuery,
                onQueryChange = onSearchQueryChange
            )

            if (uiState.searchQuery.isNotBlank()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (uiState.searchResults.isEmpty()) {
                        item {
                            Text(
                                text = "Teman tidak ditemukan",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    } else {
                        items(uiState.searchResults, key = { it.id }) { friend ->
                            FriendCard(
                                profile = friend,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                showMatchCount = false,
                                onClick = { onSearchCardClick(friend) }
                            )
                        }
                    }
                }
            }
        }

        if (uiState.showRequestPopup && uiState.selectedFriend != null) {
            SendFriendRequestPopup(
                friend = uiState.selectedFriend,
                message = uiState.requestMessage,
                onMessageChange = onRequestMessageChange,
                onDismiss = onDismissRequestPopup,
                onSubmit = onSubmitRequest
            )
        }

        if (uiState.showRequestSubmittedPopup) {
            FriendRequestSubmittedPopup(onDismiss = onDismissRequestSubmittedPopup)
        }
    }
}