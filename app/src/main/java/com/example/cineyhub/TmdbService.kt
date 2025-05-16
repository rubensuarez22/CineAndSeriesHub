// TmdbService.kt
package com.example.cineyhub

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbService {
    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String
    ): TmdbSearchResponse

    @GET("movie/now_playing")
    suspend fun getNowPlaying(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES",
        @Query("page") page: Int = 1
    ): NowPlayingResponse

    // <— Aquí faltaba la anotación @GET
    @GET("movie/popular")
    suspend fun getPopular(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES",
        @Query("page") page: Int = 1
    ): NowPlayingResponse

    @GET("movie/{movie_id}")
    suspend fun getMovieDetail(
        @Path("movie_id") id: String,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES",
        @Query("append_to_response") append: String = "credits,videos"
    ): MovieDetailResponse

    @GET("movie/{movie_id}/credits")
    suspend fun getMovieCredits(
        @Path("movie_id") movieId: String,
        @Query("api_key") apiKey: String
    ): CreditsResponse

    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(
        @Path("movie_id") movieId: String,
        @Query("api_key") apiKey: String
    ): VideosResponse

}

data class MovieDetailResponse(
    @SerializedName("poster_path")     val poster_path: String?,
    val title: String,
    @SerializedName("release_date")    val release_date: String?,
    val runtime: Int?,                          // duración en minutos
    @SerializedName("vote_average")    val vote_average: Double,
    val overview: String?,
    val credits: CreditsResponse?,
    val videos: VideosResponse?
)


data class CreditsResponse(
    val cast: List<CastMemberDto>
)
data class CastMemberDto(
    val name: String,
    @SerializedName("profile_path") val profile_path: String?
)

data class VideosResponse(
    val results: List<VideoDto>
)
data class VideoDto(
    val name: String,
    @SerializedName("key")    val key: String,    // en el JSON se llama "key"
    val site: String,
    val type: String
)

data class TmdbSearchResponse(
    val results: List<TmdbMovie>
)

data class TmdbMovie(
    val id: Int,
    val title: String?,
    val name: String?,            // para series
    val poster_path: String?,
    val vote_average: Double
)

data class NowPlayingResponse(
    val page: Int,
    val results: List<MovieDto>,
    val total_pages: Int,
    val total_results: Int
)

data class MovieDto(
    val id: Int,
    val title: String,
    val poster_path: String?,
    val vote_average: Double,
    val overview: String?,
    val release_date: String?
)













