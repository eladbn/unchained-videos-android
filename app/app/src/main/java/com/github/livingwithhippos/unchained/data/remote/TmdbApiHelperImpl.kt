package com.github.livingwithhippos.unchained.data.remote

import com.github.livingwithhippos.unchained.data.model.NetworkResponse
import com.github.livingwithhippos.unchained.data.model.TmdbMovieDetails
import com.github.livingwithhippos.unchained.data.model.TmdbSearchResponse
import com.github.livingwithhippos.unchained.data.model.TmdbTvDetails
import com.github.livingwithhippos.unchained.data.repository.BaseRepository
import javax.inject.Inject

class TmdbApiHelperImpl @Inject constructor(private val tmdbApi: TmdbApi) : 
    BaseRepository(), TmdbApiHelper {
    
    override suspend fun searchMulti(
        apiKey: String,
        query: String,
        page: Int
    ): NetworkResponse<TmdbSearchResponse> {
        return safeApiCall { tmdbApi.searchMulti(apiKey, query, page) }
    }
    
    override suspend fun searchMovies(
        apiKey: String,
        query: String,
        year: Int?,
        page: Int
    ): NetworkResponse<TmdbSearchResponse> {
        return safeApiCall { tmdbApi.searchMovies(apiKey, query, year, page) }
    }
    
    override suspend fun searchTv(
        apiKey: String,
        query: String,
        year: Int?,
        page: Int
    ): NetworkResponse<TmdbSearchResponse> {
        return safeApiCall { tmdbApi.searchTv(apiKey, query, year, page) }
    }
    
    override suspend fun getMovieDetails(
        movieId: Int,
        apiKey: String
    ): NetworkResponse<TmdbMovieDetails> {
        return safeApiCall { tmdbApi.getMovieDetails(movieId, apiKey) }
    }
    
    override suspend fun getTvDetails(
        tvId: Int,
        apiKey: String
    ): NetworkResponse<TmdbTvDetails> {
        return safeApiCall { tmdbApi.getTvDetails(tvId, apiKey) }
    }
}