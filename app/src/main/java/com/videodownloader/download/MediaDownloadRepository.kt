package com.videodownloader.download

import android.os.Environment
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

data class MediaDetails(val title: String, val thumbnail: String?, val durationSeconds: Long)
enum class DownloadMode { VIDEO, AUDIO }
enum class VideoQuality(val label: String, val selector: String) {
    BEST("أفضل جودة", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best"),
    P1080("1080p", "bestvideo[height<=1080]+bestaudio/best[height<=1080]"),
    P720("720p", "bestvideo[height<=720]+bestaudio/best[height<=720]"),
    P480("480p", "bestvideo[height<=480]+bestaudio/best[height<=480]")
}

class MediaDownloadRepository {
    suspend fun analyze(url: String): MediaDetails = withContext(Dispatchers.IO) {
        require(url.startsWith("http://") || url.startsWith("https://")) { "الرابط غير صالح" }
        val request = YoutubeDLRequest(url).apply { addOption("--no-playlist") }
        val info = YoutubeDL.getInstance().getInfo(request)
        MediaDetails(info.title ?: "بدون عنوان", info.thumbnail, (info.duration ?: 0).toLong())
    }

    suspend fun download(
        url: String,
        mode: DownloadMode,
        quality: VideoQuality,
        onProgress: (Float, Long, String) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val destination = File(root, "VideoDownloaderPro").apply {
            check(exists() || mkdirs()) { "تعذر إنشاء مجلد التنزيل" }
        }
        val request = YoutubeDLRequest(url).apply {
            addOption("--no-playlist")
            addOption("--no-mtime")
            addOption("--restrict-filenames")
            addOption("-o", File(destination, "%(title).180B [%(id)s].%(ext)s").absolutePath)
            if (mode == DownloadMode.AUDIO) {
                addOption("-x")
                addOption("--audio-format", "mp3")
                addOption("--audio-quality", "0")
                addOption("--embed-thumbnail")
                addOption("--add-metadata")
            } else {
                addOption("-f", quality.selector)
                addOption("--merge-output-format", "mp4")
            }
        }
        YoutubeDL.getInstance().execute(request, UUID.randomUUID().toString()) { progress, eta, line ->
            onProgress(progress, eta, line)
        }
        destination
    }
}
