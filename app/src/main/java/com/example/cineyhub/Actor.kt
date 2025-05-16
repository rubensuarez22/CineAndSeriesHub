package com.example.cineyhub
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Actor(
    val name: String,
    val photoUrl: String
): Parcelable
