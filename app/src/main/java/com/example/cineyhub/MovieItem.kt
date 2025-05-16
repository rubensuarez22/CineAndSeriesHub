package com.example.cineyhub

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MovieItem(
    val id: String,
    val title: String,
    val posterUrl: String,
    val rating: Float,
    val overview: String,
    val releaseDate: String,
    val cast: List<Actor> = emptyList(),
    val trailers: List<Trailer> = emptyList()
) : Parcelable
