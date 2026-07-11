package com.videodownloader

import android.app.Application
import android.util.Log
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL

class VideoDownloaderApp : Application() {
    var engineReady: Boolean = false
        private set

    override fun onCreate() {
        super.onCreate()
        engineReady = try {
            YoutubeDL.getInstance().init(this)
            FFmpeg.getInstance().init(this)
            true
        } catch (error: Exception) {
            Log.e("VideoDownloaderApp", "Engine initialization failed", error)
            false
        }
    }
}
