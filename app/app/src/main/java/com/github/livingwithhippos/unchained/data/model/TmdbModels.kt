package com.github.livingwithhippos.unchained.data.model

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class TmdbSearchResponse(
    @Json(name = "page") val page: Int?,
    @Json(name = "results") val results: List<TmdbSearchResult>?,
    @Json(name = "total_pages") val totalPages: Int?,
    @Json(name = "total_results") val totalResults: Int?
)

@Keep
@JsonClass(generateAdapter = true)
data class TmdbSearchResult(
    @Json(name = "id") val id: Int?,
    @Json(name = "title") val title: String?, // For movies
    @Json(name = "name") val name: String?, // For TV shows
    @Json(name = "overview") val overview: String?,
    @Json(name = "release_date") val releaseDate: String?, // For movies
    @Json(name = "first_air_date") val firstAirDate: String?, // For TV shows
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "backdrop_path") val backdropPath: String?,
    @Json(name = "vote_average") val voteAverage: Float?,
    @Json(name = "vote_count") val voteCount: Int?,
    @Json(name = "media_type") val mediaType: String? // "movie" or "tv"
)

@Keep
@JsonClass(generateAdapter = true)
data class TmdbMovieDetails(
    @Json(name = "id") val id: Int?,
    @Json(name = "title") val title: String?,
    @Json(name = "overview") val overview: String?,
    @Json(name = "release_date") val releaseDate: String?,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "backdrop_path") val backdropPath: String?,
    @Json(name = "vote_average") val voteAverage: Float?,
    @Json(name = "vote_count") val voteCount: Int?,
    @Json(name = "runtime") val runtime: Int?,
    @Json(name = "genres") val genres: List<TmdbGenre>?
)

@Keep
@JsonClass(generateAdapter = true)
data class TmdbTvDetails(
    @Json(name = "id") val id: Int?,
    @Json(name = "name") val name: String?,
    @Json(name = "overview") val overview: String?,
    @Json(name = "first_air_date") val firstAirDate: String?,
    @Json(name = "last_air_date") val lastAirDate: String?,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "backdrop_path") val backdropPath: String?,
    @Json(name = "vote_average") val voteAverage: Float?,
    @Json(name = "vote_count") val voteCount: Int?,
    @Json(name = "number_of_seasons") val numberOfSeasons: Int?,
    @Json(name = "number_of_episodes") val numberOfEpisodes: Int?,
    @Json(name = "genres") val genres: List<TmdbGenre>?
)

@Keep
@JsonClass(generateAdapter = true)
data class TmdbGenre(
    @Json(name = "id") val id: Int?,
    @Json(name = "name") val name: String?
)

@Keep
data class TmdbInfo(
    val id: Int,
    val title: String,
    val overview: String?,
    val releaseDate: String?,
    val posterPath: String?,
    val voteAverage: Float?,
    val mediaType: String, // "movie" or "tv"
    val year: Int?
) {
    fun getPosterUrl(): String? {
        return posterPath?.let { "https://image.tmdb.org/t/p/w342$it" }
    }
    
    fun getDisplayTitle(): String {
        return if (year != null) {
            "$title ($year)"
        } else {
            title
        }
    }
}

@Keep
data class ParsedTorrentInfo(
    val title: String,
    val year: Int? = null,
    val season: Int? = null,
    val episode: Int? = null,
    val isMovie: Boolean = season == null && episode == null
)