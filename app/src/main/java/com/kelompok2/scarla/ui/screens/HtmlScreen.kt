package com.kelompok2.scarla.ui.screens

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import android.util.Log
import com.kelompok2.scarla.R
import kotlinx.coroutines.delay
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.toMutableStateList
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.MaterialTheme
import com.kelompok2.scarla.ui.theme.*
import com.kelompok2.scarla.ui.components.*

data class MateriItem(
    val title: String,
    val duration: String,
    val videoRes: Int,
    var downloaded: Boolean = false,
    var finished: Boolean = false
)

@Composable
fun HtmlScreen(navController: NavController) {

    val context = LocalContext.current

    val downloadedList = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) {
        mutableStateListOf(false, false, false)
    }

    val finishedList = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) {
        mutableStateListOf(false, false, false)
    }

    val materiList = listOf(
        MateriItem(
            "HTML dasar - Pendahuluan",
            "05:20",
            R.raw.html_intro
        ),
        MateriItem(
            "HTML dasar - Tag",
            "08:11",
            R.raw.html_tag
        ),
        MateriItem(
            "HTML dasar - Form",
            "07:42",
            R.raw.html_form
        )
    )

    var selectedVideo by rememberSaveable {
        mutableStateOf<Int?>(null)
    }

    var selectedIndex by rememberSaveable {
        mutableStateOf(-1)
    }

    var showDownloadDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showSuccessDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var exoPlayer by remember {
        mutableStateOf<ExoPlayer?>(null)
    }

    val allFinished = finishedList.all { it }

    // Release player saat component unmount
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        // HEADER
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),

            border = BorderStroke(
                1.dp,
                Neutral200
            )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = {
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .background(
                            Primary500,
                            CircleShape
                        )
                ) {

                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = "HTML",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // VIDEONYA
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(horizontal = 12.dp),
            shape = RoundedCornerShape(12.dp)
        ) {

            if (selectedVideo == null) {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Neutral50),
                        contentAlignment = Alignment.Center
                    ) {

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Image(
                                painter = painterResource(id = R.drawable.ic_html),
                                contentDescription = null,
                                modifier = Modifier.size(90.dp)
                            )
                        }
                    }

            } else {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    var playerInitialized by remember { mutableStateOf(false) }
                    
                    DisposableEffect(selectedVideo) {
                        try {
                            exoPlayer?.release()
                            exoPlayer = null
                            
                            exoPlayer = ExoPlayer.Builder(context).build().apply {
                                val videoUri = Uri.parse(
                                    "android.resource://${context.packageName}/${selectedVideo}"
                                )
                                Log.d("HtmlScreen", "Loading video URI: $videoUri")
                                setMediaItem(MediaItem.fromUri(videoUri))
                                prepare()
                                playWhenReady = true
                                playerInitialized = true
                            }
                        } catch (e: Exception) {
                            Log.e("HtmlScreen", "Error initializing player: ${e.message}")
                            playerInitialized = false
                        }

                        onDispose {
                            exoPlayer?.release()
                            exoPlayer = null
                        }
                    }

                    if (playerInitialized && exoPlayer != null) {
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    player = exoPlayer
                                    useController = true
                                    controllerShowTimeoutMs = 5000
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                LaunchedEffect(selectedVideo, selectedIndex) {
                    delay(5000)

                    if (selectedIndex != -1) {
                        finishedList[selectedIndex] = true
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Materi",
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {

            itemsIndexed(materiList) { index, item ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                text = item.title,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = item.duration,
                                color = Neutral500
                            )
                        }

                        // DOWNLOAD
                        if (!downloadedList[index]) {

                            IconButton(
                                onClick = {
                                    selectedIndex = index
                                    showDownloadDialog = true
                                }
                            ) {

                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    tint = Neutral700
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            enabled = downloadedList[index],
                            onClick = {
                                selectedIndex = index

                                selectedVideo = null
                                selectedVideo = item.videoRes
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor =
                                    when {
                                        finishedList[index] -> Success
                                        downloadedList[index] -> Primary500
                                        else -> Neutral500
                                    }
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {

                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                when {
                                    finishedList[index] -> "Finish"
                                    downloadedList[index] -> "Mulai"
                                    else -> "Locked"
                                }
                            )
                        }
                    }
                }
            }

            // QUIZ
            item {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                text = "QUIZ",
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "5 soal"
                            )
                        }

                        AppButton(
                            text = "Mulai",
                            onClick = {
                                navController.navigate("quiz_html")
                            },

                            enabled = allFinished,

                            modifier = Modifier.wrapContentWidth(),

                            buttonType = ButtonType.PRIMARY
                        )
                    }
                }
            }
        }
    }

    // DIALOG DOWNLOAD
    if (showDownloadDialog) {

        LaunchedEffect(Unit) {

            delay(2000)

            showDownloadDialog = false
            showSuccessDialog = true

            downloadedList[selectedIndex] = true
        }

        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            title = {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    CircularProgressIndicator()

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Tunggu Sebentar")

                    Text("sedang mengunduh")
                }
            }
        )
    }

    // DIALOG SUKSES
    if (showSuccessDialog) {

        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
            },
            confirmButton = {

                TextButton(
                    onClick = {
                        showSuccessDialog = false
                    }
                ) {
                    Text(
                        text = "OK",
                        color = Neutral900
                    )
                }
            },
            title = {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        text = "SUKSES!",
                        color = Success,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text("berhasil diunduh")
                }
            }
        )
    }
}
