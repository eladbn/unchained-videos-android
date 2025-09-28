package com.github.livingwithhippos.unchained.data.repository

import android.content.SharedPreferences
import com.github.livingwithhippos.unchained.data.local.ProtoStore
import com.github.livingwithhippos.unchained.data.model.ParsedTorrentInfo
import com.github.livingwithhippos.unchained.data.model.TmdbInfo
import com.github.livingwithhippos.unchained.data.model.TmdbSearchResult
import com.github.livingwithhippos.unchained.data.remote.TmdbApiHelper
import timber.log.Timber
import javax.inject.Inject

class TmdbRepository @Inject constructor(
    protoStore: ProtoStore,
    private val tmdbApiHelper: TmdbApiHelper,
    private val preferences: SharedPreferences
) : BaseRepository(protoStore) {
    
    private fun getTmdbApiKey(): String? {
        return preferences.getString("tmdb_api_key", null)?.takeIf { it.isNotBlank() }
    }
    
    suspend fun searchForTorrentInfo(torrentName: String): TmdbInfo? {
        val apiKey = getTmdbApiKey() ?: return null
        
        try {
            val parsedInfo = parseTorrentName(torrentName)
            Timber.d("Parsed torrent info: $parsedInfo")
            
            val searchResponse = if (parsedInfo.isMovie) {
                safeApiCall(
                    call = { tmdbApiHelper.searchMovies(apiKey, parsedInfo.title, parsedInfo.year) },
                    errorMessage = "Error searching movies on TMDB"
                )
            } else {
                safeApiCall(
                    call = { tmdbApiHelper.searchTv(apiKey, parsedInfo.title, parsedInfo.year) },
                    errorMessage = "Error searching TV shows on TMDB"
                )
            }
            
            return if (searchResponse != null) {
                val results = searchResponse.results
                if (!results.isNullOrEmpty()) {
                    convertToTmdbInfo(results.first(), parsedInfo.isMovie)
                } else {
                    // Try multi search as fallback
                    searchMulti(apiKey, parsedInfo.title)
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error searching TMDB")
            return null
        }
    }
    
    private suspend fun searchMulti(apiKey: String, query: String): TmdbInfo? {
        return try {
            val searchResponse = safeApiCall(
                call = { tmdbApiHelper.searchMulti(apiKey, query) },
                errorMessage = "Error in multi search on TMDB"
            )
            
            if (searchResponse != null) {
                val results = searchResponse.results
                if (!results.isNullOrEmpty()) {
                    val firstResult = results.first()
                    val isMovie = firstResult.mediaType == "movie" || firstResult.title != null
                    convertToTmdbInfo(firstResult, isMovie)
                } else null
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in multi search")
            null
        }
    }
    
    private fun convertToTmdbInfo(result: TmdbSearchResult, isMovie: Boolean): TmdbInfo? {
        val id = result.id ?: return null
        val title = if (isMovie) result.title else result.name
        if (title.isNullOrBlank()) return null
        
        val dateString = if (isMovie) result.releaseDate else result.firstAirDate
        val year = dateString?.let { parseYear(it) }
        
        return TmdbInfo(
            id = id,
            title = title,
            overview = result.overview,
            releaseDate = dateString,
            posterPath = result.posterPath,
            voteAverage = result.voteAverage,
            mediaType = if (isMovie) "movie" else "tv",
            year = year
        )
    }
    
    private fun parseYear(dateString: String): Int? {
        return try {
            if (dateString.length >= 4) {
                dateString.substring(0, 4).toIntOrNull()
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    fun parseTorrentName(name: String): ParsedTorrentInfo {
        Timber.d("Original name: $name")
        
        // Remove file extensions
        var workingName = name.replace(Regex("\\.(mkv|mp4|avi|mov|wmv|flv|webm|m4v|3gp|mpg|mpeg)$", RegexOption.IGNORE_CASE), "")
        
        // Extract year (4 digits between 1900-2099)
        val yearRegex = Regex("\\b(19|20)\\d{2}\\b")
        val yearMatch = yearRegex.find(workingName)
        val year = yearMatch?.value?.toIntOrNull()
        
        // Extract season and episode (S##E## format)
        val seasonEpisodeRegex = Regex("S(\\d{1,2})E(\\d{1,2})", RegexOption.IGNORE_CASE)
        val seasonEpisodeMatch = seasonEpisodeRegex.find(workingName)
        
        var season: Int? = null
        var episode: Int? = null
        
        if (seasonEpisodeMatch != null) {
            season = seasonEpisodeMatch.groupValues[1].toIntOrNull()
            episode = seasonEpisodeMatch.groupValues[2].toIntOrNull()
        }
        
        // Now extract the title - simple approach
        var title = workingName
        
        // If there's a year in parentheses like "Movie Title (2019)", extract title before it
        val titleWithYearInParensRegex = Regex("^(.+?)\\s*\\((19|20)\\d{2}\\)", RegexOption.IGNORE_CASE)
        val titleWithYearMatch = titleWithYearInParensRegex.find(title)
        
        if (titleWithYearMatch != null) {
            // Title is before the year in parentheses
            title = titleWithYearMatch.groupValues[1].trim()
        } else {
            // Try to find where the title ends by looking for year or technical terms
            // Split by year if found
            if (yearMatch != null) {
                val yearIndex = title.indexOf(yearMatch.value)
                if (yearIndex > 0) {
                    title = title.substring(0, yearIndex).trim()
                }
            }
            
            // Remove season/episode info from title
            if (seasonEpisodeMatch != null) {
                title = title.replace(seasonEpisodeMatch.value, "", ignoreCase = true).trim()
            }
            
            // Look for common technical terms that indicate end of title
            val technicalTermsRegex = Regex("\\b(?:1080p|720p|480p|4K|BluRay|WEBRip|x264|x265|HEVC)\\b", RegexOption.IGNORE_CASE)
            val techMatch = technicalTermsRegex.find(title)
            if (techMatch != null) {
                val techIndex = title.indexOf(techMatch.value)
                if (techIndex > 0) {
                    title = title.substring(0, techIndex).trim()
                }
            }
        }
        
        // Clean up the title
        title = title
            .replace(".", " ")           // Dots to spaces
            .replace("_", " ")           // Underscores to spaces  
            .replace(Regex("\\s+"), " ") // Multiple spaces to single
            .trim()
        
        // Remove common brackets/parentheses content at the end
        title = title.replace(Regex("\\s*[\\[\\(][^\\]\\)]*[\\]\\)]\\s*$"), "").trim()
        
        val isMovie = season == null && episode == null
        
        Timber.d("Parsed result - Title: '$title', Year: $year, Season: $season, Episode: $episode, IsMovie: $isMovie")
        
        return ParsedTorrentInfo(
            title = title,
            year = year,
            season = season,
            episode = episode,
            isMovie = isMovie
        )
    }
}