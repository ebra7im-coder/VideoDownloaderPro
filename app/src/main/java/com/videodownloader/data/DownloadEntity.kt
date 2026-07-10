package com.videodownloader.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val id: String,
    val url: String,
    val title: String,
    val status: String = "PENDING",
    val progress: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
