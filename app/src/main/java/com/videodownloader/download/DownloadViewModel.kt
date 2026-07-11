package com.videodownloader.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DownloadUiState(
    val url: String = "", val analyzing: Boolean = false, val downloading: Boolean = false,
    val details: MediaDetails? = null, val quality: VideoQuality = VideoQuality.BEST,
    val progress: Float = 0f, val etaSeconds: Long = 0, val status: String = "",
    val error: String? = null, val completedPath: String? = null
)

class DownloadViewModel(
    private val repository: MediaDownloadRepository = MediaDownloadRepository()
) : ViewModel() {
    private val _state = MutableStateFlow(DownloadUiState())
    val state: StateFlow<DownloadUiState> = _state.asStateFlow()

    fun setUrl(value: String) { _state.value = DownloadUiState(url = value.trim()) }
    fun setQuality(value: VideoQuality) { _state.value = _state.value.copy(quality = value) }
    fun analyze() = viewModelScope.launch {
        val url = state.value.url
        _state.value = state.value.copy(analyzing = true, error = null, details = null)
        runCatching { repository.analyze(url) }
            .onSuccess { _state.value = state.value.copy(analyzing = false, details = it) }
            .onFailure { _state.value = state.value.copy(analyzing = false, error = it.message ?: "فشل التحليل") }
    }
    fun download(mode: DownloadMode) = viewModelScope.launch {
        val snapshot = state.value
        _state.value = snapshot.copy(downloading = true, progress = 0f, error = null, completedPath = null)
        runCatching {
            repository.download(snapshot.url, mode, snapshot.quality) { progress, eta, line ->
                _state.value = state.value.copy(progress = progress / 100f, etaSeconds = eta, status = line.takeLast(100))
            }
        }.onSuccess { _state.value = state.value.copy(downloading = false, progress = 1f, completedPath = it.absolutePath) }
         .onFailure { _state.value = state.value.copy(downloading = false, error = it.message ?: "فشل التنزيل") }
    }
}
