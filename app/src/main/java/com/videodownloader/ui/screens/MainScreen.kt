package com.videodownloader.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen() {
    var url by remember { mutableStateOf("") }
    Scaffold(topBar = { TopAppBar(title = { Text("Video Downloader Pro") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("نزّل الفيديو أو الصوت", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("رابط الفيديو") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            Button(onClick = { /* TODO: connect analysis engine */ }, enabled = url.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("تحليل الرابط") }
            Spacer(Modifier.height(12.dp))
            Text("نسخة تأسيسية — أضف محرك التحميل مع احترام شروط المواقع وحقوق النشر.")
        }
    }
}
