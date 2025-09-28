package com.github.livingwithhippos.unchained.data.repository

import android.content.SharedPreferences
import com.github.livingwithhippos.unchained.data.model.NetworkResponse
import com.github.livingwithhippos.unchained.data.model.ParsedTorrentInfo
import com.github.livingwithhippos.unchained.data.model.TmdbInfo
import com.github.livingwithhippos.unchained.data.model.TmdbSearchResult
import com.github.livingwithhippos.unchained.data.remote.TmdbApiHelper
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

class TmdbRepository @Inject constructor(
    private val tmdbApiHelper: TmdbApiHelper,
    private val preferences: SharedPreferences
) {
    
    private fun getTmdbApiKey(): String? {
        return preferences.getString("tmdb_api_key", null)?.takeIf { it.isNotBlank() }
    }
    
    suspend fun searchForTorrentInfo(torrentName: String): TmdbInfo? {
        val apiKey = getTmdbApiKey() ?: return null
        
        try {
            val parsedInfo = parseTorrentName(torrentName)
            Timber.d("Parsed torrent info: $parsedInfo")
            
            val searchResponse = if (parsedInfo.isMovie) {
                tmdbApiHelper.searchMovies(apiKey, parsedInfo.title, parsedInfo.year)
            } else {
                tmdbApiHelper.searchTv(apiKey, parsedInfo.title, parsedInfo.year)
            }
            
            return when (searchResponse) {
                is NetworkResponse.Success -> {
                    val results = searchResponse.body.results
                    if (!results.isNullOrEmpty()) {
                        convertToTmdbInfo(results.first(), parsedInfo.isMovie)
                    } else {
                        // Try multi search as fallback
                        searchMulti(apiKey, parsedInfo.title)
                    }
                }
                is NetworkResponse.Error -> {
                    Timber.e("TMDB API error: ${searchResponse.message}")
                    null
                }
                is NetworkResponse.GenericError -> {
                    Timber.e("TMDB generic error: ${searchResponse.error}")
                    null
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error searching TMDB")
            return null
        }
    }
    
    private suspend fun searchMulti(apiKey: String, query: String): TmdbInfo? {
        return try {
            val searchResponse = tmdbApiHelper.searchMulti(apiKey, query)
            when (searchResponse) {
                is NetworkResponse.Success -> {
                    val results = searchResponse.body.results
                    if (!results.isNullOrEmpty()) {
                        val firstResult = results.first()
                        val isMovie = firstResult.mediaType == "movie" || firstResult.title != null
                        convertToTmdbInfo(firstResult, isMovie)
                    } else null
                }
                else -> null
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
        val cleanName = name.replace(".", " ")
            .replace("_", " ")
            .replace("-", " ")
            .trim()
        
        // Extract year (4 digits)
        val yearRegex = Regex("\\b(19|20)\\d{2}\\b")
        val yearMatch = yearRegex.find(cleanName)
        val year = yearMatch?.value?.toIntOrNull()
        
        // Extract season and episode (S##E## or S##EP## formats)
        val seasonEpisodeRegex = Regex("S(\\d{1,2})E(?:P)?(\\d{1,2})", RegexOption.IGNORE_CASE)
        val seasonEpisodeMatch = seasonEpisodeRegex.find(cleanName)
        
        var season: Int? = null
        var episode: Int? = null
        
        if (seasonEpisodeMatch != null) {
            season = seasonEpisodeMatch.groupValues[1].toIntOrNull()
            episode = seasonEpisodeMatch.groupValues[2].toIntOrNull()
        } else {
            // Try alternative formats like "Season 1" or "S1"
            val seasonRegex = Regex("(?:Season\\s*|S)(\\d{1,2})", RegexOption.IGNORE_CASE)
            val seasonMatch = seasonRegex.find(cleanName)
            season = seasonMatch?.groupValues?.get(1)?.toIntOrNull()
        }
        
        // Clean title (remove year, season/episode info, quality indicators, etc.)
        var title = cleanName
        
        // Remove year
        if (yearMatch != null) {
            title = title.replace(yearMatch.value, "").trim()
        }
        
        // Remove season/episode info
        if (seasonEpisodeMatch != null) {
            title = title.replace(seasonEpisodeMatch.value, "").trim()
        }
        
        // Remove common quality/format indicators
        val qualityRegex = Regex("\\b(?:1080p|720p|480p|4K|HD|BluRay|BRRip|DVDRip|WEBRip|HDTV|x264|x265|HEVC|AC3|DTS|MULTI|DUBBED|SUBBED|UNCUT|EXTENDED|DIRECTORS?|CUT|REPACK|PROPER|REAL|INTERNAL|LIMITED|FESTIVAL|SCREENER|CAM|TS|TC|R5|DVDScr)\\b", RegexOption.IGNORE_CASE)
        title = qualityRegex.replace(title, "").trim()
        
        // Remove common release group indicators (usually at the end in brackets or after dash)
        val releaseGroupRegex = Regex("[-\\[].*$")
        title = releaseGroupRegex.replace(title, "").trim()
        
        // Remove extra spaces
        title = title.replace(Regex("\\s+"), " ").trim()
        
        val isMovie = season == null && episode == null
        
        return ParsedTorrentInfo(
            title = title,
            year = year,
            season = season,
            episode = episode,
            isMovie = isMovie
        )
    }
}