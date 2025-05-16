package com.example.cineyhub
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Trailer(
    val title: String,
    val videoKey: String,
    val thumbnail: String = "https://img.youtube.com/vi/$videoKey/hqdefault.jpg"
): Parcelable
