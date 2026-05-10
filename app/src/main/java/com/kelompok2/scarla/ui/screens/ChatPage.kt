package com.kelompok2.scarla.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.kelompok2.scarla.R
import com.kelompok2.scarla.navigation.Screen
import com.kelompok2.scarla.ui.theme.Neutral300
import com.kelompok2.scarla.ui.theme.Neutral600
import com.kelompok2.scarla.ui.theme.Neutral700
import com.kelompok2.scarla.ui.theme.Neutral800
import com.kelompok2.scarla.ui.theme.Neutral900
import com.kelompok2.scarla.ui.theme.Primary500
import com.kelompok2.scarla.ui.theme.Secondary500
import com.kelompok2.scarla.ui.viewmodel.ChatContact
import com.kelompok2.scarla.ui.viewmodel.ChatMessage
import com.kelompok2.scarla.ui.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ──────────────────────────────────────────────────────────────────────────────
// ENTRY POINT — Rute: "chat_list"
// Menampilkan daftar percakapan yang dimiliki user saat ini.
// ──────────────────────────────────────────────────────────────────────────────

@Composable
fun ChatPage(
    navController: NavController? = null,
    viewModel: ChatViewModel = viewModel()
) {
    // REAKTIF: collectAsStateWithLifecycle → UI otomatis update saat ada chat baru
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val contacts by viewModel.contactsFlow.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFCFCFC))
    ) {
        // ── Header ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Pesan",
                style = TextStyle(
                    fontSize = 22.sp,
                    fontFamily = FontFamily(Font(R.font.poppins_bold)),
                    fontWeight = FontWeight(700),
                    color = Neutral900
                )
            )
            // Badge jumlah chat
            if (contacts.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Secondary500)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${contacts.size}",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontFamily = FontFamily(Font(R.font.poppins_bold)),
                            fontWeight = FontWeight(700),
                            color = Color.White
                        )
                    )
                }
            }
        }

        // ── Konten ──────────────────────────────────────────────────────
        if (contacts.isEmpty()) {
            EmptyChatState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(contacts, key = { it.uid }) { contact ->
                    ChatContactCard(
                        contact = contact,
                        onClick = {
                            viewModel.openChat(
                                peerUid = contact.uid,
                                peerName = contact.name,
                                peerAvatar = contact.avatarUrl
                            )
                            navController?.navigate(Screen.ChatRoom.createRoute(contact.uid, contact.name))
                        }
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// HALAMAN CHAT ROOM — Rute: "chat_room/{peerUid}/{peerName}"
// Menampilkan percakapan real-time dengan satu orang.
// ──────────────────────────────────────────────────────────────────────────────

@Composable
fun ChatRoomPage(
    peerUid: String,
    peerName: String,
    peerAvatar: String = "",
    navController: NavController? = null,
    viewModel: ChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val myUid = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    val listState = rememberLazyListState()

    // REAKTIF: Mulai listen pesan begitu screen dibuka
    LaunchedEffect(peerUid) {
        viewModel.openChat(
            peerUid = peerUid,
            peerName = peerName,
            peerAvatar = peerAvatar
        )
    }

    // Auto-scroll ke pesan terbaru setiap kali messages berubah
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFCFCFC))
            .navigationBarsPadding()
            .imePadding()
    ) {
        // ── App Bar ──────────────────────────────────────────────────────
        ChatRoomHeader(
            peerName = uiState.activePeerName.ifBlank { peerName },
            peerAvatar = uiState.activePeerAvatar.ifBlank { peerAvatar },
            isOnline = uiState.activePeerIsOnline,
            onBack = {
                viewModel.closeChat()
                navController?.popBackStack()
            },
            onProfileClick = {
                navController?.navigate(com.kelompok2.scarla.navigation.Screen.PeerProfile.createRoute(peerUid))
            }
        )

        // ── Daftar Pesan (REAKTIF — otomatis update saat ada pesan baru) ──
        Box(modifier = Modifier.weight(1f)) {
            if (uiState.isLoadingMessages) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Secondary500
                )
            } else if (uiState.messages.isEmpty()) {
                Text(
                    text = "Belum ada pesan. Mulai percakapan! 👋",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        color = Neutral600
                    ),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.messages, key = { it.id }) { msg ->
                        MessageBubble(
                            message = msg,
                            isMe = msg.senderUid == myUid
                        )
                    }
                }
            }
        }

        // ── Input Field ──────────────────────────────────────────────────
        ChatInputBar(
            value = uiState.currentMessage,
            onValueChange = viewModel::onMessageChange,
            onSend = viewModel::sendMessage
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// KOMPONEN INTERNAL
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun ChatRoomHeader(
    peerName: String,
    peerAvatar: String,
    isOnline: Boolean,
    onBack: () -> Unit,
    onProfileClick: () -> Unit
) {
    val context = LocalContext.current
    val avatarRes = remember(peerAvatar) {
        if (peerAvatar.isNotBlank())
            context.resources.getIdentifier(peerAvatar, "drawable", context.packageName)
        else 0
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .shadow(elevation = 2.dp)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Kembali",
                tint = Neutral800
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .clickable { onProfileClick() }
                .padding(vertical = 4.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Primary500)
                    .border(1.dp, Neutral300, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (avatarRes != 0) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(avatarRes),
                        contentDescription = peerName,
                        modifier = Modifier.size(36.dp)
                    )
                } else {
                    Text(
                        text = peerName.firstOrNull()?.uppercase() ?: "?",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.poppins_bold)),
                            color = Neutral900
                        )
                    )
                }
            }

            Column {
                Text(
                    text = peerName.ifBlank { "Pengguna" },
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontFamily = FontFamily(Font(R.font.poppins_bold)),
                        fontWeight = FontWeight(600),
                        color = Neutral900
                    )
                )
                if (isOnline) {
                    Text(
                        text = "Aktif",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            color = Color(0xFF4CAF50) // Hijau
                        )
                    )
                } else {
                    Text(
                        text = "Tidak Aktif",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            color = Neutral600 // Abu-abu
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    isMe: Boolean
) {
    val bubbleColor = if (isMe) Secondary500 else Color.White
    val textColor = if (isMe) Color.White else Neutral900
    val alignment = if (isMe) Alignment.End else Alignment.Start
    val shape = if (isMe)
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    else
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)

    val timeStr = remember(message.timestampMillis) {
        if (message.timestampMillis > 0L)
            SimpleDateFormat("HH:mm", Locale("id")).format(Date(message.timestampMillis))
        else ""
    }

    // ANIMASI: pesan baru slide in dari bawah
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = alignment
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .shadow(
                        elevation = if (!isMe) 2.dp else 0.dp,
                        shape = shape
                    )
                    .background(bubbleColor, shape)
                    .border(
                        width = if (!isMe) 1.dp else 0.dp,
                        color = if (!isMe) Neutral300 else Color.Transparent,
                        shape = shape
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = message.text,
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            color = textColor
                        )
                    )
                    if (timeStr.isNotBlank()) {
                        Text(
                            text = timeStr,
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                                color = if (isMe) Color.White.copy(alpha = 0.75f) else Neutral600
                            ),
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .shadow(elevation = 4.dp)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(
                    "Tulis pesan...",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontFamily = FontFamily(Font(R.font.poppins_regular)),
                        color = Neutral600
                    )
                )
            },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Secondary500,
                unfocusedBorderColor = Neutral300,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            textStyle = TextStyle(
                fontSize = 13.sp,
                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                color = Neutral900
            ),
            maxLines = 4,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { if (value.isNotBlank()) onSend() })
        )

        // Tombol kirim
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (value.isNotBlank()) Secondary500 else Neutral300)
                .clickable(enabled = value.isNotBlank()) { onSend() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Kirim",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ChatContactCard(
    contact: ChatContact,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val avatarRes = remember(contact.avatarUrl) {
        if (contact.avatarUrl.isNotBlank())
            context.resources.getIdentifier(contact.avatarUrl, "drawable", context.packageName)
        else 0
    }

    val timeStr = remember(contact.lastMessageMillis) {
        if (contact.lastMessageMillis > 0L)
            SimpleDateFormat("HH:mm", Locale("id")).format(Date(contact.lastMessageMillis))
        else ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Primary500)
                .border(1.dp, Neutral300, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (avatarRes != 0) {
                androidx.compose.foundation.Image(
                    painter = painterResource(avatarRes),
                    contentDescription = contact.name,
                    modifier = Modifier.size(46.dp)
                )
            } else {
                Text(
                    text = contact.name.firstOrNull()?.uppercase() ?: "?",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontFamily = FontFamily(Font(R.font.poppins_bold)),
                        color = Neutral900
                    )
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = contact.name.ifBlank { "Pengguna" },
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.poppins_bold)),
                        fontWeight = FontWeight(600),
                        color = Neutral900
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (timeStr.isNotBlank()) {
                    Text(
                        text = timeStr,
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontFamily = FontFamily(Font(R.font.poppins_regular)),
                            color = Neutral600
                        )
                    )
                }
            }
            Text(
                text = contact.lastMessage.ifBlank { "Mulai percakapan..." },
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = FontFamily(Font(R.font.poppins_regular)),
                    color = if (contact.lastMessage.isBlank()) Neutral600 else Neutral700
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    // Divider
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 78.dp)
            .height(1.dp)
            .background(Neutral300)
    )
}

@Composable
private fun EmptyChatState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ChatBubbleOutline,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Neutral300
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Belum ada percakapan",
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.poppins_bold)),
                fontWeight = FontWeight(600),
                color = Neutral700
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Tambahkan teman dan mulai mengobrol!",
            style = TextStyle(
                fontSize = 13.sp,
                fontFamily = FontFamily(Font(R.font.poppins_regular)),
                color = Neutral600
            ),
            textAlign = TextAlign.Center
        )
    }
}
