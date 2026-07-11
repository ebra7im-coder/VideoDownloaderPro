package com.videodownloader.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.videodownloader.download.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(vm: DownloadViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    var qualityMenu by remember { mutableStateOf(false) }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Video Downloader Pro") }) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("تنزيل الوسائط المسموح بها", style = MaterialTheme.typography.headlineSmall)
            Text("استخدم روابط لمحتوى تملكه أو لديك إذن بتنزيله. لا يدعم التطبيق تجاوز DRM.", style = MaterialTheme.typography.bodySmall)

            OutlinedTextField(
                value = state.url,
                onValueChange = vm::setUrl,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("رابط الوسائط") },
                placeholder = { Text("https://example.com/video") },
                singleLine = true,
                enabled = !state.analyzing && !state.downloading
            )
            Button(
                onClick = vm::analyze,
                enabled = state.url.isNotBlank() && !state.analyzing && !state.downloading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.analyzing) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Icon(Icons.Default.Search, null)
                Spacer(Modifier.width(8.dp)); Text(if (state.analyzing) "جارٍ التحليل…" else "تحليل الرابط")
            }

            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            state.details?.let { details ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (!details.thumbnail.isNullOrBlank()) {
                            AsyncImage(
                                model = details.thumbnail, contentDescription = null,
                                modifier = Modifier.fillMaxWidth().height(190.dp), contentScale = ContentScale.Crop
                            )
                        }
                        Text(details.title, style = MaterialTheme.typography.titleMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
                        Text("المدة: ${formatDuration(details.durationSeconds)}")

                        Box {
                            OutlinedButton(onClick = { qualityMenu = true }, modifier = Modifier.fillMaxWidth()) {
                                Text("الجودة: ${state.quality.label}")
                            }
                            DropdownMenu(expanded = qualityMenu, onDismissRequest = { qualityMenu = false }) {
                                VideoQuality.entries.forEach { quality ->
                                    DropdownMenuItem(text = { Text(quality.label) }, onClick = {
                                        vm.setQuality(quality); qualityMenu = false
                                    })
                                }
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { vm.download(DownloadMode.VIDEO) }, enabled = !state.downloading, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Download, null); Spacer(Modifier.width(4.dp)); Text("فيديو")
                            }
                            OutlinedButton(onClick = { vm.download(DownloadMode.AUDIO) }, enabled = !state.downloading, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.AudioFile, null); Spacer(Modifier.width(4.dp)); Text("MP3")
                            }
                        }
                    }
                }
            }

            if (state.downloading) {
                LinearProgressIndicator(progress = { state.progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                Text("${(state.progress * 100).toInt()}% — المتبقي تقريبًا ${state.etaSeconds} ثانية")
                Text(state.status, style = MaterialTheme.typography.bodySmall, maxLines = 2)
            }
            state.completedPath?.let {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                    Text("اكتمل التنزيل. الملفات داخل Downloads/VideoDownloaderPro", Modifier.padding(14.dp))
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun formatDuration(total: Long): String {
    val hours = total / 3600
    val minutes = total % 3600 / 60
    val seconds = total % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%d:%02d".format(minutes, seconds)
}
