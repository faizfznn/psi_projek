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
import com.kelompok2.scarla.data.remote.ScarlaApi
import com.kelompok2.scarla.ui.theme.*
import com.kelompok2.scarla.ui.components.*
import com.kelompok2.scarla.navigation.Screen

data class MateriItem(
    val id: String,
    val title: String,
    val duration: String,
    val videoUrl: String
)

@Composable
fun HtmlScreen(
    navController: NavController,
    materialId: String
) {

    val context = LocalContext.current

    var materiList by remember(materialId) {
        mutableStateOf<List<MateriItem>>(emptyList())
    }

    var materialTitle by remember(materialId) {
        mutableStateOf(materialId.uppercase())
    }

    var quizId by remember(materialId) {
        mutableStateOf<String?>(null)
    }

    var isLoading by remember(materialId) {
        mutableStateOf(true)
    }

    var errorMessage by remember(materialId) {
        mutableStateOf<String?>(null)
    }

    var selectedVideo by rememberSaveable(materialId) {
        mutableStateOf<String?>(null)
    }

    var selectedIndex by rememberSaveable(materialId) {
        mutableStateOf(-1)
    }

    val downloadedList = remember(materialId, materiList.size) {
        mutableStateListOf<Boolean>().apply {
            repeat(materiList.size) { add(false) }
        }
    }

    val finishedList = remember(materialId, materiList.size) {
        mutableStateListOf<Boolean>().apply {
            repeat(materiList.size) { add(false) }
        }
    }

    var showDownloadDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showSuccessDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    val allFinished = materiList.isNotEmpty() && finishedList.all { it }

    LaunchedEffect(materialId) {
        isLoading = true
        errorMessage = null
        selectedVideo = null
        selectedIndex = -1

        try {
            val material = ScarlaApi.service.getMaterial(materialId)
            val materialData = material.data

            if (materialData == null) {
                throw IllegalStateException("Material data is empty")
            }

            materialTitle = materialData.title
            quizId = materialData.quizId
                ?: materialData.quizzes.firstOrNull()?.quizId
                ?: materialData.id

            materiList = materialData.videos.map { video ->
                MateriItem(
                    id = video.id,
                    title = video.title,
                    duration = video.duration ?: "--:--",
                    videoUrl = normalizeVideoUrl(video.videoRes ?: video.videoUrl ?: video.url.orEmpty())
                )
            }

            if (materiList.isEmpty() && !quizId.isNullOrBlank()) {
                navController.navigate(Screen.MaterialQuiz.createRoute(quizId!!, materialId)) {
                    popUpTo("material/$materialId") { inclusive = true }
                }
                return@LaunchedEffect
            }
        } catch (e: Exception) {
            materialTitle = materialId.uppercase()
            quizId = materialId
            materiList = emptyList()
            errorMessage = "Gagal memuat materi dari server"
            Log.e("HtmlScreen", "Error loading material: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    // Release player saat component unmount
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    LaunchedEffect(selectedVideo) {
        selectedVideo?.let { videoUrl ->
            try {
                val videoUri = Uri.parse(videoUrl)
                Log.d("HtmlScreen", "Loading video URI: $videoUri")
                exoPlayer.setMediaItem(MediaItem.fromUri(videoUri))
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
            } catch (e: Exception) {
                Log.e("HtmlScreen", "Error loading video: ${e.message}")
            }
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
                    text = materialTitle,
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

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Neutral50),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary500)
                }
            } else if (selectedVideo == null) {

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
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = true
                                controllerShowTimeoutMs = 5000
                            }
                        },
                        update = { view ->
                            view.player = exoPlayer
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                LaunchedEffect(selectedVideo, selectedIndex) {
                    delay(1000)

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
                            enabled = downloadedList[index] && item.videoUrl.isNotBlank(),
                            onClick = {
                                selectedIndex = index

                                selectedVideo = null
                                selectedVideo = item.videoUrl
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
                                quizId?.let { id ->
                                    navController.navigate(Screen.MaterialQuiz.createRoute(id, materialId))
                                }
                            },

                            enabled = allFinished && !quizId.isNullOrBlank(),

                            modifier = Modifier.wrapContentWidth(),

                            buttonType = ButtonType.PRIMARY
                        )
                    }
                }
            }

            if (!isLoading && errorMessage != null) {
                item {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = Error,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            if (!isLoading && materiList.isEmpty()) {
                item {
                    Text(
                        text = "Belum ada video untuk materi ini",
                        color = Neutral700,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
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

            if (selectedIndex in downloadedList.indices) {
                downloadedList[selectedIndex] = true
            }
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

private fun normalizeVideoUrl(rawUrl: String): String {
    if (rawUrl.isBlank()) return rawUrl

    return when {
        rawUrl.contains("localhost:3000") -> rawUrl.replace(
            "http://localhost:3000",
            "https://be-scarla.vercel.app"
        )
        rawUrl.contains("127.0.0.1:3000") -> rawUrl.replace(
            "http://127.0.0.1:3000",
            "https://be-scarla.vercel.app"
        )
        rawUrl.startsWith("/") -> "https://be-scarla.vercel.app$rawUrl"
        else -> rawUrl
    }
}
